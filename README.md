# Unstable Underworld v3.5.0 — Ruinas y rendimiento (Minecraft Java 26.3)

Proyecto fuente para **Fabric**. Contiene el código completo, `gradle.properties`,
`build.gradle`, recursos, pruebas locales y GitHub Actions. No incluye el `.jar` compilado.

## Optimización

La v3.3.2 generaba el terreno completo de cada chunk en el evento `CHUNK_LOAD`,
lo que podía saturar el hilo del servidor al explorar rápidamente. En v3.4.0:

- **Cola de generación en el hilo principal**: como máximo 2 chunks terminados por tick,
  con un presupuesto **orientativo** de 8 ms entre trabajos. Un chunk individual no
  se puede interrumpir: si tarda más, **aún podría producir un tick lento**.
- Al descargarse un chunk se cancela el trabajo pendiente para ese chunk.
- Las posiciones se deduplican: no se genera dos veces un chunk cargado.
- Al entrar desde el Nether, se genera inmediatamente el chunk de destino,
  para mantener una superficie real mientras cae la entidad.
- Los cambios diferidos en bloques usan `ServerLevel.setBlock` para que lleguen
  al cliente aunque el chunk se haya enviado antes de terminar la decoración.
- Las estructuras omiten operaciones sobre el aire ya vacío y reutilizan
  resultados de comprobación de emplazamientos con caché LRU de 512 entradas.
- Se elimina una evaluación redundante de la altura por columna y se evita
  la generación fuera del radio de 6.400 bloques.
- Los portales se comprueban cada 5 ticks, sin retardar la detección de caídas.

**Limitación intencionada:** cuando se explora rápidamente, algunos chunks
pueden visualizarse vacíos durante un breve periodo hasta que su turno se procese.
Esto sacrifica inmediatez a cambio de reducir ráfagas de trabajo. Si aparecen
huecos persistentes o el servidor se bloquea de nuevo, se necesitan los registros
`logs/latest.log` y `crash-reports` para encontrar el origen concreto.

## Características conservadas

- Dimensión independiente del Nether: **el Nether original no se modifica**.
- Entrada al caer bajo el Nether, con las mismas X/Z y movimiento descendente.
- Regreso mediante `/underworld return` a **Y = −7** y las mismas X/Z.
- Modo Aventura en el Underworld, restaurando el modo anterior al salir.
- Muerte por daño normal del vacío cuando se cae fuera del Underworld.
- Terreno de **30 bloques**, pequeñas montañas, grandes zonas de soul sand,
  soul soil, blackstone, grietas y vacío completo a más de 6.400 bloques.
- Doce tipos de ruinas detalladas, cofres con botín temático y portales
  extremadamente raros al Overworld.
- Cielo tipo Nether, sin sol ni nubes.

## Comandos

- `/underworld enter`: entra cayendo desde arriba del Underworld.
- `/underworld return`: sale al Nether, Y = −7, X/Z intactas.
- `/underworld queue`: muestra cuántos chunks esperan generación; útil al
  explorar rápidamente o diagnosticar retrasos.

## Requisitos

- Minecraft Java **26.3**
- Fabric Loader **0.19.5**
- Fabric API **0.162.0+26.3**
- Java **25** (para compilación y ejecución)

## Compilación en GitHub

1. Crea una copia de seguridad completa de tu mundo y del servidor.
2. Sube **todos** los archivos y carpetas de este ZIP a la raíz del repositorio.
3. Asegúrate de borrar los JAR y los JSON duplicados de versiones anteriores.
4. Ve a **Actions → Build Minecraft 26.3 mod → Run workflow**.
5. Descarga el artefacto `unstable-underworld-26.3` y extrae
   `unstable-underworld-3.5.0.jar`.
6. Instala **el mismo JAR** en cliente y servidor Fabric, junto con Fabric API.
7. Prueba primero **un mundo nuevo o una copia del original**.

El workflow realiza: pruebas de geometría, distribución, regresión matemática,
validación de recursos y botín, `gradle build` e inspección del JAR.

## Migración y seguridad

- Los chunks del Underworld de v3.3.2 que tengan el marcador de finalización
  **no se regeneran** y conservan sus estructuras.
- Un chunk antiguo sin ese marcador puede regenerarse, eliminando construcciones.
- Si aparecen avisos de datapacks o modo seguro, no guardes el único mundo
  original en modo seguro. Extrae la causa exacta de `logs/latest.log`.
- No recomendamos migrar el único mundo existente sin una copia de seguridad.

## Verificación y límites

Las pruebas matemáticas, los validadores JSON y la integridad del ZIP pueden
realizarse sin Minecraft. **No son una compilación ni una prueba de arranque**:
el proyecto requiere una ejecución real de GitHub Actions y prueba en el juego.
La cola de trabajos reduce los picos por ráfagas de chunks pero no ofrece una
cota dura al tiempo de un único chunk. Tampoco garantiza que la causa de los
crashes previos fuera exclusivamente la generación: podría ser memoria,
conflictos de mods o datos de mundo.

Los diseños de ruinas son originales y no copias de mapas de videos.
Licencia: MIT; consulta `LICENSE`.

---

## v3.5.0 — Ruinas, botín y regiones visuales (2026-10-13)

### Qué cambia

- **12 familias de ruinas**, frente a 8: se añaden osarios, acueductos
  fracturados, fortalezas de almas y agujas destruidas. Se mantienen las ruinas
  antiguas y su distribución no alineada con los ejes de chunks.
- **Seis posibles cofres por ruina**. La probabilidad individual aproximada es
  85 %, 77 %, 64 %, 53 %, 39 % y 31 %; ciertas ruinas reciben 8 puntos extra.
  El último cofre, cuando aparece, usa la tabla `forbidden_treasury`.
- El botín ordinario ahora contiene **manzanas doradas (1 a 3)** y, raramente,
  **lingotes de netherita**. Las tesorerías raras ofrecen mejores cantidades,
  diamantes, restos antiguos y otras recompensas. No se garantiza que todos los
  cofres contengan netherita.
- Los cofres siempre se colocan como **individuales**, con orientación hacia el
  centro de la ruina, terreno de apoyo y una zona de acceso despejada de tres
  bloques de altura. La geometría y las casillas reservadas se calculan de
  manera idéntica desde cualquier chunk. No se colocan sobre huecos del vacío.
- **Cinco regiones geológicas visuales**: dunas de almas, páramos de basalto,
  tierra de blackstone, llanuras cenicientas y suelo de sculk.
- **Caminos sinuosos de basalto pulido** dentro de las regiones de basalto y de
  cenizas. Siguen coordenadas absolutas y se cortan al llegar a los abismos.
- Se mantiene la cola de generación de v3.4 (máximo dos chunks/tick y
  presupuesto blando), el espesor de 30 bloques, el vacío final, el modo
  aventura y los portales raros al Overworld.

### Aclaración sobre «biomas»

Para **no romper el generador vacío** que permite conservar los chunks del Nether
original y garantizar la generación retroactiva, las cinco regiones son
**sub-biomas visuales** calculados por el mod. En F3, la dimensión todavía indica
el bioma registrado `underworld:wasteland`. No son cinco biomas independientes
registrados en Minecraft. Si se requieren biomas reales identificables en F3,
sería necesaria una modificación mayor del biome source y pruebas con el servidor.

### Compatibilidad con mundos anteriores

Los chunks del Underworld **ya generados con v3.4** y que contienen su marcador
se mantienen como están; los nuevos caminos, regiones y estructuras aparecerán
**en chunks nuevos**. Esto evita borrar cofres, inventarios o construcciones.
No elimines manualmente la carpeta de la dimensión sin hacer copia de seguridad.

### Pruebas añadidas

El workflow compila/ejecuta dos pruebas Java nuevas sin requerir Minecraft:
`StructureLayoutTest` (12 familias, ubicación de cofres, entradas accesibles,
separación y colocación determinista) y `VisualRegionsTest` (5 regiones,
frecuencia de senderos y determinismo). También valida las dos tablas de botín
antes de llamar a Gradle y verifica su inclusión en el JAR.

**Pendiente**: compilar la parte Minecraft/Fabric con JDK 25 en GitHub Actions y
probar carga de recursos/looteo de cofres dentro del juego. Una prueba de JSON no
comprueba automáticamente la semántica del datapack.
