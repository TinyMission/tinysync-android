package com.tinymission.tinysync.demo.models;

import android.content.Context;

import com.tinymission.tinysync.db.DbCollection;
import com.tinymission.tinysync.db.DbContext;

/**
 * Database context for the demo application.
 */
public class DemoContext extends DbContext {
    public DemoContext(Context androidContext) {
        super(androidContext);
    }

    public final DbCollection<User> users = new DbCollection<>(User.class);

    public final DbCollection<Task> tasks = new DbCollection<>(Task.class);
}
