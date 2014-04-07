package models;

import com.tinymission.tinysync.db.DbColumn;
import com.tinymission.tinysync.db.DbHasMany;
import com.tinymission.tinysync.db.DbModel;
import com.tinymission.tinysync.validation.ValidateNotNull;

import java.util.List;

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

    public final DbHasMany<Post> posts = new DbHasMany<Post>(this, Post.class);
}
