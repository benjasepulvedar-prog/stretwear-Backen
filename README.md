# CAOS: backend de la tienda

API básica con Java 17, Spring Boot 4.1.2, Maven, colecciones en memoria, Spring Security, JWT y Swagger. Parte de la base creada con Spring Initializr.

## Iniciar para revisar los tres roles

En la terminal del backend anterior presiona **Ctrl+C**. Después ejecuta en PowerShell:

```powershell
cd "C:\Users\sepul\OneDrive\Documents\Duocuc\Fullstack 2\stretwear-Backen\streetwear-backend"
$env:JAVA_HOME = "C:\Program Files\Java\jdk-17"
.\mvnw.cmd spring-boot:run "-Dspring-boot.run.profiles=demo"
```

Abre http://localhost:8080/swagger-ui/index.html y mantén la terminal abierta.

El perfil `demo` crea estas cuentas de demostración en memoria al iniciar:

| Correo | Contraseña de demostración | Rol |
|---|---|---|
| admin@caos.cl | CAOS2026! | admin |
| empleado@caos.cl | CAOS2026! | employee |
| cliente@caos.cl | CAOS2026! | customer |

Estas credenciales coinciden con la demostración del frontend. Todos los usuarios, productos y pedidos se guardan únicamente en memoria: se pierden al detener el backend. Todavía no hay conexión a una base de datos.

Para ejecutar el modo normal, usa `.\mvnw.cmd spring-boot:run`, sin el argumento del perfil. Ese modo comienza sin cuentas y permite registrar clientes temporales.

## Permisos

| Acción | Visitante | Cliente | Empleado | Administrador |
|---|---|---|---|---|
| Registro e inicio de sesión | Sí | Sí | Sí | Sí |
| Consultar catálogo y detalle | Sí | Sí | Sí | Sí |
| Crear, editar y eliminar productos | No | No | No | Sí |
| Registrar compra | No | Sí | No | No |
| Consultar pedidos propios | No | Sí | No | No |
| Consultar todos los pedidos y cambiar estados | No | No | Sí | No |
| Gestionar la cuenta propia | No | Sí | Sí | Sí |

Los permisos mantienen la separación del frontend: administrador para productos, empleado para pedidos y cliente para sus compras. El CRUD de cuentas siempre se limita a la cuenta del JWT.

El registro público crea un `customer`. Enviar `role: "admin"` en el registro o la actualización no concede permisos. El rol se guarda en memoria, se devuelve en el login y se incluye en el JWT. El backend comprueba que el rol del JWT todavía coincida con la cuenta.

## 1. Registro, login y JWT

Para registrar una cuenta usa **POST /api/auth/register**:

```json
{
  "name": "Benjamín",
  "email": "benjamin@example.com",
  "password": "MiClave123!"
}
```

Para iniciar sesión usa **POST /api/auth/login**. Para probar la administración:

```json
{
  "email": "admin@caos.cl",
  "password": "CAOS2026!"
}
```

La respuesta devuelve `token`, `tokenType`, `expiresIn` y `role`.

Copia el valor real de `token`, pulsa **Authorize** en Swagger y pégalo sin comillas y sin escribir `Bearer`. Para cambiar de rol, inicia sesión con la otra cuenta y reemplaza la autorización.

Fuera de Swagger se envía el encabezado:

```http
Authorization: Bearer TU_TOKEN
```

Las contraseñas se almacenan con BCrypt. El JWT dura una hora y se valida su firma, emisor, expiración, cuenta y rol. Se genera una clave nueva al iniciar, por lo que los JWT anteriores dejan de funcionar al reiniciar. En modo demo puedes ingresar otra vez con las cuentas de ejemplo; en modo normal debes registrar nuevamente tu cuenta.

Rutas de la cuenta propia:

| Método | Ruta | Función |
|---|---|---|
| GET | /api/users/me | Consultar datos y rol |
| PUT | /api/users/me | Actualizar nombre, correo y contraseña |
| DELETE | /api/users/me | Eliminar la cuenta |

El PUT recibe los mismos tres campos completos del registro. El correo es único. La contraseña exige entre 8 y 72 caracteres, con un límite de 72 bytes en UTF-8. Los datos de usuario devueltos son `id`, `name`, `email` y `role`, sin contraseña.

### Práctica de auth y users en Swagger

Un endpoint es la combinación de un método HTTP y una ruta. El mismo /api/users/me permite acciones diferentes según el método.

| Método | Acción | ¿Envía JSON? |
|---|---|---|
| POST | Registrar una cuenta o iniciar sesión | Sí |
| GET | Consultar datos | No |
| PUT | Actualizar los tres datos de la cuenta | Sí |
| DELETE | Eliminar la cuenta | No |

1. Registra una cuenta con POST /api/auth/register y los campos name, email y password. Devuelve 201 y tu usuario con role customer.
2. Inicia sesión con POST /api/auth/login usando ese email y password. Devuelve 200 y un token.
3. En Authorize pega el token real, sin comillas ni la palabra Bearer.
4. En GET /api/users/me pulsa Try it out y Execute. No escribas JSON. Devuelve 200 con id, name, email y role de la cuenta del token.
5. En PUT /api/users/me envía los tres campos completos con los valores nuevos. Devuelve 200. Los siguientes logins utilizan el nuevo correo y contraseña.
6. En DELETE /api/users/me pulsa Execute. Devuelve 204 sin cuerpo y elimina la cuenta. Su token ya no permite entrar a las rutas protegidas.

Ejemplo de actualización:

```json
{
  "name": "Benjamín actualizado",
  "email": "benjamin.nuevo@example.com",
  "password": "NuevaClave123!"
}
```

GET consulta: no registra ni modifica nada. El texto me se escribe literalmente; no debes reemplazarlo por un identificador. El backend obtiene tu identificador del JWT.

Para estas consultas protegidas usa Swagger con Authorize: abrir solamente la dirección en la barra del navegador no agrega el encabezado JWT.

Todos los datos de esta práctica son temporales. Al detener el backend se pierden las cuentas creadas, productos y pedidos. El perfil demo vuelve a crear sus tres cuentas al iniciar.

## 2. CRUD de productos

Inicia sesión como administrador y autoriza su token. En **POST /products**, envía:

```json
{
  "name": "Polera Thrasher Black",
  "category": "Ropa",
  "price": 24990,
  "stock": 10,
  "image": "/img/productos/polera-thrasher.webp",
  "featured": true
}
```

La API devuelve el producto y su identificador. Usa el identificador real recibido en las siguientes operaciones:

| Método | Ruta | Función |
|---|---|---|
| GET | /products | Listar productos |
| GET | /products/{id} | Consultar un producto |
| POST | /products | Crear producto (admin) |
| PUT | /products/{id} | Reemplazar sus datos (admin) |
| DELETE | /products/{id} | Eliminarlo (admin) |

El PUT recibe los mismos campos del POST. El precio debe ser positivo y el stock no puede ser negativo. El campo `image` guarda la ruta o URL de la imagen.

El catálogo empieza vacío. Crear el primer producto desde Swagger permite comprobar el CRUD.

## 3. Compra y pedidos

Inicia sesión como cliente y autoriza su token. En **POST /api/orders**, envía el identificador de un producto existente:

```json
{
  "name": "Benjamín",
  "address": "Av. Central 123, Santiago",
  "delivery": "normal",
  "items": [
    {
      "productId": 1,
      "quantity": 2
    }
  ]
}
```

Usa el identificador real de tu producto en lugar del `1` si es distinto. La entrega admite `normal` o `express`.

El backend obtiene el comprador desde el JWT y el correo desde su cuenta. Consulta los productos guardados en memoria, calcula el total y descuenta el stock. No utiliza precios, totales o identificadores de comprador enviados por el cliente.

Primero se valida el pedido completo. Si falta stock o un producto no existe, no se guarda el pedido ni se descuenta stock. Después de validar todas las líneas, se descuentan las cantidades y se guarda el pedido en memoria. No se admiten productos repetidos. Se usa `synchronized` para que dos compras no vendan la misma última unidad.

| Método | Ruta | Función |
|---|---|---|
| POST | /api/orders | Registrar compra (cliente) |
| GET | /api/orders | Ver pedidos propios (cliente) o todos (empleado) |
| GET | /api/orders/{id} | Consultar un pedido propio o recibido |
| PATCH | /api/orders/{id}/status | Cambiar el estado (empleado) |

Para gestionar un pedido, inicia sesión como `empleado@caos.cl`, reemplaza el token y ejecuta el PATCH:

```json
{
  "status": "Preparando"
}
```

Los estados admitidos son **Pendiente**, **Preparando**, **Enviado** y **Entregado**, iguales al frontend.

El pedido conserva el nombre y precio de los productos al comprar. Editar o eliminar un producto después no cambia los pedidos anteriores. Este flujo registra la compra y gestiona su pedido; no procesa pagos bancarios.

El carrito puede seguir en React: al confirmar la compra se enviará a esta API la lista de identificadores y cantidades.

## Códigos de respuesta

- **200:** consulta, actualización o login correctos.
- **201:** usuario, producto o pedido creado.
- **204:** cuenta o producto eliminado, sin cuerpo.
- **400:** datos inválidos.
- **401:** falta un JWT válido o las credenciales son incorrectas.
- **403:** la cuenta existe, pero su rol no permite esa operación.
- **404:** recurso inexistente o pedido ajeno.
- **409:** correo repetido o stock insuficiente.

## Compilar y probar

Desde `streetwear-backend`:

```powershell
.\mvnw.cmd clean install
```

Las pruebas usan instancias de almacenamiento en memoria separadas. Comprueban usuarios, BCrypt, JWT, roles, CRUD de productos, compras, permisos, aislamiento de pedidos, validación completa de compras y dos compras simultáneas de la última unidad.

Los repositorios usan `Map` de Java y contadores para generar los identificadores. No se instala, abre ni configura una base de datos.

## Organización

- `controllers`: rutas HTTP.
- `dto`: datos recibidos y respuestas de usuarios.
- `models`: usuarios, roles, productos y pedidos.
- `repositories`: colecciones `Map` de Java, solo en memoria.
- `services`: reglas de usuarios, productos y compras.
- `security`: creación y validación del JWT y autorización por rol.
- `config/DemoUsers.java`: cuentas del perfil de demostración.
- `exceptions`: errores de la API.
- `SwaggerConfig.java`: documentación y autorización en Swagger.

Se conservan Spring Data JPA y H2 en el pom.xml, junto con Spring Web, Spring Security/JWT, Validation, DevTools y Swagger. La configuración application.properties desactiva el inicio automático de DataSource, Hibernate/JPA, repositorios JPA y consola H2. Las dependencias están disponibles, pero la aplicación utiliza solamente colecciones Java en memoria.

## Cobertura de la pauta del backend

| Requisito | Implementación |
|---|---|
| Registro e inicio de sesión | /api/auth/register y /api/auth/login |
| CRUD de productos | /products y /products/{id} |
| Otro flujo de la tienda | Compra y gestión de pedidos en /api/orders |
| Creación y validación de JWT | Firma, expiración, identidad y roles |
| Swagger | Rutas, ejemplos y autorización Bearer |

El frontend anterior continúa usando sus datos locales y su login de demostración. La conexión de sus pantallas con esta API debe hacerse como un trabajo separado en el repositorio del frontend.

Referencias: [Spring Security JWT](https://docs.spring.io/spring-security/reference/servlet/oauth2/resource-server/jwt.html), [autorización por rutas](https://docs.spring.io/spring-security/reference/servlet/authorization/authorize-http-requests.html).
