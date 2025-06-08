
## Descripción del Repositorio

Este repositorio contiene el desarrollo del **microservicio de procesamiento de posiciones y gestión de geocercas (geofencing)** realizado como parte del proyecto de tesis titulado *"Construcción de un Prototipo de Arquitectura Modular Basada en Microservicios para la Optimización de Sistemas de Monitoreo GPS"*.

El objetivo principal del microservicio es **procesar datos de posición GPS en tiempo real**, aplicar lógica de decodificación y codificación, y gestionar eventos relacionados con **geocercas (GeoFences)**. Este componente fue desarrollado utilizando **Spring Boot** en lenguaje **Java**, con el fin de desacoplar funcionalidades críticas del sistema monolítico original **Traccar** y migrar progresivamente hacia una arquitectura basada en microservicios (https://github.com/traccar/traccar).

---

##  Objetivos del Microservicio

- **Procesamiento eficiente de datos de posición GPS**
- **Gestión de eventos asociados a geocercas (Geofencing)**
- **Desacoplamiento modular del sistema monolítico Traccar**
- **Servir como base para futuras implementaciones de microservicios similares**

---

## Arquitectura y Tecnologías Utilizadas

- **Lenguaje:** Java
- **Framework:** Spring Boot
- **Base de Datos:** MongoDB
- **Comunicación:** REST API
- **Colas de mensajes:** RabbitMQ
- **Contenerización:** Docker

---
## Clonar

1. Clona el repositorio:
   ```bash
   git clone https://github.com/MrT-coder/TRACCAR_POSITION-GEOFENCE_JAVA.git
   ```

2. Navega al directorio:
   ```bash
   cd TRACCAR_POSITION-GEOFENCE_JAVA
   ```
---

## Endpoints Principales

| Método | Ruta                     | Descripción                              |
|--------|--------------------------|------------------------------------------|
| GET    | `/api/positions`         | Lista todas las posiciones               |
| GET    | `/api/positions/{id}`    | Obtiene una posición por ID              |
| POST   | `/api/positions`         | Crea una nueva posición                  |
| PUT    | `/api/positions/{id}`    | Actualiza una posición existente         |
| DELETE | `/api/positions/{id}`    | Elimina una posición                     |
| GET    | `/api/geofences`         | Lista todas las geocercas                |
| POST   | `/api/geofences`         | Crea una nueva geocerca                  |

---

##  Referencia al Proyecto Principal

Este repositorio forma parte de un conjunto más amplio de microservicios desarrollados durante la tesis mencionada. Otros repositorios relacionados:

- [Microservicio de Eventos en Java](https://github.com/MrT-coder/TRACCAR_EVENTS_JAVA)
- [Microservicio de Posición en C/C++](https://github.com/MrT-coder/POSITION_GEOFENCE_C-)

---
## Estado del Proyecto

Este proyecto se encuentra en estado **experimental y básico**, aún con fallos, desarrollado principalmente como parte de una investigación académica. Es útil como punto de partida para proyectos futuros relacionados con sistemas de monitoreo GPS basados en microservicios.

---

## Contribución

Si deseas contribuir al proyecto, siéntete libre de abrir un *issue* o enviar un *pull request*. ¡Cualquier ayuda es bienvenida!

---
