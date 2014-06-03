package com.tinymission.tinysync.sync;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.tinymission.tinysync.db.DbCollection;
import com.tinymission.tinysync.db.DbModel;
import com.tinymission.tinysync.db.DbSet;

import org.joda.time.DateTime;

/**
 * Encapsulates sync information for one entity (model) type.
 */
public class SyncEntity {

    private String name;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    private JsonObject scope;

    private JsonObject[] created;

    public JsonObject[] getCreated() {
        return created;
    }

    private JsonObject[] updated;

    public JsonObject[] getUpdated() {
        return updated;
    }


    //region Parsing Responses

    private DbModel[] parseArray(DbCollection collection, JsonObject[] array, boolean isPersisted) {
        Gson gson = Syncer.createGson();
        if (array == null)
            return new DbModel[0];
        DbModel[] records = new DbModel[array.length];
        for (int i=0; i<array.length; i++) {
            JsonObject obj = array[i];
            records[i] = (DbModel) gson.fromJson(obj, collection.getModelClass());
            records[i].setIsPersisted(isPersisted);
        }
        return records;
    }

    /**
     * Parses the created records into proper model objects.
     * @param collection the collection that the records will be added to
     * @param <T>
     * @return
     */
    public <T extends DbModel> DbModel[] parseCreated(DbCollection<T> collection) {
        return parseArray(collection, created, false);
    }

    /**
     * Parses the created records into proper model objects.
     * @param collection the collection that the records will be added to
     * @param <T>
     * @return
     */
    public <T extends DbModel> DbModel[] parseUpdated(DbCollection<T> collection) {
        return parseArray(collection, updated, true);
    }

    //endregion


    //region Generating Requests

    public void populate(DbCollection collection, DateTime lastSynced) {
        Gson gson = Syncer.createGson();
        DbSet createdRecords = collection.where("sync_state", DbModel.SyncState.created).run();
        created = new JsonObject[createdRecords.size()];
        for (int i=0; i<created.length; i++) {
            created[i] = gson.toJsonTree(createdRecords.next()).getAsJsonObject();
        }

        DbSet updatedRecords = collection.where("sync_state", DbModel.SyncState.updated).run();
        updated = new JsonObject[updatedRecords.size()];
        for (int i=0; i<updated.length; i++) {
            updated[i] = gson.toJsonTree(updatedRecords.next()).getAsJsonObject();
        }
    }

    //endregion
}
