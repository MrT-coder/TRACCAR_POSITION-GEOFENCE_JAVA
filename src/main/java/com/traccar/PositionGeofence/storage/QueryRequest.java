package com.traccar.PositionGeofence.storage;

import org.springframework.data.mongodb.core.query.Query;

public class QueryRequest {
    private final Query query;

    public QueryRequest(Query query) {
        this.query = query;
    }

    public Query getQuery() {
        return query;
    }
}