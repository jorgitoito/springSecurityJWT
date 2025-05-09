Security Token DEMO
===================================

Proyecto Demo con Spring Boot 3.4.4, java 21 y Spring security y JWS (Json Web Token).
- Autenticacion y Autorizacion.

Se han añadido otras caracteristicas:
- Controller de java para API REST. Con swagger-ui
- Cliente Feign para conectarse a API publica. AEMET. Spring Cloud OpenFeign.
- JPA para la Base de Datos.
- Auditable para UserEntity.
- Seguridad Custom para Controller "/signin" con LoginAttemptService.
- Test Unitarios e Integracion.
- Resilencia. Feign Retry para AEMET


Prerequisitos:
- Docker. Utilizamos una bbdd.
- Postman/Swaguer-ui para probar API.
- Cliente de Postgres (para poder ejecutar SQLs de creación de tablas)

La aplicacion para entorno DEV, necesita variables de entorno:
- AEMET_API_KEY = = tu_apikey_pedida_a_aemet_gratis
- SPRING_PROFILES_ACTIVE = dev
- DDBB_PASS = tu_password_postgres
- JWT_SECRET = tu_password_para_JWT_512_longitud

Despues de levantar la app podemos acceder a swagger-ui:

http://localhost:8080/swagger-ui/index.html#/

Docker. Utilizamos una bbdd: Comando:
docker run --name postgres-security -e POSTGRES_PASSWORD=tu_password_postgres -e POSTGRES_DB=securityDemo
-p 5432:5432 -d postgres:latest

Iniciar la app en local:
------------------------
- Paso 1. - Levantar la bbdd con el comando docker de arriba
- Paso 2. - Levantar la app con las variables de entorno descritas.
- Paso 3. - Darse de alta(usuario/password)
- Paso 4. - Conseguir JWT con los datos del login.
- Paso 5. - Llamar a end-point securizados con el JWT.
- Paso 6. - AEMET y swagger-ui no estan securizados.
