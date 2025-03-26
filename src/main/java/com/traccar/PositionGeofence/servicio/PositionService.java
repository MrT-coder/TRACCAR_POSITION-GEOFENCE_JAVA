// package com.traccar.PositionGeofence.servicio;

// import org.slf4j.Logger;
// import org.slf4j.LoggerFactory;
// import org.springframework.stereotype.Service;

// import com.traccar.PositionGeofence.modelo.Position;

// @Service
// public class PositionService {

//     private static final Logger LOGGER = LoggerFactory.getLogger(PositionService.class);

//     // Inyección de todos los handlers adaptados a Spring (estos deben estar definidos como beans)
//     private final TimeHandler timeHandler;
//     private final HemisphereHandler hemisphereHandler;
//     private final FilterHandler filterHandler;
//     private final DistanceHandler distanceHandler;
//     private final DriverHandler driverHandler;
//     private final EngineHoursHandler engineHoursHandler;
//     private final GeocoderHandler geocoderHandler;
//     private final GeolocationHandler geolocationHandler;
//     private final MotionHandler motionHandler;
//     private final OutdatedHandler outdatedHandler;
//     private final SpeedLimitHandler speedLimitHandler;
//     private final PostProcessHandler postProcessHandler;
//     private final PositionForwardingHandler positionForwardingHandler; // Opcional, si se requiere forwarding

//     public PositionService(TimeHandler timeHandler,
//                            HemisphereHandler hemisphereHandler,
//                            FilterHandler filterHandler,
//                            DistanceHandler distanceHandler,
//                            DriverHandler driverHandler,
//                            EngineHoursHandler engineHoursHandler,
//                            GeocoderHandler geocoderHandler,
//                            GeolocationHandler geolocationHandler,
//                            MotionHandler motionHandler,
//                            OutdatedHandler outdatedHandler,
//                            SpeedLimitHandler speedLimitHandler,
//                            PostProcessHandler postProcessHandler,
//                            PositionForwardingHandler positionForwardingHandler) {
//         this.timeHandler = timeHandler;
//         this.hemisphereHandler = hemisphereHandler;
//         this.filterHandler = filterHandler;
//         this.distanceHandler = distanceHandler;
//         this.driverHandler = driverHandler;
//         this.engineHoursHandler = engineHoursHandler;
//         this.geocoderHandler = geocoderHandler;
//         this.geolocationHandler = geolocationHandler;
//         this.motionHandler = motionHandler;
//         this.outdatedHandler = outdatedHandler;
//         this.speedLimitHandler = speedLimitHandler;
//         this.postProcessHandler = postProcessHandler;
//         this.positionForwardingHandler = positionForwardingHandler;
//     }

//     /**
//      * Procesa la posición aplicando la lógica de cada handler.
//      * Cada handler se invoca de forma secuencial.
//      * 
//      * @param position la posición a procesar
//      * @return la posición procesada
//      */
//     public Position processPosition(Position position) {
//         // Ajusta el tiempo según configuración
//         timeHandler.onPosition(position, processed -> {});
        
//         // Ajusta la latitud/longitud según hemisferio
//         hemisphereHandler.onPosition(position, processed -> {});
        
//         // Realiza filtrado (por ejemplo, datos inválidos, duplicados, etc.)
//         filterHandler.onPosition(position, processed -> {
//             if (processed) {
//                 LOGGER.info("La posición fue filtrada por FilterHandler.");
//                 // Aquí podrías optar por abortar el procesamiento si se filtra.
//             }
//         });
        
//         // Calcula distancias y actualiza la posición con la distancia recorrida
//         distanceHandler.onPosition(position, processed -> {});
        
//         // Asigna el driver asociado (si aplica)
//         driverHandler.onPosition(position, processed -> {});
        
//         // Calcula horas de motor y acumula el valor
//         engineHoursHandler.onPosition(position, processed -> {});
        
//         // Obtiene la dirección mediante geocodificación
//         geocoderHandler.onPosition(position, processed -> {});
        
//         // Si la posición está desactualizada o tiene problemas, se puede actualizar con datos de red
//         geolocationHandler.onPosition(position, processed -> {});
        
//         // Determina si el dispositivo está en movimiento
//         motionHandler.onPosition(position, processed -> {});
        
//         // Si la posición está marcada como desactualizada, se actualiza con la última posición conocida
//         outdatedHandler.onPosition(position, processed -> {});
        
//         // Actualiza el límite de velocidad (este handler utiliza callbacks asíncronos)
//         speedLimitHandler.onPosition(position, processed -> {});
        
//         // Post-procesa la posición: actualiza el dispositivo y cache, etc.
//         postProcessHandler.onPosition(position, processed -> {});
        
//         // Opcional: Reenvía la posición a otros sistemas
//         if (positionForwardingHandler != null) {
//             positionForwardingHandler.onPosition(position, processed -> {});
//         }
        
//         return position;
//     }
// }