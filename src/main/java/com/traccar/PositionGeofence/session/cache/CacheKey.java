package com.traccar.PositionGeofence.session.cache;

import com.traccar.PositionGeofence.modelo.BaseModel;

record CacheKey(Class<? extends BaseModel> clazz, String id) {
    CacheKey(BaseModel object) {
        this(object.getClass(), object.getId());
    }
}
