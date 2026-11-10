# Salud Ágil — prototipo

Plataforma web para consultar la disponibilidad de medicamentos y seguir la dispensación en la Nueva EPS de Armenia (piloto: puntos norte, centro y sur).

Proyecto del curso **Formulación de Proyectos**, Ingeniería de Sistemas y Computación, Universidad del Quindío. Integrantes: David Gómez Ramírez, Emili García Bermúdez, David Felipe Pedraza y Luis Carlos Calderón Calvo. Profesor: Alejandro Urrea Ospina.

> Todos los datos (afiliados, documentos, teléfonos, direcciones, inventarios) son **ficticios** y se cargan al iniciar.

---

## Qué hace (relación con la propuesta)

| Funcionalidad de la propuesta | Dónde está en el prototipo |
|---|---|
| Consultar disponibilidad en tiempo real por punto, con fecha y hora de la última actualización | *Soy afiliado → ¿Está mi medicamento?* · `GET /api/medicamentos/{id}/disponibilidad` |
| Seguir el estado de la fórmula: pendiente, en tránsito, disponible, entregada | *Soy afiliado → Mis fórmulas y turnos* · `GET /api/afiliados/{documento}` |
| Notificaciones por SMS, WhatsApp o correo cuando el medicamento llega o cambia de estado | `NotificacionService` + `EnviadorNotificaciones` (simulado: escribe en el log y queda en la tabla `notificacion`; el afiliado los ve en *Mis avisos*) |
| Entregas parciales y medicamentos pendientes | `FormulaService.dispensar(...)` entrega lo que haya y deja la fórmula PENDIENTE con lo que falta |
| Agendar turno para distribuir la demanda durante el día | Franjas de 30 min con cupo de 4 personas (configurable) · `/api/turnos` |
| Tablero de indicadores para la EPS y el gestor farmacéutico | *Indicadores EPS* · `GET /api/indicadores` |
| Personal de farmacia actualiza la disponibilidad | *Farmacia → Inventario / Fórmulas / Turnos / Radicar fórmula* |

### Ciclo de vida de una fórmula

```
radicar ──► PENDIENTE ──(farmacia la despacha)──► EN_TRANSITO
               │  ▲                                   │
   llegan las  │  │ se agotan antes                   │ llegan las
   existencias ▼  │ de la entrega                     ▼ existencias
             DISPONIBLE ◄─────────────────────────────┘
               │
   dispensar   ├── completa ──► ENTREGADA ──► encuesta de satisfacción
               └── parcial  ──► PENDIENTE (con lo que falta)
```

Cada vez que la farmacia guarda una cantidad o registra un conteo físico, el sistema revisa las fórmulas abiertas de ese punto que necesitan ese medicamento, cambia su estado y avisa al afiliado.

### Indicadores (y su meta, configurable en `application.properties`)

| Indicador | Cálculo | Meta por defecto |
|---|---|---|
| Tiempo promedio de dispensación | Horas entre radicación y entrega completa | ≤ 24 h |
| Entregas completas | % de entregas en ventanilla con las que la fórmula quedó completa | ≥ 90 % |
| Exactitud del inventario | % de conteos físicos que coinciden con el sistema | ≥ 95 % |
| Satisfacción | Promedio de la encuesta (1 a 5) | ≥ 4,0 |

Los datos de demostración incluyen 45 fórmulas históricas de los últimos 30 días. El punto sur se cargó con más faltantes a propósito, para que el tablero muestre cómo se ve un punto que no cumple los acuerdos de nivel de servicio.

---

## Arquitectura

```
frontend/  React 18 + Vite        →  vistas: afiliado, farmacia, indicadores
backend/   Java 21 + Spring Boot 3.5 (Web, Data JPA, Validation)
           H2 en memoria (desarrollo) · MariaDB (perfil "mariadb")
docker-compose.yml  →  MariaDB + backend + frontend (nginx)
```

Paquetes del backend (`co.edu.uniquindio.saludagil`):

- `domain` — entidades JPA: `PuntoEntrega`, `Medicamento`, `Inventario`, `Afiliado`, `Formula`, `FormulaItem`, `Dispensacion`, `Turno`, `Notificacion`, `ConteoFisico`, `Encuesta`.
- `repository` — interfaces Spring Data.
- `service` — reglas de negocio (`FormulaService`, `InventarioService`, `TurnoService`, `IndicadorService`, `NotificacionService`).
- `web` — controladores REST y manejo uniforme de errores (`{ estado, mensaje, fecha }`).
- `dto` — records de entrada y salida.
- `config` — propiedades de negocio, CORS y carga de datos de demostración.

El envío real de mensajes se deja detrás de la interfaz `EnviadorNotificaciones`: para pasar a producción basta con agregar una implementación por proveedor (p. ej. Twilio para SMS, WhatsApp Cloud API, SMTP para correo) sin tocar los servicios.

---

## Cómo ejecutarlo

### Opción 1: desarrollo (sin Docker)

Requisitos: JDK 21, Maven 3.9+, Node 18+.

```bash
# Terminal 1 — API en http://localhost:8080 (base H2 en memoria)
cd backend
mvn spring-boot:run

# Terminal 2 — interfaz en http://localhost:5173
cd frontend
npm install
npm run dev
```

La consola de H2 queda en http://localhost:8080/h2-console (JDBC URL `jdbc:h2:mem:saludagil`, usuario `sa`, sin clave).

### Opción 2: todo con Docker y MariaDB

```bash
docker compose up --build
```

Abrir http://localhost:3000. La API queda también en http://localhost:8080.

### Pruebas

```bash
cd backend
mvn test
```

`FlujoDispensacionTest` recorre el flujo completo: consulta por punto, llegada de inventario que habilita una fórmula y notifica, turno y entrega completa, entrega parcial, cupo por franja y cálculo de indicadores.

---

## Guion de demostración (5 minutos)

1. **Afiliado → ¿Está mi medicamento?** Buscar *insulina*: agotada en el norte y con pocas unidades en el sur. Se ve la hora de la última actualización de cada punto.
2. **Afiliado → Mis fórmulas**, documento `1094000001` (María Elena). Tiene una fórmula lista y otra pendiente por insulina. Agendar turno para la lista.
3. **Farmacia → Punto Norte → Inventario.** Poner 10 en *Insulina glargina* y guardar.
4. Volver a María Elena: la fórmula pendiente pasó a **Lista para reclamar** y en *Mis avisos* aparece el mensaje de llegada.
5. **Farmacia → Turnos**: entregar el turno agendado. La fórmula queda entregada y María puede calificar la atención.
6. **Farmacia → Punto Sur → Fórmulas**: la de Luz Dary (`1094000003`) tiene entrega parcial; falta el inhalador.
7. **Indicadores EPS**: comparar los tres puntos. El sur no cumple las metas.

Otros documentos de demostración: `1094000002` (José: fórmula en camino y una entregada por calificar), `1094000004` (Carlos: turno ya agendado).

---

## API (resumen)

| Método | Ruta | Uso |
|---|---|---|
| GET | `/api/puntos` | Puntos de entrega |
| GET | `/api/medicamentos?q=` | Buscar medicamentos |
| GET | `/api/medicamentos/{id}/disponibilidad` | Existencias por punto |
| GET | `/api/afiliados/{documento}` | Fórmulas y avisos del afiliado |
| GET | `/api/formulas/{codigo}` | Detalle de una fórmula |
| POST | `/api/formulas/{codigo}/encuesta` | Calificar `{ calificacion, comentario }` |
| GET | `/api/turnos/franjas?puntoId=&fecha=` | Franjas y cupos del día |
| POST | `/api/turnos` | Agendar `{ formulaCodigo, fecha, hora }` |
| DELETE | `/api/turnos/{codigo}` | Cancelar turno |
| GET | `/api/farmacia/puntos/{id}/inventario` | Inventario del punto |
| PUT | `/api/farmacia/inventario` | Fijar cantidad `{ puntoId, medicamentoId, cantidad }` |
| POST | `/api/farmacia/conteos` | Conteo físico `{ puntoId, medicamentoId, cantidadFisica }` |
| GET | `/api/farmacia/puntos/{id}/formulas` | Fórmulas del punto |
| POST | `/api/farmacia/formulas` | Radicar `{ documento, puntoId, items: [{ medicamentoId, cantidad }] }` |
| POST | `/api/farmacia/formulas/{codigo}/en-transito` | Marcar en camino |
| POST | `/api/farmacia/formulas/{codigo}/dispensar` | Entregar lo disponible |
| GET | `/api/farmacia/puntos/{id}/turnos?fecha=` | Agenda del día |
| GET | `/api/indicadores` | Tablero (red y por punto) |

---

## Limitaciones del prototipo y siguientes pasos

- **Sin autenticación.** En producción: Spring Security con roles AFILIADO, FARMACIA y EPS, y validación de identidad del afiliado (no basta con el número de documento).
- **Datos personales y de salud.** Antes de usar datos reales hay que cumplir la Ley 1581 de 2012 (habeas data): autorización del titular, política de tratamiento y cifrado.
- **Notificaciones simuladas.** Falta conectar proveedores reales de SMS, WhatsApp y correo.
- **Inventario cargado a mano.** La integración por API con los sistemas de las farmacias (propuesta a futuro) reemplazaría la pestaña de inventario.
- **Sin reserva de existencias.** Dos fórmulas pueden quedar "disponibles" con las mismas unidades. Si se agotan antes de la entrega, la fórmula vuelve a pendiente y se avisa al afiliado. Una mejora sería apartar las unidades al agendar el turno.
- **Indicadores calculados en memoria.** Suficiente para tres puntos; con más volumen convendría usar consultas agregadas.
