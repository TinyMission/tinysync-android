package models;

import android.content.Context;

import com.tinymission.tinysync.db.DbContext;
import com.tinymission.tinysync.db.DbSet;

/**
 * Database context for the test models.
 */
public class MyContext extends DbContext {

    public final DbSet<Author> authors = new DbSet<Author>(Author.class);

    public final DbSet<Post> posts = new DbSet<Post>(Post.class);

    public final DbSet<Comment> comments = new DbSet<Comment>(Comment.class);

    public MyContext(Context androidContext) {
        super(androidContext);
    }
}
