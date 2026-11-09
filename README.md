# Directorio de funcionarios

Taller de Spring Boot del Bootcamp de Desarrollo Web Backend Java (Campuslands).

Directorio web para que el equipo administrativo registre funcionarios, consulte sus datos, los corrija y retire a quienes ya no trabajan en la organización. Una sola aplicación Spring Boot guarda los datos en MySQL, expone una API REST y sirve la página que consume esa API.

## Tecnologías

- Java 21
- Spring Boot 4 con el patrón MVC (Spring Web MVC, Spring Data JPA, Bean Validation)
- MySQL 8
- HTML, CSS y JavaScript vanilla (sin frameworks de frontend)
- Maven (incluye el Maven Wrapper, no hace falta instalar Maven)

## Requisitos

- JDK 21
- MySQL en ejecución en `localhost:3306`
- Visual Studio Code con el *Extension Pack for Java* (opcional, para ejecutar desde el editor)

## Configuración de la base de datos

La aplicación crea por sí sola la base de datos `directorio_funcionarios` y la tabla `funcionarios` al arrancar. Solo necesita un usuario de MySQL con permisos para crearlas.

Los datos de conexión se leen de variables de entorno, para no publicar contraseñas en el repositorio:

| Variable | Valor por defecto | Descripción |
|---|---|---|
| `DB_HOST` | `localhost` | Servidor de MySQL |
| `DB_PORT` | `3306` | Puerto de MySQL |
| `DB_NAME` | `directorio_funcionarios` | Nombre de la base de datos |
| `DB_USER` | `root` | Usuario |
| `DB_PASSWORD` | *(vacía)* | Contraseña |
| `PORT` | `8080` | Puerto de la aplicación web |

## Cómo ejecutar

En la terminal de VS Code (PowerShell), desde la carpeta del proyecto:

```powershell
$env:DB_PASSWORD = "tu_contraseña_de_mysql"
.\mvnw.cmd spring-boot:run
```

En Linux o macOS:

```bash
DB_PASSWORD="tu_contraseña_de_mysql" ./mvnw spring-boot:run
```

Luego abre <http://localhost:8080> en el navegador.

Los datos quedan guardados en MySQL: al detener y volver a iniciar la aplicación siguen disponibles.

## API REST

Ruta base: `/api/v1/funcionarios`. Los datos viajan en JSON. Una respuesta 204 no lleva cuerpo.

| Operación | Método y ruta | Respuesta |
|---|---|---|
| Listar | `GET /api/v1/funcionarios` | 200 con la lista. 204 si el directorio está vacío. |
| Consultar por ID | `GET /api/v1/funcionarios/{id}` | 200 con el funcionario. 404 si el ID no existe. |
| Registrar | `POST /api/v1/funcionarios` | 201 con el funcionario creado y su ID. |
| Actualizar | `PUT /api/v1/funcionarios/{id}` | 200 con los datos actualizados. 404 si el ID no existe. |
| Eliminar | `DELETE /api/v1/funcionarios/{id}` | 204 al eliminar. 404 si el ID no existe. |
| Buscar por nombre | `GET /api/v1/funcionarios?nombre=ana` | 200 con las coincidencias. 204 si no hay resultados. |
| Filtrar por departamento | `GET /api/v1/funcionarios?departamento=Contabilidad` | 200 con las coincidencias. 204 si no hay resultados. |

Ejemplo de cuerpo para registrar o actualizar:

```json
{
  "nombre": "Ana Torres",
  "cargo": "Contadora",
  "departamento": "Contabilidad",
  "telefono": "3001234567"
}
```

### Reglas

- `nombre`, `cargo` y `departamento` son obligatorios. `telefono` es opcional.
- La API responde 400 si un campo obligatorio falta, está vacío o solo contiene espacios.
- La API responde 400 si se envía un `id` no nulo al registrar. Al actualizar, el ID de la ruta identifica el registro.
- La búsqueda por nombre encuentra coincidencias parciales. El filtro por departamento exige el nombre completo del área.
- Ambas búsquedas ignoran la diferencia entre mayúsculas y minúsculas.
- Un filtro vacío o con solo espacios se trata como ausente. Si llegan los dos con valor, se aplica el de nombre.

Ejemplo de respuesta de error:

```json
{
  "estado": 400,
  "mensaje": "Hay campos obligatorios sin diligenciar o con valores no válidos",
  "errores": { "nombre": "El nombre es obligatorio" }
}
```

## Estructura del proyecto

```
src/main/java/com/campuslands/directorio
├── DirectorioFuncionariosApplication.java   Punto de entrada
├── model/Funcionario.java                   Entidad JPA (tabla funcionarios)
├── repository/FuncionarioRepository.java    Acceso a datos con Spring Data JPA
├── service/FuncionarioService.java          Reglas del directorio y filtros
├── controller/FuncionarioController.java    Controlador REST
├── dto/                                     Cuerpos JSON de solicitud y de error
└── exception/                               Excepciones y manejador global (400 y 404)

src/main/resources
├── application.properties                   Conexión a MySQL
└── static/                                  Página: index.html, styles.css, app.js
```

## Pruebas

```powershell
.\mvnw.cmd test
```

Las pruebas comprueban los códigos HTTP y las reglas de la API con un repositorio simulado, así que no necesitan MySQL.
