-- ============================================================================
--  V2 – Datos semilla
--  Equivalentes a clinica-frontend/src/data/mockData.js, para que el front
--  conectado a la API muestre exactamente lo mismo que el prototipo.
--
--  Usuarios de prueba (clave para todos: clinica2026):
--    mquispe (TO) · cmendoza (TL) · jsalazar (TF) · acastillo (PS)
--    coordinador (COORDINADOR) · sistemas (SISTEMAS, solo lectura)
-- ============================================================================

-- ---------------------------------------------------------------------------
-- Especialidades (SPECIALTIES)
-- ---------------------------------------------------------------------------
INSERT INTO especialidad (id, nombre, nombre_corto, colegio_profesional, color, orden) VALUES
    ('TO', 'Terapia Ocupacional', 'T. Ocupacional', 'CTMP', '#0083A8', 1),
    ('TL', 'Terapia de Lenguaje', 'T. Lenguaje',    'CTMP', '#6F4FB3', 2),
    ('TF', 'Terapia Física',      'T. Física',      'CTMP', '#7F9A1C', 3),
    ('PS', 'Psicología',          'Psicología',     'CPsP', '#C2185B', 4);

-- ---------------------------------------------------------------------------
-- Usuarios (hash BCrypt de "clinica2026")
-- ---------------------------------------------------------------------------
INSERT INTO usuario (id, nombre_usuario, clave_hash, rol) VALUES
    ('usr-001', 'mquispe',     '$2a$10$jhvn1pM29QcnzOyFKQRb0.ST0rePDqiawcm/mH/qdLXhGkrLOGPDu', 'TERAPEUTA'),
    ('usr-002', 'cmendoza',    '$2a$10$jhvn1pM29QcnzOyFKQRb0.ST0rePDqiawcm/mH/qdLXhGkrLOGPDu', 'TERAPEUTA'),
    ('usr-003', 'jsalazar',    '$2a$10$jhvn1pM29QcnzOyFKQRb0.ST0rePDqiawcm/mH/qdLXhGkrLOGPDu', 'TERAPEUTA'),
    ('usr-004', 'acastillo',   '$2a$10$jhvn1pM29QcnzOyFKQRb0.ST0rePDqiawcm/mH/qdLXhGkrLOGPDu', 'PSICOLOGO'),
    ('usr-005', 'coordinador', '$2a$10$jhvn1pM29QcnzOyFKQRb0.ST0rePDqiawcm/mH/qdLXhGkrLOGPDu', 'COORDINADOR'),
    ('usr-006', 'sistemas',    '$2a$10$jhvn1pM29QcnzOyFKQRb0.ST0rePDqiawcm/mH/qdLXhGkrLOGPDu', 'SISTEMAS');

-- ---------------------------------------------------------------------------
-- Profesionales (currentProfessional + profesionales de la vista 360°)
-- ---------------------------------------------------------------------------
INSERT INTO profesional (id, nombres, iniciales, especialidad_id, colegio, nro_colegiatura, correo, usuario_id) VALUES
    ('prof-017', 'Lic. María Fernanda Quispe Rojas', 'MQ', 'TO', 'CTMP', '14582', 'mquispe@clinicasanjuandedios.pe',   'usr-001'),
    ('prof-021', 'Lic. Carla Mendoza Paredes',       'CM', 'TL', 'CTMP', '21877', 'cmendoza@clinicasanjuandedios.pe',  'usr-002'),
    ('prof-019', 'Lic. Jorge Luis Salazar Peña',     'JS', 'TF', 'CTMP', '19034', 'jsalazar@clinicasanjuandedios.pe',  'usr-003'),
    ('prof-033', 'Ps. Andrea Castillo Ruiz',         'AC', 'PS', 'CPsP', '28410', 'acastillo@clinicasanjuandedios.pe', 'usr-004');

-- ---------------------------------------------------------------------------
-- Paciente (patient)
-- ---------------------------------------------------------------------------
INSERT INTO paciente (id, nro_historia, nombres, iniciales, fecha_nac, sexo, diagnosticos,
                      apoderado_nombre, apoderado_relacion, apoderado_telefono, cobertura, colegio) VALUES
    ('pac-0231', 'HC-2024-0231', 'Mateo Alejandro Huamán Torres', 'MH', DATE '2019-03-12', 'M',
     ARRAY['TEA nivel 1 (CIE-11 6A02.0)', 'TDAH presentación combinada (CIE-11 6A05.2)'],
     'Rosa Torres Villanueva', 'Madre', '987 654 321', 'Salud Card · Convenio corporativo', 'I.E. Santa Rosa · 1.er grado');

-- ---------------------------------------------------------------------------
-- Banco de objetivos (objectiveBank)
-- ---------------------------------------------------------------------------
INSERT INTO banco_objetivo (id, especialidad_id, texto, creado_por) VALUES
    ('to-001', 'TO', 'Agarrar el lápiz con pinza trípode', 'prof-017'),
    ('to-002', 'TO', 'Mantener la atención en una actividad de mesa durante 10 minutos', 'prof-017'),
    ('to-003', 'TO', 'Abotonar y desabotonar prendas de forma autónoma', 'prof-017'),
    ('to-004', 'TO', 'Tolerar texturas diversas en actividades de integración sensorial', 'prof-017'),
    ('to-005', 'TO', 'Recortar con tijera siguiendo una línea recta', 'prof-017'),
    ('to-006', 'TO', 'Enhebrar cuentas medianas en un cordón', 'prof-017'),
    ('to-007', 'TO', 'Copiar figuras geométricas básicas (círculo, cuadrado, triángulo)', 'prof-017'),
    ('to-008', 'TO', 'Vestirse y desvestirse con mínima asistencia', 'prof-017'),
    ('to-009', 'TO', 'Cruzar la línea media en actividades bimanuales', 'prof-017'),
    ('to-010', 'TO', 'Seguir rutinas de 3 pasos con apoyo visual', 'prof-017'),

    ('tl-001', 'TL', 'Producir el fonema /r/ en posición inicial de palabra', 'prof-021'),
    ('tl-002', 'TL', 'Responder preguntas simples (¿qué?, ¿quién?, ¿dónde?)', 'prof-021'),
    ('tl-003', 'TL', 'Ampliar la longitud media del enunciado a 3-4 palabras', 'prof-021'),
    ('tl-004', 'TL', 'Mantener contacto visual al iniciar la comunicación', 'prof-021'),
    ('tl-005', 'TL', 'Usar fórmulas sociales (hola, gracias, por favor) de forma espontánea', 'prof-021'),
    ('tl-006', 'TL', 'Seguir instrucciones de dos pasos sin apoyo gestual', 'prof-021'),
    ('tl-007', 'TL', 'Nombrar objetos de categorías semánticas (animales, alimentos)', 'prof-021'),
    ('tl-008', 'TL', 'Narrar secuencias de 3 láminas con coherencia temporal', 'prof-021'),

    ('tf-001', 'TF', 'Mantener el equilibrio monopodal durante 5 segundos', 'prof-019'),
    ('tf-002', 'TF', 'Saltar con ambos pies sin apoyo', 'prof-019'),
    ('tf-003', 'TF', 'Subir y bajar escaleras alternando pies', 'prof-019'),
    ('tf-004', 'TF', 'Lanzar y atrapar una pelota mediana a 2 metros', 'prof-019'),
    ('tf-005', 'TF', 'Mantener postura sedente erguida durante 10 minutos', 'prof-019'),
    ('tf-006', 'TF', 'Caminar sobre una línea recta (talón-punta)', 'prof-019'),
    ('tf-007', 'TF', 'Completar un circuito motor de 4 estaciones', 'prof-019'),

    ('ps-001', 'PS', 'Identificar y nombrar emociones básicas en sí mismo', 'prof-033'),
    ('ps-002', 'PS', 'Tolerar la frustración ante cambios de rutina', 'prof-033'),
    ('ps-003', 'PS', 'Esperar su turno en juegos estructurados', 'prof-033'),
    ('ps-004', 'PS', 'Reducir conductas de evitación en interacciones con pares', 'prof-033'),
    ('ps-005', 'PS', 'Usar estrategias de autorregulación (respiración, rincón de calma)', 'prof-033'),
    ('ps-006', 'PS', 'Iniciar interacciones de juego con un par', 'prof-033'),
    ('ps-007', 'PS', 'Cumplir límites con apoyo de economía de fichas', 'prof-033');

-- ---------------------------------------------------------------------------
-- Ciclos abiertos del paciente (cycle + ciclos de la vista 360°)
--   sesion_actual = sesiones registradas + 1
-- ---------------------------------------------------------------------------
INSERT INTO ciclo (id, paciente_id, especialidad_id, profesional_id, fecha_inicio, total_sesiones, sesion_actual, frecuencia, max_objetivos, estado) VALUES
    ('cic-2026-TO-0231-03', 'pac-0231', 'TO', 'prof-017', DATE '2026-09-24', 12, 4, '1 sesión / semana · 45 min', 3, 'ABIERTO'),
    ('cic-2026-TL-0231-02', 'pac-0231', 'TL', 'prof-021', DATE '2026-09-15', 12, 6, '1 sesión / semana · 45 min', 3, 'ABIERTO'),
    ('cic-2026-TF-0231-01', 'pac-0231', 'TF', 'prof-019', DATE '2026-09-25', 12, 4, '1 sesión / semana · 45 min', 3, 'ABIERTO'),
    ('cic-2026-PS-0231-02', 'pac-0231', 'PS', 'prof-033', DATE '2026-09-09', 12, 7, '1 sesión / semana · 45 min', 3, 'ABIERTO');

-- ---------------------------------------------------------------------------
-- Objetivos activos por ciclo (activeObjectives + objectives de la vista 360°)
-- ---------------------------------------------------------------------------
INSERT INTO objetivo_ciclo (id, ciclo_id, banco_objetivo_id, linea_base, meta, sesion_inicio, orden) VALUES
    ('obj-1',    'cic-2026-TO-0231-03', 'to-001', 10, 80, 1, 1),
    ('obj-2',    'cic-2026-TO-0231-03', 'to-002', 20, 70, 1, 2),

    ('tl-obj-1', 'cic-2026-TL-0231-02', 'tl-003', 20, 75, 1, 1),
    ('tl-obj-2', 'cic-2026-TL-0231-02', 'tl-002', 40, 80, 1, 2),
    ('tl-obj-3', 'cic-2026-TL-0231-02', 'tl-004', 25, 70, 1, 3),

    ('tf-obj-1', 'cic-2026-TF-0231-01', 'tf-001', 20, 80, 1, 1),
    ('tf-obj-2', 'cic-2026-TF-0231-01', 'tf-002', 50, 90, 1, 2),

    ('ps-obj-1', 'cic-2026-PS-0231-02', 'ps-002', 10, 70, 1, 1),
    ('ps-obj-2', 'cic-2026-PS-0231-02', 'ps-003', 30, 80, 1, 2),
    ('ps-obj-3', 'cic-2026-PS-0231-02', 'ps-005', 15, 75, 1, 3);

-- ---------------------------------------------------------------------------
-- Sesiones de Terapia Ocupacional (sessionHistory)
-- ---------------------------------------------------------------------------
INSERT INTO sesion (ciclo_id, numero, fecha, profesional_id, actividad, logro, observaciones) VALUES
    ('cic-2026-TO-0231-03', 1, DATE '2026-09-24', 'prof-017',
     'Evaluación de línea base de la prensión. Actividades de pinza con pinzas de ropa y plastilina; trazos libres con crayón grueso. Se introdujo el tablero de rutina visual (3 pasos) para la actividad de mesa.',
     'Logra pinza digital inferior. La pinza trípode aparece de forma emergente solo con guía física total. Permanece sentado 3-4 minutos antes de levantarse.',
     'Baja tolerancia a la frustración al corregir el agarre. Se recomienda en casa: juegos de pinza (pinzas de ropa, cuentas grandes) 10 minutos diarios. Coordinar con Psicología estrategias de regulación.'),
    ('cic-2026-TO-0231-03', 2, DATE '2026-10-01', 'prof-017',
     'Uso de adaptador triangular en el lápiz. Trazos verticales y horizontales en plano vertical (pizarra). Actividad de mesa con reloj visual y economía de fichas (1 ficha por cada 2 minutos sentado).',
     'Con adaptador logra pinza trípode en 3 de 10 intentos sin ayuda física. Tolera 5 minutos continuos en la actividad de mesa con 2 recordatorios verbales.',
     'Mejor respuesta cuando la consigna va acompañada de apoyo visual. Mantener el adaptador en casa y en el colegio. Reducir progresivamente la guía física.'),
    ('cic-2026-TO-0231-03', 3, DATE '2026-10-08', 'prof-017',
     'Trazos de pre-escritura (líneas onduladas y en zigzag) con lápiz adaptado. Juego de ensartado con cuentas medianas. Actividad de mesa de 8 minutos dividida en dos bloques con pausa sensorial (cojín vibratorio).',
     'Pinza trípode funcional en 5 de 10 intentos con retiro de la guía física; aún requiere recordatorio verbal. Completa el primer bloque de 4 minutos sin levantarse.',
     'La pausa sensorial entre bloques mejora la permanencia en la tarea. Se sugiere a Terapia de Lenguaje reforzar el seguimiento de instrucciones de dos pasos en mesa. Enviar hoja de trazos para casa.');

-- evaluations: obj-1 → 2/10, 3/10, 5/10 · obj-2 → 1/5, 2/6, 3/6
INSERT INTO evaluacion_objetivo (sesion_id, objetivo_ciclo_id, aciertos, intentos) VALUES
    ((SELECT id FROM sesion WHERE ciclo_id = 'cic-2026-TO-0231-03' AND numero = 1), 'obj-1', 2, 10),
    ((SELECT id FROM sesion WHERE ciclo_id = 'cic-2026-TO-0231-03' AND numero = 1), 'obj-2', 1, 5),
    ((SELECT id FROM sesion WHERE ciclo_id = 'cic-2026-TO-0231-03' AND numero = 2), 'obj-1', 3, 10),
    ((SELECT id FROM sesion WHERE ciclo_id = 'cic-2026-TO-0231-03' AND numero = 2), 'obj-2', 2, 6),
    ((SELECT id FROM sesion WHERE ciclo_id = 'cic-2026-TO-0231-03' AND numero = 3), 'obj-1', 5, 10),
    ((SELECT id FROM sesion WHERE ciclo_id = 'cic-2026-TO-0231-03' AND numero = 3), 'obj-2', 3, 6);

-- ---------------------------------------------------------------------------
-- Sesiones de Terapia de Lenguaje (vista 360°: history TL)
--   tl-obj-1 → 20,30,40,50,55 · tl-obj-2 → 40,50,60,70,70 · tl-obj-3 → 25,30,30,40,45
-- ---------------------------------------------------------------------------
INSERT INTO sesion (ciclo_id, numero, fecha, profesional_id, actividad, logro, observaciones) VALUES
    ('cic-2026-TL-0231-02', 1, DATE '2026-09-15', 'prof-021',
     'Evaluación de línea base: muestra de lenguaje espontáneo en juego simbólico (cocina) y preguntas con láminas.',
     'Enunciados de 2 palabras en promedio; responde ¿qué? con apoyo gestual. Contacto visual breve al iniciar.',
     'Mayor producción verbal en juego simbólico. Introducir pictogramas para las preguntas.'),
    ('cic-2026-TL-0231-02', 2, DATE '2026-09-22', 'prof-021',
     'Expansión de enunciados con modelado (sujeto + verbo + objeto) durante el juego de la tienda.',
     'Imita enunciados de 3 palabras con modelo; responde ¿quién? señalando y nombrando.',
     'Esperar 5 segundos antes de repetir la consigna mejora la respuesta.'),
    ('cic-2026-TL-0231-02', 3, DATE '2026-09-29', 'prof-021',
     'Rutinas de saludo con contacto visual y descripción de láminas de acciones.',
     'Produce enunciados de 3 palabras de forma espontánea en 4 de 10 oportunidades.',
     'Reforzar en casa con preguntas cerradas durante la rutina de la cena.'),
    ('cic-2026-TL-0231-02', 4, DATE '2026-10-06', 'prof-021',
     'Juego simbólico dirigido (médico) con preguntas ¿dónde? y turnos de habla.',
     'Responde preguntas ¿dónde? en 7 de 10 intentos; sostiene el contacto visual al pedir objetos.',
     'Evitar consignas de más de 2 pasos; apoyar con pictogramas de secuencia.'),
    ('cic-2026-TL-0231-02', 5, DATE '2026-10-13', 'prof-021',
     'Narración de secuencias cortas con apoyo visual y expansión de enunciados a 4 palabras.',
     'Enunciados de 3-4 palabras en más de la mitad de las oportunidades; mantiene contacto visual al iniciar la comunicación en 9 de 20 intentos.',
     'Responde mejor en formato de juego simbólico. Se recomienda a las demás áreas usar consignas cortas y esperar 5 segundos antes de repetir.');

INSERT INTO evaluacion_objetivo (sesion_id, objetivo_ciclo_id, aciertos, intentos) VALUES
    ((SELECT id FROM sesion WHERE ciclo_id = 'cic-2026-TL-0231-02' AND numero = 1), 'tl-obj-1', 2, 10),
    ((SELECT id FROM sesion WHERE ciclo_id = 'cic-2026-TL-0231-02' AND numero = 1), 'tl-obj-2', 4, 10),
    ((SELECT id FROM sesion WHERE ciclo_id = 'cic-2026-TL-0231-02' AND numero = 1), 'tl-obj-3', 5, 20),
    ((SELECT id FROM sesion WHERE ciclo_id = 'cic-2026-TL-0231-02' AND numero = 2), 'tl-obj-1', 3, 10),
    ((SELECT id FROM sesion WHERE ciclo_id = 'cic-2026-TL-0231-02' AND numero = 2), 'tl-obj-2', 5, 10),
    ((SELECT id FROM sesion WHERE ciclo_id = 'cic-2026-TL-0231-02' AND numero = 2), 'tl-obj-3', 3, 10),
    ((SELECT id FROM sesion WHERE ciclo_id = 'cic-2026-TL-0231-02' AND numero = 3), 'tl-obj-1', 4, 10),
    ((SELECT id FROM sesion WHERE ciclo_id = 'cic-2026-TL-0231-02' AND numero = 3), 'tl-obj-2', 6, 10),
    ((SELECT id FROM sesion WHERE ciclo_id = 'cic-2026-TL-0231-02' AND numero = 3), 'tl-obj-3', 3, 10),
    ((SELECT id FROM sesion WHERE ciclo_id = 'cic-2026-TL-0231-02' AND numero = 4), 'tl-obj-1', 5, 10),
    ((SELECT id FROM sesion WHERE ciclo_id = 'cic-2026-TL-0231-02' AND numero = 4), 'tl-obj-2', 7, 10),
    ((SELECT id FROM sesion WHERE ciclo_id = 'cic-2026-TL-0231-02' AND numero = 4), 'tl-obj-3', 4, 10),
    ((SELECT id FROM sesion WHERE ciclo_id = 'cic-2026-TL-0231-02' AND numero = 5), 'tl-obj-1', 11, 20),
    ((SELECT id FROM sesion WHERE ciclo_id = 'cic-2026-TL-0231-02' AND numero = 5), 'tl-obj-2', 7, 10),
    ((SELECT id FROM sesion WHERE ciclo_id = 'cic-2026-TL-0231-02' AND numero = 5), 'tl-obj-3', 9, 20);

-- ---------------------------------------------------------------------------
-- Sesiones de Terapia Física (vista 360°: history TF)
--   tf-obj-1 → 20,30,40 · tf-obj-2 → 50,60,70
-- ---------------------------------------------------------------------------
INSERT INTO sesion (ciclo_id, numero, fecha, profesional_id, actividad, logro, observaciones) VALUES
    ('cic-2026-TF-0231-01', 1, DATE '2026-09-25', 'prof-019',
     'Evaluación de línea base: equilibrio monopodal, salto bipodal y control postural en sedente.',
     'Equilibrio monopodal de 1-2 segundos con apoyo visual; salta con ambos pies con sujeción de una mano.',
     'Hipotonía leve de tronco. Iniciar fortalecimiento de core con pelota terapéutica.'),
    ('cic-2026-TF-0231-01', 2, DATE '2026-10-02', 'prof-019',
     'Circuito motor: equilibrio sobre cojín inestable, saltos en aros y rodado sobre colchoneta.',
     'Mantiene equilibrio monopodal 3 segundos en 3 de 10 intentos; salto bipodal sin apoyo en 6 de 10.',
     'Mejor desempeño con cuenta regresiva verbal. Reforzar saltos en casa sobre superficie blanda.'),
    ('cic-2026-TF-0231-01', 3, DATE '2026-10-09', 'prof-019',
     'Control postural en sedente con cojín de aire y actividades de alcance; saltos consecutivos en zigzag.',
     'Equilibrio monopodal de 5 segundos en 4 de 10 intentos; salto bipodal sin apoyo en 7 de 10.',
     'Hipotonía leve de tronco; se trabaja control postural. La postura sedente erguida impacta directamente en la escritura (coordinar con T. Ocupacional).');

INSERT INTO evaluacion_objetivo (sesion_id, objetivo_ciclo_id, aciertos, intentos) VALUES
    ((SELECT id FROM sesion WHERE ciclo_id = 'cic-2026-TF-0231-01' AND numero = 1), 'tf-obj-1', 2, 10),
    ((SELECT id FROM sesion WHERE ciclo_id = 'cic-2026-TF-0231-01' AND numero = 1), 'tf-obj-2', 5, 10),
    ((SELECT id FROM sesion WHERE ciclo_id = 'cic-2026-TF-0231-01' AND numero = 2), 'tf-obj-1', 3, 10),
    ((SELECT id FROM sesion WHERE ciclo_id = 'cic-2026-TF-0231-01' AND numero = 2), 'tf-obj-2', 6, 10),
    ((SELECT id FROM sesion WHERE ciclo_id = 'cic-2026-TF-0231-01' AND numero = 3), 'tf-obj-1', 4, 10),
    ((SELECT id FROM sesion WHERE ciclo_id = 'cic-2026-TF-0231-01' AND numero = 3), 'tf-obj-2', 7, 10);

-- ---------------------------------------------------------------------------
-- Sesiones de Psicología (vista 360°: history PS)
--   ps-obj-1 → 10,20,20,30,35,40 · ps-obj-2 → 30,40,45,50,60,60 · ps-obj-3 → 15,25,35,40,50,55
-- ---------------------------------------------------------------------------
INSERT INTO sesion (ciclo_id, numero, fecha, profesional_id, actividad, logro, observaciones) VALUES
    ('cic-2026-PS-0231-02', 1, DATE '2026-09-09', 'prof-033',
     'Evaluación de línea base: observación en juego estructurado con cambios de rutina programados.',
     'Llanto y rechazo ante el cambio de actividad; espera su turno con ayuda física en 3 de 10 oportunidades.',
     'Anticipar los cambios con apoyo visual. Entrevista a la madre para replicar estrategias en casa.'),
    ('cic-2026-PS-0231-02', 2, DATE '2026-09-16', 'prof-033',
     'Introducción del rincón de calma y pictogramas de emociones; juego de turnos con reloj de arena.',
     'Acude al rincón de calma con guía verbal; espera el turno con apoyo visual en 4 de 10 oportunidades.',
     'Responde bien a la respiración con burbujas. Entregar pictogramas para casa.'),
    ('cic-2026-PS-0231-02', 3, DATE '2026-09-23', 'prof-033',
     'Juego estructurado con cambios de rutina anticipados mediante cuenta regresiva visual.',
     'Tolera un cambio de actividad sin llanto en 2 de 10 ocasiones; usa la respiración con modelo en 7 de 20.',
     'La cuenta regresiva visual reduce las conductas de evitación.'),
    ('cic-2026-PS-0231-02', 4, DATE '2026-09-30', 'prof-033',
     'Economía de fichas en juegos de mesa cooperativos; práctica de autorregulación ante frustración inducida.',
     'Espera su turno en 5 de 10 oportunidades; recurre al rincón de calma por iniciativa propia una vez.',
     'Unificar la economía de fichas con las demás terapias.'),
    ('cic-2026-PS-0231-02', 5, DATE '2026-10-07', 'prof-033',
     'Historias sociales sobre cambios de rutina y juego por turnos con tres pares.',
     'Tolera el cambio de rutina en 7 de 20 ocasiones; espera su turno en 6 de 10 con recordatorio visual.',
     'La familia replica el rincón de calma en casa con buenos resultados.'),
    ('cic-2026-PS-0231-02', 6, DATE '2026-10-14', 'prof-033',
     'Generalización de estrategias de autorregulación a la sala de espera y al aula (visita escolar).',
     'Tolera la frustración ante cambios en 4 de 10 ocasiones; usa estrategias de autorregulación de forma espontánea en 11 de 20.',
     'Se implementó rincón de calma con pictogramas; la familia lo replica en casa. Sugerencia a todas las áreas: anticipar el cambio de actividad con cuenta regresiva visual.');

INSERT INTO evaluacion_objetivo (sesion_id, objetivo_ciclo_id, aciertos, intentos) VALUES
    ((SELECT id FROM sesion WHERE ciclo_id = 'cic-2026-PS-0231-02' AND numero = 1), 'ps-obj-1', 1, 10),
    ((SELECT id FROM sesion WHERE ciclo_id = 'cic-2026-PS-0231-02' AND numero = 1), 'ps-obj-2', 3, 10),
    ((SELECT id FROM sesion WHERE ciclo_id = 'cic-2026-PS-0231-02' AND numero = 1), 'ps-obj-3', 3, 20),
    ((SELECT id FROM sesion WHERE ciclo_id = 'cic-2026-PS-0231-02' AND numero = 2), 'ps-obj-1', 2, 10),
    ((SELECT id FROM sesion WHERE ciclo_id = 'cic-2026-PS-0231-02' AND numero = 2), 'ps-obj-2', 4, 10),
    ((SELECT id FROM sesion WHERE ciclo_id = 'cic-2026-PS-0231-02' AND numero = 2), 'ps-obj-3', 5, 20),
    ((SELECT id FROM sesion WHERE ciclo_id = 'cic-2026-PS-0231-02' AND numero = 3), 'ps-obj-1', 2, 10),
    ((SELECT id FROM sesion WHERE ciclo_id = 'cic-2026-PS-0231-02' AND numero = 3), 'ps-obj-2', 9, 20),
    ((SELECT id FROM sesion WHERE ciclo_id = 'cic-2026-PS-0231-02' AND numero = 3), 'ps-obj-3', 7, 20),
    ((SELECT id FROM sesion WHERE ciclo_id = 'cic-2026-PS-0231-02' AND numero = 4), 'ps-obj-1', 3, 10),
    ((SELECT id FROM sesion WHERE ciclo_id = 'cic-2026-PS-0231-02' AND numero = 4), 'ps-obj-2', 5, 10),
    ((SELECT id FROM sesion WHERE ciclo_id = 'cic-2026-PS-0231-02' AND numero = 4), 'ps-obj-3', 4, 10),
    ((SELECT id FROM sesion WHERE ciclo_id = 'cic-2026-PS-0231-02' AND numero = 5), 'ps-obj-1', 7, 20),
    ((SELECT id FROM sesion WHERE ciclo_id = 'cic-2026-PS-0231-02' AND numero = 5), 'ps-obj-2', 6, 10),
    ((SELECT id FROM sesion WHERE ciclo_id = 'cic-2026-PS-0231-02' AND numero = 5), 'ps-obj-3', 5, 10),
    ((SELECT id FROM sesion WHERE ciclo_id = 'cic-2026-PS-0231-02' AND numero = 6), 'ps-obj-1', 4, 10),
    ((SELECT id FROM sesion WHERE ciclo_id = 'cic-2026-PS-0231-02' AND numero = 6), 'ps-obj-2', 6, 10),
    ((SELECT id FROM sesion WHERE ciclo_id = 'cic-2026-PS-0231-02' AND numero = 6), 'ps-obj-3', 11, 20);

-- ---------------------------------------------------------------------------
-- Notas interdisciplinarias compartidas (sharedNotes)
-- ---------------------------------------------------------------------------
INSERT INTO nota_compartida (id, paciente_id, especialidad_id, profesional_id, fecha, texto) VALUES
    ('note-1', 'pac-0231', 'PS', 'prof-033', DATE '2026-10-14', 'Nuevo plan de refuerzo: economía de fichas unificada para todas las terapias (1 ficha = 2 min en tarea).'),
    ('note-2', 'pac-0231', 'TF', 'prof-019', DATE '2026-10-09', 'Usar cojín de aire en la silla durante actividades de mesa para favorecer el control postural.'),
    ('note-3', 'pac-0231', 'TL', 'prof-021', DATE '2026-10-06', 'Evitar consignas de más de 2 pasos; apoyar con pictogramas de secuencia.');
