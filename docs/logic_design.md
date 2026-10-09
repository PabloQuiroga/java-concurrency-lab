# Diseño Lógico: Simulador de Trading de Alta Frecuencia

## 1. Modelo de Flujo de Datos
El sistema se basa en una arquitectura de **Pipeline Asíncrono**, dividida en tres etapas desacopladas:

`Ingesta de Datos` $\rightarrow$ `Cola de Eventos` $\rightarrow$ `Motor de Decisiones` $\rightarrow$ `Ejecutor de Órdenes`

### 1.1 Etapa de Ingesta (Productores)
- Múltiples fuentes de datos independientes generan actualizaciones de precios.
- Cada fuente opera de forma asíncrona para evitar que la lentitud de una fuente afectue a las demás.
- La tarea única de esta etapa es capturar la información y enviarla a la cola central.

### 1.2 Cola de Eventos (Buffer de Sincronización)
- Actúa como un amortiguador (*buffer*) entre los productores y el consumidor.
- Implementa la propiedad **Thread-Safe**, permitiendo que múltiples hilos escriban y un hilo lea simultáneamente sin corromper los datos.

### 1.3 Motor de Decisiones (Consumidor)
- Un proceso dedicado consume la cola de eventos la mayor velocidad posible.
- Para cada precio recibido, el motor consulta una tabla de reglas predefinidas.
- Si se cumple una condición, el motor genera una orden de ejecución.

## 2. Lógica de Concurrencia y Sincronización

### 2.1 Gestión de Estado Compartido
Para evitar condiciones de carrera (*race conditions*) al leer y escribir precios, se implementará una estrategia de **Acceso Concurrente Optimizado**:
- El almacenamiento de precios debe permitir lecturas rápidas y escrituras atómicas.
- Se evitará el bloqueo total del sistema (*Global Lock*) para permitir que la lectura de un activo no bloquee la escritura de otro.

### 2.2 Control de Flujo y Presión (Backpressure)
Para evitar que la memoria se sature si la ingesta de datos es más rápida que la capacidad de procesamiento del motor:
- La cola de eventos tendrá una capacidad máxima definida.
- Se definirá una estrategia de desborde (ej: descartar la actualización más antigua para priorizar la más reciente).

## 3. Algoritmo de Ejecución de Órdenes
1. **Captura**: Recibir `(Activo, Precio, Timestamp)`.
2. **Validación**: Verificar si el activo tiene reglas activas.
3. **Comparación**: Evaluar `Precio Actual` vs `Precio Objetivo` de la regla.
4. **Disparo**: Si la condición es verdadera, enviar la orden a la cola de ejecución.
5. **Registro**: Almacenar la orden ejecutada en el historial de auditoría.