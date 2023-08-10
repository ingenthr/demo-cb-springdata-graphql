package com.couchbase.example.graphql;

import com.couchbase.client.java.Bucket;
import com.couchbase.client.java.Cluster;
import com.couchbase.client.java.json.JsonObject;
import com.couchbase.client.java.query.QueryResult;
import graphql.schema.DataFetcher;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Database {

    public static DataFetcher getPokemonData(Cluster cluster) {
        return environment -> {
            System.out.println("FETCHING POKEMON DATA...");
            String statement = "SELECT example.* "
                    + "FROM example "
                    + "WHERE type = 'pokemon'";
            QueryResult queryResult = cluster.query(statement);
            return extractResultOrThrow(queryResult);
        };
    }
    public static DataFetcher getGameData(Bucket bucket) {
        return environment -> {
            HashMap<String, Object> parent = environment.getSource();
            JsonObject document;
            if(parent != null) {
                System.out.println("FETCHING GAME DATA FOR " + parent.get("game") + "...");
                document = bucket.defaultCollection().get((String) parent.get("game")).contentAsObject();
            } else {
                System.out.println("FETCHING GAME DATA FOR " + environment.getArgument("id") + "...");
                document = bucket.defaultCollection().get(environment.getArgument("id")).contentAsObject();
            }
            return document.toMap();
        };
    }
    public static DataFetcher getGamesData(Cluster cluster) {
        return environment -> {
            System.out.println("FETCHING GAMES DATA...");
            String statement = "SELECT example.* "
                    + "FROM example "
                    + "WHERE type = 'game'";
            QueryResult queryResult = cluster.query(statement);
            return extractResultOrThrow(queryResult);
        };
    }
    private static List<Map<String, Object>> extractResultOrThrow(QueryResult result) {
        List<Map<String, Object>> content = new ArrayList<>();
        for (JsonObject row : result.rowsAsObject()) {
            content.add(row.toMap());
        }
        return content;
    }
}