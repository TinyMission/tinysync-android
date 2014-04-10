package com.tinymission.tinysync.sync;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.google.gson.*;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;
import com.tinymission.tinysync.db.DbBelongsTo;
import com.tinymission.tinysync.db.DbCollection;
import com.tinymission.tinysync.db.DbContext;
import com.tinymission.tinysync.db.DbHasMany;
import com.tinymission.tinysync.db.DbModel;
import com.tinymission.tinysync.db.ObjectId;
import com.tinymission.tinysync.db.SaveResult;

import org.joda.time.DateTime;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.util.Date;

/**
 * Facilitates synchronization between the client and the server.
 */
public class Syncer {

    static final String LogTag = "tinysync.sync.Syncer";

    Context _androidContext;

    public Syncer(DbContext context) {
        _context = context;
        _androidContext = _context.getAndroidContext();
    }

    private DbContext _context;


    //region Preferences

    /**
     * Name of Android preferences.
     */
    public static final String PrefsName = "tinysync";

    /**
     * Key used for storing the last synced date in preferences.
     */
    public static final String LastSyncedKey = "tinysync_last_synced";

    /**
     * @return the last synced date from the preferences.
     */
    public DateTime getLastSynced() {
        SharedPreferences prefs = _androidContext.getSharedPreferences(PrefsName, Context.MODE_PRIVATE);
        String lastSyncedString = prefs.getString(LastSyncedKey, "2000-01-01T01:00:00-0000");
        return new DateTime(lastSyncedString);
    }

    /**
     * @param lastSynced the last synced date to set in the preferences
     */
    public void setLastSynced(DateTime lastSynced) {
        SharedPreferences prefs = _androidContext.getSharedPreferences(PrefsName, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(LastSyncedKey, lastSynced.toString());
        editor.commit();
    }

    //endregion


    //region Response Processing

    /**
     * Parses a JSON sync request or response into the object representation
     * @param json
     * @return
     * @throws IOException
     */
    public SyncRequest parseJson(String json) throws IOException {
        InputStream stream = new ByteArrayInputStream(json.getBytes("UTF-8"));
        return parseJson(stream);
    }

    /**
     * Parses an input stream containing a JSON sync request or response into the object representation
     * @param in
     * @return
     * @throws IOException
     */
    public SyncRequest parseJson(InputStream in) throws IOException {
        Gson gson = createGson();
        InputStreamReader isr = new InputStreamReader(in);
        return gson.fromJson(isr, SyncRequest.class);
    }

    /**
     * Processes the sync response and saves all new or updated records to the local database.
     * @param response
     */
    public SaveResult processResponse(SyncRequest response) {
        for (SyncEntity entity: response.getEntities()) {
            DbCollection collection = _context.getCollection(entity.getName());
            collection.addAll(entity.parseCreated(collection));
            collection.addAll(entity.parseUpdated(collection));
        }
        setLastSynced(response.getLastSynced());
        return _context.save();
    }

    /**
     * Shorthand for processResponse(parseJson(stream))
     * @param stream an input stream containing a sync response
     */
    public void parseAndProcessResponse(InputStream stream) throws IOException {
        processResponse(parseJson(stream));
    }

    //endregion


    //region Generating Requests

    public SyncRequest generateRequest() {
        SyncRequest request = new SyncRequest();
        DateTime lastSynced = getLastSynced();

        String[] order = _context.getSyncOrder();
        SyncEntity[] entities = new SyncEntity[order.length];
        for (int i=0; i<order.length; i++) {
            String entityName = order[i];
            SyncEntity entity = new SyncEntity();
            entity.setName(entityName);
            entity.populate(_context.getCollection(entityName), lastSynced);
            entities[i] = entity;
        }
        request.setEntities(entities);

        return request;
    }

    //endregion


    //region Serializers

    public static Gson createGson() {
        return new GsonBuilder()
                .registerTypeAdapter(DateTime.class, new DateTimeTypeAdapter())
                .registerTypeAdapter(ObjectId.class, new ObjectIdTypeAdapter())
                .registerTypeAdapterFactory(new EnumTypeAdapterFactory())
                .setExclusionStrategies(new JsonExclusionStrategy())
                .create();
    }

    private static class DateTimeTypeAdapter
            implements JsonSerializer<DateTime>, JsonDeserializer<DateTime> {

        @Override
        public JsonElement serialize(DateTime src, Type srcType, JsonSerializationContext context) {
            return new JsonPrimitive(src.toString());
        }

        @Override
        public DateTime deserialize(JsonElement json, Type type, JsonDeserializationContext context)
                throws JsonParseException {
            try {
                return new DateTime(json.getAsString());
            } catch (IllegalArgumentException e) {
                // May be it came in formatted as a java.util.Date, so try that
                Date date = context.deserialize(json, Date.class);
                return new DateTime(date);
            }
        }
    }

    private static class ObjectIdTypeAdapter
        implements JsonSerializer<ObjectId>, JsonDeserializer<ObjectId> {

        @Override
        public ObjectId deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            return new ObjectId(json.getAsString());
        }

        @Override
        public JsonElement serialize(ObjectId src, Type typeOfSrc, JsonSerializationContext context) {
            return new JsonPrimitive(src.toHexString());
        }
    }

    private static class EnumTypeAdapterFactory implements TypeAdapterFactory {

        public <T> TypeAdapter<T> create(Gson gson, TypeToken<T> type) {
            final Class<T> rawType = (Class<T>) type.getRawType();
            if (!rawType.isEnum())
                return null;
            Log.d(LogTag, "EnumTypeAdapterFactory: generating adapter for type: " + rawType.getName() + " (enum: " + rawType.isEnum() + ")");

            return new TypeAdapter<T>() {
                public void write(JsonWriter out, T value) throws IOException {
                    if (value == null) {
                        out.nullValue();
                    } else {
                        out.value(value.toString());
                    }
                }

                public T read(JsonReader reader) throws IOException {
                    if (reader.peek() == JsonToken.NULL) {
                        reader.nextNull();
                        return null;
                    } else {
                        String valString = reader.nextString();
                        for (T val: rawType.getEnumConstants()) {
                            if (val.toString().equalsIgnoreCase(valString))
                                return val;
                        }
                        return null;
                    }
                }
            };
        }
    }

    private static class JsonExclusionStrategy implements ExclusionStrategy {
        @Override
        public boolean shouldSkipField(FieldAttributes f) {
            if (f.getDeclaredClass().isAssignableFrom(DbHasMany.class)
                    || f.getDeclaredClass().isAssignableFrom(DbBelongsTo.class))
                return true;
            return false;
        }

        @Override
        public boolean shouldSkipClass(Class<?> clazz) {
            return false;
        }
    }

    //endregion
}
