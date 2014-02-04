package models;

import com.tinymission.tinysync.db.DbColumn;
import com.tinymission.tinysync.db.DbModel;

/**
 * Wraps a row in the authors table.
 */
public class Author extends DbModel {

    public enum Seniority {
        junior, senior
    }

    @DbColumn()
    public String name;

    @DbColumn()
    public int age;

    @DbColumn()
    public Seniority seniority;
}
