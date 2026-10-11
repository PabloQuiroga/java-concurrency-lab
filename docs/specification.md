# Especificación: Simulador de Trading de Alta Frecuencia (HFT)

## 1. Objetivo
Implementar un sistema de simulación de Trading de Alta Frecuencia capaz de procesar múltiples flujos de datos de precios en tiempo real y ejecutar órdenes de compra/venta basadas en reglas predefinidas, asegurando la máxima eficiencia en la concurrencia y la integridad de los datos.

## 2. Requerimientos Funcionales

### 2.1 Gestión de Flujos de Precios (Price Feeds)
- **Ingesta Asíncrona**: El sistema debe ser capaz de recibir actualizaciones de precios de múltiples fuentes simultáneamente sin que una fuente bloquee a las demás.
- **Normalización**: Cada actualización de precio debe contener el identificador del activo (ej: "BTC/USD"), el precio actual y la marca de tiempo (timestamp).

### 2.2 Motor de Decisiones (Trading Engine)
- **Evaluación de Reglas**: El sistema debe evaluar cada actualización de precio frente a un conjunto de reglas activas.
- **Disparo de Órdenes**: Si una regla se cumple (ej: "Precio > X"), el motor debe generar una orden de ejecución inmediatamente.
- **Independencia de Activos**: El procesamiento de un activo no debe interferir ni retrasar la evaluación de otros activos.

### 2.3 Ejecución y Registro (Order Execution)
- **Cola de Órdenes**: Las órdenes generadas deben enviarse a una cola de ejecución para evitar saturar el sistema de salida.
- **Registro de Auditoría**: Cada orden ejecutada debe quedar registrada con su precio de ejecución, activo y hora exacta.

## 3. Requerimientos No Funcionales

### 3.1 Concurrencia y Rendimiento
- **Baja Latencia**: El tiempo transcurrido desde que llega el precio hasta que se genera la orden debe ser mínimo.
- **Escalabilidad**: El sistema debe soportar un incremento en la cantidad de activos monitorizados sin degradar el rendimiento linealmente.
- **Integridad de Datos**: No deben ocurrir condiciones de carrera (*race conditions*) al actualizar el precio de un activo que está siendo leído por el motor de decisiones.

### 3.2 Robustez
- **Aislamiento de Fallos**: El fallo en el procesamiento de un flujo de precios no debe detener la ejecución de los demás flujos.
- **Gestión de Recursos**: El sistema debe evitar la creación excesiva de hilos, utilizando un pool de ejecución optimizado.

## 4. Criterios de Éxito
El sistema se considera exitoso si:
1. Procesa múltiples flujos de precios concurrentes sin bloqueos.
2. Ejecuta órdenes correctamente basadas en las reglas definidas.
3. Mantiene la consistencia de los precios incluso bajo una carga masiva de actualizaciones.
4. Demuestra una gestión eficiente de la memoria y la CPU.

## 5. Resiliencia y Observabilidad
- **Mecanismo de Protección**: El sistema debe implementar un patrón de *Circuit Breaker* para suspender la ejecución de órdenes si se detecta un fallo crítico recurrente en el ejecutor.
- **Medición de Latencia**: El sistema debe calcular y registrar la latencia total desde la creación del evento hasta su ejecución final.
- **Recuperación Automática**: El sistema debe ser capaz de reiniciar la ejecución de órdenes automáticamente tras un periodo de enfriamiento (*cooldown*) después de un fallo.