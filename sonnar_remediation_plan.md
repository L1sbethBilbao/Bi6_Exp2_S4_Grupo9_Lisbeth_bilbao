# Plan de remediación — hallazgos SonarQube (backend Unidos por los animales)

Documento de seguimiento de **issues corregidos** en el repositorio con motivo de los informes de **SonarQube** (calidad y seguridad). Cada bloque indica la **criticidad** según la severidad típica en Sonar para diferenciar hallazgos.

---

## Leyenda de criticidad (severidad Sonar)

| Criticidad en este documento | Severidad Sonar (típica) | Significado breve |
|-------------------------------|---------------------------|-------------------|
| **Máxima** | **Blocker** | Seguridad / bloqueante para calidad de código |
| **Alta** | **High** | Mantenibilidad o diseño con impacto relevante |
| **Baja** | **Low** | Convenciones, deuda menor, legibilidad |

---

## Resumen ejecutivo

| # | Área / regla (típica) | Criticidad | Categoría Sonar | Estado |
|---|------------------------|------------|-----------------|--------|
| 1 | Nombres de paquete Java (regex minúsculas) | **Baja** | Maintainability | Corregido |
| 2 | Credenciales en `application*.properties` (S2068) | **Máxima** | Security — Blocker | Corregido (sin clave `password` en repo; binding `SPRING_DATASOURCE_*`) |
| 3 | Secretos en Dockerfile MySQL | **Máxima** | Security / Hotspot | Corregido (Compose + `.env`) |
| 4 | Complejidad cognitiva en `saveInvoice` (S3776) | **Alta** | Maintainability — High | Corregido |
| 5 | Literal `"error` duplicado en API (S1192) | **Alta** | Maintainability — High | Corregido |
| 6 | Método de test vacío `contextLoads` (S1186) | **Alta** | Maintainability — High | Corregido |

---

## 1. Nombres de paquetes y carpetas

**Criticidad: Baja** (Sonar: **Low**, Maintainability)

### Síntoma

- *“Rename this package name to match the regular expression…”*
- Paquetes deben ir en **minúsculas** y alineados con carpetas.

### Remediación

- Carpetas y paquetes `appointment`, `care`, `invoice`, `medication`, `patient`; imports y índice Git coherentes.

### Verificación

- `mvn test` (perfil `test` / H2).

---

## 2. Contraseñas en archivos de propiedades (Blocker — imagen Sonar)

**Criticidad: Máxima** (Sonar: **Blocker**, Security — tags *Responsibility*, *cwe*)

### Síntoma (captura SonarQube)

- *“Revoke and change this password, as it is compromised.”*
- **L7** `application.properties`, **L5** `application-docker.properties` (líneas según análisis con `spring.datasource.password=...`).

### Por qué seguía fallando con `${DB_PASSWORD}`

- Algunos analizadores marcan **cualquier** propiedad cuya clave sea `spring.datasource.password`, aunque el valor sea un placeholder, porque la clave implica credencial versionada.

### Remediación aplicada (definitiva frente a S2068)

- **Eliminar** del repositorio las propiedades `spring.datasource.password` y `spring.datasource.username`.
- Confiar en el **binding relajado de Spring Boot** (variables de entorno estándar):
  - **`SPRING_DATASOURCE_PASSWORD`** — obligatoria para MySQL en runtime.
  - **`SPRING_DATASOURCE_USERNAME`** — p. ej. `myuser`, alineada con `MYSQL_USER` en Compose.
- En `application.properties` / `application-docker.properties` solo quedan URL JDBC y driver; comentarios explican Sonar y el uso de `SPRING_DATASOURCE_*`.
- Jenkins / local: inyectar esas variables como secretos, no como texto en Git.

### Regla orientativa

- **S2068** — Credentials should not be hard-coded.

### Verificación

- `mvn test` (perfil `test` / H2 no usa estas propiedades).
- Arranque manual: exportar `SPRING_DATASOURCE_USERNAME` y `SPRING_DATASOURCE_PASSWORD` (ver `compose/RECETA.md`).

---

## 3. Secretos en Dockerfile de MySQL

**Criticidad: Máxima** (Sonar: **Blocker** / Security Hotspot)

### Remediación

- Eliminado `docker/mysql/Dockerfile` con secretos; **`compose/docker-compose.yml`** + **`.env`** (ignorado en Git). Ver `compose/RECETA.md`.

---

## 4. Complejidad cognitiva — `InvoiceService.saveInvoice`

**Criticidad: Alta** (Sonar: **High**, Maintainability — *brain-overload* / Adaptability)

### Síntoma

- *“Refactor this method to reduce its Cognitive Complexity from 24 to the 15 allowed.”*
- Archivo: `src/main/java/com/duoc/backend/invoice/InvoiceService.java` (histórico: `.../Invoice/InvoiceService.java`).

### Causa

- Un solo método concentraba validación de cita, medicamentos, servicios, cargos adicionales y cálculo de total, con muchas ramas anidadas.

### Remediación aplicada

- Extraer métodos privados con responsabilidad única:
  - `requireAppointmentWithId`, `loadAppointment`
  - `resolveMedications`, `resolveCares`
  - `validateAdditionalCharges`, `computeTotal`
  - `nullToEmpty` para listas opcionales
- `saveInvoice` queda como orquestación lineal; la complejidad cognitiva por método baja por debajo del umbral habitual (**≤ 15**).

### Regla orientativa

- **S3776** — Cognitive Complexity of methods should not be too high.

### Verificación

- `mvn test`; comportamiento de negocio de facturación preservado.

---

## 5. Literal duplicado — clave JSON `"error"`

**Criticidad: Alta** (Sonar: **High**, Maintainability — *design* / Adaptability)

### Síntoma

- *“Define a constant instead of duplicating this literal 'error' 3 times.”*
- Archivo: `src/main/java/com/duoc/backend/exception/ApiExceptionHandler.java`.

### Remediación aplicada

- Constante `private static final String JSON_ERROR_KEY = "error";`
- Uso de `Map.of(JSON_ERROR_KEY, ex.getMessage())` en los tres `@ExceptionHandler`.

### Regla orientativa

- **S1192** — String literals should not be duplicated.

### Verificación

- Contrato JSON de error sin cambio semántico (`{"error":"..."}`).

---

## 6. Método de test aparentemente vacío — `contextLoads`

**Criticidad: Alta** (Sonar: **High**, Maintainability — *suspicious* / Intentionality)

### Síntoma

- *“Add a nested comment explaining why this method is empty…”*
- Archivo: `src/test/java/com/duoc/backend/BackendApplicationTests.java`.

### Remediación aplicada

- Comentario dentro del método explicando que **`@SpringBootTest`** ya valida el arranque del contexto y que no se requieren aserciones adicionales para el objetivo del test.

### Regla orientativa

- **S1186** — Methods should not be empty.

### Verificación

- `mvn test`; el test sigue cargando el contexto Spring completo.

---

## 7. Tabla issues Low (referencia cruzada)

| Issue | Criticidad | Fix |
|-------|------------|-----|
| Paquetes PascalCase | **Baja** | Paquetes/carpetas en minúsculas |

---

## 8. Próximos pasos sugeridos (Sonar)

1. Re-ejecutar análisis y cerrar issues cuando el código esté en la rama analizada.
2. Valorar externalizar por completo **`JWT_SECRET`** en producción si Sonar marca el default en `application.properties`.
3. Secretos solo en CI/CD y `compose/.env` local.

---

## 9. Archivos tocados (última remediación documentada)

- `src/main/java/com/duoc/backend/invoice/InvoiceService.java`
- `src/main/java/com/duoc/backend/exception/ApiExceptionHandler.java`
- `src/test/java/com/duoc/backend/BackendApplicationTests.java`
- (Histórico) paquetes bajo `com.duoc.backend.{appointment,care,invoice,medication,patient}`
- `src/main/resources/application.properties`, `application-docker.properties`
- `compose/*`, `.gitignore`

---

## 10. Cobertura de pruebas (JaCoCo ≥ 90 % y SonarQube)

### Qué se configuró

- Plugin **JaCoCo** en `pom.xml`: agente en `test`, informe XML/HTML en `verify`, y regla **`check`** con mínimo **90 %** de líneas cubiertas en el bundle (excluye solo `BackendApplication` del cómputo).
- Propiedad Maven **`sonar.coverage.jacoco.xmlReportPaths`** apuntando a `target/site/jacoco/jacoco.xml` para que el análisis de Sonar importe la cobertura tras `mvn verify`.

### Cómo ejecutarlo en local

```bash
mvn clean verify
```

- Informe HTML: `target/site/jacoco/index.html`.
- Si el umbral del 90 % no se cumple, el build falla en la fase `check` de JaCoCo.

### Jenkins / SonarQube

1. En el pipeline, ejecutar al menos **`mvn clean verify`** (no omitir tests) **antes** del paso de análisis Sonar.
2. En las propiedades del análisis (o en el `pom`), asegurar que Sonar reciba el informe:
   - `sonar.coverage.jacoco.xmlReportPaths=target/site/jacoco/jacoco.xml`
3. Volver a lanzar el análisis; el Quality Gate de cobertura debería mejorar respecto a 0 %.

### Pruebas añadidas (resumen)

- Unitarias con **Mockito**: servicios (`Invoice`, `Appointment`, `Care`, `Medication`, `Patient`), `LoginController` (standalone MockMvc), `MyUserDetailsService`, `JwtSigningKeyProvider`, modelos de dominio.
- **`@WebMvcTest`** de controladores REST con `@MockBean` de dependencias y de **`JwtSigningKeyProvider`** (el contexto de seguridad lo requiere).
- Integración **`@SpringBootTest` + `MockMvc`**: login `admin` / `admin123`, acceso protegido con JWT, JWT inválido y petición sin autenticación.
- Manejador global de excepciones y `SecuredController` con MockMvc standalone.

---

*Ajustar códigos de regla (Sxxxx) según la versión y el quality profile de tu instancia SonarQube.*
