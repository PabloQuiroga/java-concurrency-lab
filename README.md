# Java Concurrency Lab: Simulador de Trading de Alta Frecuencia

## 🚀 Descripción General
Este proyecto es una demostración técnica de patrones avanzados de concurrencia en Java. Implementa un **Simulador de Trading de Alta Frecuencia (HFT)** diseñado para procesar múltiples flujos de precios en tiempo real y ejecutar órdenes basadas en reglas predefinidas, asegurando la máxima eficiencia y la seguridad de los hilos (*thread-safety*).

## 🎯 Objetivos de Ingeniería
El objetivo es trascender el multihilo básico e implementar patrones de concurrencia de grado industrial:
- **Ingesta de Datos Asíncrona**: Uso de una arquitectura Productor-Consumidor para manejar múltiples flujos de precios concurrentes.
- **Estructuras Lock-Free / Baja Contención**: Aprovechamiento de `ConcurrentHashMap` y variables `Atomic` para minimizar la contención entre hilos.
- **Optimización de Recursos**: Uso de `ExecutorService` y `ThreadPools` para gestionar los costos de ejecución y evitar el agotamiento de hilos.
- **Pipelines No Bloqueantes**: Implementación de flujos asíncronos con `CompletableFuture` para desacoplar la ingesta de precios de la ejecución de órdenes.

## 🛠 Stack Técnico
- **Lenguaje**: Java 17+
- **Herramientas de Concurrencia**: `java.util.concurrent` (ExecutorService, BlockingQueue, ConcurrentHashMap, etc.)
- **Sistema de Construcción**: Gradle / Maven
- **CI/CD**: GitHub Actions
- **Validación**: JUnit 5 y Pruebas de Estrés (Stress Testing)

## 📖 Documentación del Proyecto
Este proyecto sigue un proceso estricto de **Desarrollo Basado en Especificaciones (SDD)**. Todos los detalles técnicos se encuentran en la carpeta `/docs`:
- [Especificación](docs/specification.md): Requerimientos funcionales y no funcionales (Agnóstica al lenguaje).
- [Diseño Lógico](docs/logic_design.md): Flujo algorítmico y modelo de concurrencia.
- [Plan de Implementación](docs/implementation_plan.md): Traducción técnica a Java.
- [Decisiones de Arquitectura](docs/decisions/): Registro de decisiones técnicas críticas (ADRs).

## 🚀 Inicio Rápido
*(Se actualizará tras la fase de implementación)*
1. Clonar el repositorio.
2. Ejecutar los tests usando `./gradlew test`.
3. Ejecutar el simulador usando `./gradlew run`.

## 🛡 Aseguramiento de Calidad
El proyecto es validado a través de:
1. **CI Automatizada**: Cada push es compilado y testeado vía GitHub Actions.
2. **Pruebas de Concurrencia**: Validación de condiciones de carrera (*race conditions*) y bloqueos mutuos (*deadlocks*) bajo alta carga.
3. **Perfilado de Memoria**: Análisis del uso del Heap y detección de fugas de hilos.