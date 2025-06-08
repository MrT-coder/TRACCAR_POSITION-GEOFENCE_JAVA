// package com.traccar.PositionGeofence.helper;

// import com.fasterxml.jackson.databind.ObjectMapper;

// import jakarta.inject.Inject;
// import jakarta.ws.rs.ext.ContextResolver;

// // This does not work as a lambda
// public class ObjectMapperContextResolver implements ContextResolver<ObjectMapper> {

//     private final ObjectMapper objectMapper;

//     @Inject
//     public ObjectMapperContextResolver(ObjectMapper objectMapper) {
//         this.objectMapper = objectMapper;
//     }

//     @Override
//     public ObjectMapper getContext(Class<?> clazz) {
//         return objectMapper;
//     }

// }