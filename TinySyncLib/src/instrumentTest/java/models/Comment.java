package models;

import com.tinymission.tinysync.db.DbColumn;
import com.tinymission.tinysync.db.DbModel;
import com.tinymission.tinysync.db.ObjectId;

import org.joda.time.DateTime;

/**
 * Wraps rows of the comment table.
 */
public class Comment extends DbModel {

    @DbColumn()
    public String body;

    @DbColumn()
    public DateTime commentedAt;

    @DbColumn()
    public ObjectId postId;
}
