package com.tinymission.tinysync.demo.models;

import com.tinymission.tinysync.db.DbColumn;
import com.tinymission.tinysync.db.DbModel;
import com.tinymission.tinysync.validation.ValidateNotNull;
import com.tinymission.tinysync.validation.ValidateUniqueness;

public class User extends DbModel {

    @DbColumn
    @ValidateNotNull
    @ValidateUniqueness
    public String name;

}
