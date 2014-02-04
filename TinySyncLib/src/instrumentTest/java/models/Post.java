package models;

import com.tinymission.tinysync.db.DbColumn;
import com.tinymission.tinysync.db.DbModel;
import com.tinymission.tinysync.db.ObjectId;

import org.joda.time.DateTime;

/**
 * Wraps rows of the post table.
 */
public class Post extends DbModel {

    @DbColumn()
    public String title;

    @DbColumn()
    public DateTime postedAt;

    @DbColumn()
    public String body;

    @DbColumn()
    public int points;

    @DbColumn()
    public ObjectId authorId;


}
