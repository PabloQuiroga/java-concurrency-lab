# Plan de Implementación: Simulador de Trading de Alta Frecuencia

## 1. Tecnologías y Herramientas
- **Lenguaje**: Java 17+
- **Gestión de Concurrencia**: `java.util.concurrent`
- **Sistema de Construcción**: Gradle / Maven
- **CI/CD**: GitHub Actions
- **Validación**: JUnit 5 y pruebas de estrés mediante hilos concurrentes.

## 2. Arquitectura Técnica (Mapeo de Componentes)

### 2.1 Componentes de Ingesta (Productores)
- **`PriceFeed`**: Una clase que simula la llegada de datos mediante la ejecución de tareas en un `ScheduledExecutorService`.
- **Generación de Datos**: Uso de `ThreadLocalRandom` para generar fluctuaciones de precios realistas y eficientes.

### 2.2 Buffer de Sincronización (Cola)
- **`BlockingQueue`**: Implementaremos la cola de eventos utilizando `LinkedBlockingQueue`.
    - *Justificación*: Proporciona un mecanismo nativo de bloqueo que suspende el hilo consumidor cuando la cola está vacía y el productor cuando la cola está llena (Backpressure).

### 2.3 Motor de Decisiones (Consumidor)
- **`TradingEngine`**: Un componente que corre en un hilo dedicado.
- **Almacenamiento de Precios**: Uso de `ConcurrentHashMap<String, Double>` para almacenar el último precio de cada activo.
    - *Justificación*: Permite lecturas y escrituras concurrentes sin bloquear la tabla completa.
- **Procesamiento Asíncrono**: Uso de `CompletableFuture` para disparar las órdenes de ejecución sin bloquear el procesamiento del siguiente precio.

### 2.4 Ejecutor de Órdenes Resiliente
- **`OrderExecutor`**: Implementa un pipeline asíncrono mediante `CompletableFuture.runAsync()`.
- **Patrón Circuit Breaker**: Incluye un contador de errores (`AtomicInteger`). Si se alcanza el umbral de fallos, el sistema entra en modo de recuperación, bloqueando temporalmente nuevas órdenes para evitar la saturación del sistema.
- **Métricas de Latencia**: Cálculo de la diferencia temporal entre el `timestamp` del evento y la ejecución final utilizando `java.time.Duration`.
- **Manejo de Errores Asíncronos**: Uso de `.exceptionally()` para capturar y registrar fallos en la ejecución de la orden sin afectar la estabilidad del hilo del motor.

## 3. Estrategia de Sincronización y Seguridad
- **Atómicos**: Uso de `AtomicLong` para contar el número total de órdenes ejecutadas sin necesidad de mutexes costosos.
- **Visibilidad de Memoria**: Uso de la palabra clave `volatile` o estructuras concurrentes para asegurar que el motor de decisiones vea la versión más reciente del precio.

## 4. Plan de Validación Técnica
- **Pruebas de Carga**: Inyectar 100,000 actualizaciones de precios en menos de un segundo para verificar la estabilidad del `BlockingQueue`.
- **Detección de Race Conditions**: Ejecutar el simulador con múltiples hilos y verificar que el conteo final de órdenes sea exacto.
- **Análisis de Latencia**: Medir el tiempo transcurrido desde la llegada del dato hasta la ejecución de la orden.