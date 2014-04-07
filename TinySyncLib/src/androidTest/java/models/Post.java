package models;

import com.tinymission.tinysync.db.DbBelongsTo;
import com.tinymission.tinysync.db.DbColumn;
import com.tinymission.tinysync.db.DbModel;
import com.tinymission.tinysync.db.ObjectId;
import com.tinymission.tinysync.validation.ValidateNotNull;

import org.joda.time.DateTime;

/**
 * Wraps rows of the post table.
 */
public class Post extends DbModel {

    @DbColumn
    @ValidateNotNull
    public String title;

    @DbColumn
    public DateTime postedAt;

    @DbColumn
    public String body;

    @DbColumn
    public int points;

    public final DbBelongsTo<Author> author = new DbBelongsTo<Author>(this, Author.class);

    @DbColumn
    public float averageRating;

}
