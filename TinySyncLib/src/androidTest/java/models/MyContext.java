package models;

import android.content.Context;

import com.tinymission.tinysync.db.DbCollection;
import com.tinymission.tinysync.db.DbContext;

/**
 * Database context for the test models.
 */
public class MyContext extends DbContext {

    public final DbCollection<Author> authors = new DbCollection<Author>(Author.class);

    public final DbCollection<Post> posts = new DbCollection<Post>(Post.class);

    public final DbCollection<Comment> comments = new DbCollection<Comment>(Comment.class);

    public MyContext(Context androidContext) {
        super(androidContext);
    }
}
