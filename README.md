# TinySync Android Client Library

TinySync is a set of libraries used to develop cross-platform synchronized database systems.
Each library uses a set of conventions to establish a common synchronization method that works on various client and server platforms.

*NOTE: TinySync is still under active development and only recommended for adventurous souls!*


## Installation

At this point, you have to check out the code from Github and manually add it to your Android application as a library project.
We'll add it to maven once it's more feature complete.


## Architecture

A TinySync project uses a strict client/server architecture.
There is a single server (generally a database-backed web application) that stores the *primary* copy of the system's data.
Each client (native mobile applications or browser applications) each store a copy of a *subset* of the system's data (see Sync Scopes).

Synchronization is initiated by the client and performed using a single HTTP request with a JSON payload (see Sync Requests and Responses).
The synchronization interface is defined by a convention of JSON payload format sent between the client and server.
Individual client libraries exist to perform the synchronization on various client platforms.

All data is assumed to be stored in a table/collection structure inside a database.
Each table or collection maps to an *entity* and is assumed to have the same schema on both client and server.
TinySync provides tools for managing the entity schema and providing code generation mechanisms for native client libraries like this one.


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
```


### Database Context

In order to query or persist model objects, you need to create a database context.
A database context extends *DbContext* and has public fields containing a *DbSet* for each model type:

```java
    public class MyContext extends DbContext {

        public final DbSet<Author> authors = new DbSet<Author>(Author.class);

        public final DbSet<Post> posts = new DbSet<Post>(Post.class);

        public final DbSet<Comment> comments = new DbSet<Comment>(Comment.class);

    }
```

The *DbSet* objects form the foundation of the query and persistence interface.



### Sync Requests and Responses

The basic mechanism of synchronizing data between the client and server in TinySync can be summarized as:

1. The client sends a *sync request* to the server containing all entities that have changed on the client since the last time it synced.
2. The server updates its database based on the updated entities in the *sync request*
3. The server returns a *sync response* that contains all entities that have changed on the server since the *last_synced* value in the *sync request*.
4. The client updates its database based on the updated entities in the *sync response*.

The *sync request* and *sync response* are JSON payloads with the same general form:

```javascript
    {
        "last_synced": "2014-02-14T09:12:43-700",
        "entities": [
            {
                "name": "post",
                "scope": {"author_id": "52212589594cc44541000016"},
                "created": [
                    {
                        "_id": "521fa720594cc48c1d000003",
                        "author_id": "52212589594cc44541000016",
                        "updated_at": "2014-02-17T17:23:54-700",
                        "body": "Some text in a new post..."
                    },
                    ...
                ]
                "updated": [
                    {
                        "_id": "521fa720594cc48c1d000016",
                        "author_id": "52212589594cc44541000016",
                        "updated_at": "2014-02-16T12:05:24-700",
                        "body": "Some new text in an existing post..."
                    },
                    ...
                ]
            },
            ...
        ]
    }
```


### Sync Scopes

When a client sends a *sync request* to the server, each root entity it wants to sync needs a corresponding *sync scope*.
The *sync scope* is a JSON object that defines a query used to limit the updated entities sent back in the *sync response*.

In the example above, the *sync scope* for the *post* entity is `{"author_id": "52212589594cc44541000016"}`.
In this case, the client in question is only interested in storing posts with an author_id of "52212589594cc44541000016".
Only posts matching that query will be returned in the *sync response*.

Clients can leave the *sync scope* null and the server will return all updated values of that entity.
However, it is assumed that for non-trivial system, it will be impractical or impossible to sync the entire server dataset to each client.
Designing an appropriate *sync scope* is the key to having a system that syncs quickly with low client-side memory footprint.

NOTE: The *last_synced* time from the request will be automatically included into the scope to test against *updated_at* in the database.



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