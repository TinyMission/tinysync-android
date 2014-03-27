package models;

import com.tinymission.tinysync.db.DbColumn;
import com.tinymission.tinysync.db.DbModel;
import com.tinymission.tinysync.validation.ValidateNotNull;

/**
 * Wraps a row in the authors table.
 */
public class Author extends DbModel {

    public enum Seniority {
        junior, senior
    }

    @DbColumn()
    @ValidateNotNull
    public String name;

    @DbColumn()
    public int age;

    @DbColumn()
    public Seniority seniority;
}
