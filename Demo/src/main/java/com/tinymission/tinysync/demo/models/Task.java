package com.tinymission.tinysync.demo.models;

import com.tinymission.tinysync.db.DbBelongsTo;
import com.tinymission.tinysync.db.DbColumn;
import com.tinymission.tinysync.db.DbModel;
import com.tinymission.tinysync.validation.ValidateNotNull;

import org.joda.time.DateTime;

public class Task extends DbModel {

    @DbColumn
    @ValidateNotNull
    public String title;

    @DbColumn
    public DateTime createdAt;

    @DbColumn
    public int importance;

    public final DbBelongsTo<User> user = new DbBelongsTo<User>(this, User.class);
}
