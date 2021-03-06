# TinySync Android Client Library

TinySync is a set of libraries used to develop cross-platform synchronized database systems.
Each library uses a set of conventions to establish a common synchronization method that works on various client and server platforms.

*NOTE: TinySync is still under active development and only recommended for adventurous souls!*


## Installation

At this point, you have to check out the code from GitHub and manually add it to your Android application as a library project.
We'll add it to maven once it's more feature complete.


## Usage

This library is used for writing Android client applications that sync to a TinySync server.

TinySync is not meant to be a drop-in solution that makes your application automatically sync data between the server and client.
Implementing a sync solution involves a thorough understanding of the TinySync libraries, as well as the underlying database technologies used on both the server and client.


### Models

The database schema is defined locally using model classes.
These models are used to query and persist changes to the local database, which is then synced to the server at a later time.

Model class definitions should be familiar to anyone who has used a standard object-relational mapper (ORM).
Each class inherits from *DbModel*, which provides common column definitions like the identifier and timestamps.
Fields that are marked with the *@DbColumn* annotation are mapped to database columns.

```java
    public class Author extends DbModel {
        public static enum Seniority {
            junior, senior
        }

        @DbColumn
        @ValidateNotNull
        public String name;

        @DbColumn
        public int age;

        @DbColumn
        public Seniority seniority;

        public final DbHasMany<Post> posts =
            new DbHasMany<Post>(this, Post.class);
    }

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

        public final DbBelongsTo<Author> author =
            new DbBelongsTo<Author>(this, Author.class);

        @DbColumn
        public float averageRating;
    }
```

One-Many relationships are described with the *DbBelongsTo* and *DbHasMany* classes.
By making public fields with these classes in your models, TinySync will handle the querying, caching, and persistence of the relationship for you.


### Database Context

In order to query or persist model objects, you need to create a database context.
A database context extends *DbContext* and has public fields containing a *DbCollection* for each model type:

```java
    public class MyContext extends DbContext {

        public final DbCollection<Author> authors =
            new DbCollection<Author>(Author.class);

        public final DbCollection<Post> posts =
            new DbCollection<Post>(Post.class);

        public final DbCollection<Comment> comments =
            new DbCollection<Comment>(Comment.class);

    }
```

The *DbCollection* objects form the foundation of the query and persistence interface.


### Querying

TinySync supports a 'fluent' query interface on the *DbCollection* class.
Queries are constructed by chaining calls to *where*, *orderBy*, and *include* on a collection:

```java
    DbSet<Author> youngAuthors = context.authors
                                    .where("age.lt", 30)
                                    .orderBy("createdAt")
                                    .run();
```

The results of a query are stored in a *DbSet* object, which will lazily load the resulting rows into model objects as you use them.




## Contributing

1. Fork it
2. Create your feature branch (`git checkout -b my-new-feature`)
3. Commit your changes (`git commit -am 'Add some feature'`)
4. Push to the branch (`git push origin my-new-feature`)
5. Create new Pull Request


## License

The MIT License (MIT)

Copyright (c) 2014 Tiny Mission LLC

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in
all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
THE SOFTWARE.