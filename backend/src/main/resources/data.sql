-- Datos semilla para probar la API a mano (Postman, etc.).
-- Ejecutar UNA VEZ contra la base local, despues de levantar la app
-- al menos una vez (para que Hibernate cree las tablas con ddl-auto: update).

INSERT INTO usuarios (nombre, correo, password, rol, codigo_estudiante, universidad, documento_identidad, estado_verificacion, fecha_creacion)
VALUES
    ('Ana Torres', 'ana.torres@upc.edu.pe', 'password123', 'BUSCADOR', 'U202012345', 'UPC', NULL, 'VERIFICADO', now()),
    ('Diego Ramos', 'diego.ramos@upc.edu.pe', 'password123', 'BUSCADOR', 'U202054321', 'UPC', NULL, 'PENDIENTE', now()),
    ('Luis Gomez', 'luis.gomez@gmail.com', 'password123', 'ANFITRION', NULL, NULL, '70123456', 'VERIFICADO', now());

INSERT INTO publicaciones (anfitrion_id, zona, precio, descripcion, estado, fecha_publicacion)
SELECT id, 'Jesus Maria', 650.00, 'Habitacion amoblada cerca a la UPC, bano compartido, incluye wifi.', 'ACTIVA', now()
FROM usuarios WHERE correo = 'luis.gomez@gmail.com';

INSERT INTO publicacion_fotos (publicacion_id, url_foto)
SELECT id, 'https://example.com/fotos/habitacion1-1.jpg'
FROM publicaciones WHERE zona = 'Jesus Maria';

INSERT INTO publicacion_fotos (publicacion_id, url_foto)
SELECT id, 'https://example.com/fotos/habitacion1-2.jpg'
FROM publicaciones WHERE zona = 'Jesus Maria';
