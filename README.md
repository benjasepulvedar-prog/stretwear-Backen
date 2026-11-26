# Backend CAOS

Este es el backend de la tienda CAOS. Está hecho con Java 17, Spring Boot y Maven. La base del proyecto se creó con Spring Initializr.

Permite registrar usuarios, iniciar sesión, administrar productos y guardar compras y pedidos. También tiene JWT para proteger las rutas y Swagger para probarlas.

## Cómo iniciarlo

Desde la carpeta del repositorio:

```powershell
.\mvnw.cmd spring-boot:run "-Dspring-boot.run.profiles=demo"
```

Después abre http://localhost:8080/swagger-ui/index.html.

La terminal debe quedar abierta. Para detener el programa se usa Ctrl+C.

## Cuentas para probar

Todas usan la contraseña `CAOS2026!` en el modo demo.

- Administrador: `admin@caos.cl`. Puede crear, editar y eliminar productos.
- Empleado: `empleado@caos.cl`. Puede ver pedidos y cambiar su estado.
- Cliente: `cliente@caos.cl`. Puede comprar y consultar sus pedidos.

También se pueden registrar clientes con `POST /api/auth/register`.

Para iniciar sesión se usa `POST /api/auth/login`. Hay que copiar el token que devuelve y pegarlo en el botón Authorize de Swagger, sin escribir Bearer.

## Rutas principales

- `/api/auth`: registro e inicio de sesión.
- `/api/users/me`: consultar, actualizar o eliminar la cuenta propia.
- `/products`: consultar productos y hacer el CRUD con el administrador.
- `/api/orders`: registrar compras, consultar pedidos y actualizar estados con el empleado.

## Datos

Por ahora los datos se guardan en memoria. Al apagar el backend se pierden los usuarios creados, productos y pedidos.

Están incluidas las dependencias de Spring Initializr: Spring Data JPA, Spring Security, Lombok, Spring Web, Flyway Migration y SpringDoc OpenAPI. También se conserva H2.

JPA, H2 y Flyway tienen su inicio desactivado. Todavía no se conecta a una base de datos ni se ejecutan migraciones.

## Pruebas

Desde la carpeta del repositorio:

```powershell
.\mvnw.cmd test
```

