package com.tinymission.tinysync.sync;

import com.google.gson.JsonObject;
import com.tinymission.tinysync.db.DbCollection;
import com.tinymission.tinysync.db.DbModel;

import org.joda.time.DateTime;

import java.util.HashMap;

/**
 * Object representation of the JSON sync request/response used to synchronize with the server.
 */
public class SyncRequest {

    public SyncRequest() {

    }

    public SyncRequest(DateTime lastSynced) {
        last_synced = lastSynced;
    }

    private DateTime last_synced;

    public DateTime getLastSynced() {
        return last_synced;
    }

    private SyncEntity[] entities;

    public SyncEntity[] getEntities() {
        return entities;
    }

    public void setEntities(SyncEntity[] entities) {
        this.entities = entities;
    }
}
