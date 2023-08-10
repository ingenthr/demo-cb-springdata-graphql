# Running an Example

This is a modified version of the app that originally appeared on the
(Couchbase Blog)[https://www.couchbase.com/blog/processing-graphql-queries-with-java-spring-boot-and-nosql/].
It was updated to the most recent version of dependencies, including porting from SDK 2.x to 3.x.

To run a graphql query against it, do something like this:

```
curl -H "Content-Type: application/json" -d @src/main/resources/graphql-query.json http://localhost:8080/graphql
```

## ToDo
- Look into porting it to use Spring Data Couchbase.
- Clean up some of the trace statements, etc.
- Better description of querying.
- Maybe a frontend that uses this.
