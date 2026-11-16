# Backend CAOS

Este es el backend de la tienda CAOS. Está hecho con Java 17, Spring Boot y Maven. La base del proyecto se creó con Spring Initializr.

Permite registrar usuarios, iniciar sesión, administrar productos y guardar compras y pedidos. También tiene JWT para proteger las rutas y Swagger para probarlas.

## Cómo iniciarlo

Desde la carpeta del repositorio:

```powershell
cd streetwear-backend
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

JPA y H2 siguen en las dependencias, pero su inicio automático está desactivado. Todavía no se conecta a una base de datos.

## Pruebas

Desde la carpeta streetwear-backend:

```powershell
.\mvnw.cmd test
```

La última ejecución pasó las 19 pruebas de usuarios, productos, pedidos, permisos y JWT.

El frontend está en otro repositorio y su conexión con esta API sigue pendiente.
