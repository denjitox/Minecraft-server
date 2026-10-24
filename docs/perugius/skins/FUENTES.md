# FUENTES — Skins para la Fortaleza de Perugius

Descargado: **2026-10-18 (mskins.net)**.
Método: búsqueda por apodo en `https://mskins.net/en/search?q=<término>` → página
`https://mskins.net/en/skin/<Nombre>-<id>` → descarga cruda vía
`https://mskins.net/en/skin/<Nombre>-<id>/download`.

Verificación de cada archivo (hecha en disco, 2026-10-18): magic bytes PNG
`89 50 4E 47 0D 0A 1A 0A`, dimensiones y tamaño leídos con `System.Drawing`,
SHA256 con `Get-FileHash`. Todos son PNG válidos.

> **mskins.net no expone un campo de autor.** Indexa las skins por el **apodo del
> jugador que la usa**. Por eso la "fuente" es la URL de la skin, y el `<Nombre>`
> del slug es el apodo, no el autor. No hay licencia declarada en el sitio (skins
> de comunidad, uso libre de facto): si vas a publicar el servidor, conviene
> re-subirlas a Mineskin y guardar el blob (ver "Cómo aplicarlas").

---

## ⚠️ Aviso de fidelidad (leer antes de usar)

**No existe una skin canónica/oficial de Perugius Dola ni de sus 11 Espíritus
Ancestrales** en las fuentes consultadas. Busqué en mskins.net, NameMC,
PlanetMinecraft, NovaSkin y Laby (vía búsqueda web, 2026-10-18): **0 diseños
confirmados del personaje**. Cero resultados para "perugius skin" en buscadores
generales; lo único que aparece son cuentas de Minecraft *llamadas* Perugius.

`mskins.net` indexa por apodo, así que encontré skins de cuentas llamadas
"Perugius", "PERUGIUS_DOLA", "Yuruzu", "Orsted", etc., **pero la mayoría NO
dibuja al personaje**. Rendericé cada candidata (carpeta `_preview/`, 600 px) y la
clasifiqué por **lo que la skin ES**, no por el nombre del archivo. Los apodos que
resultaron ser otra cosa quedaron en `_descartados/`.

En corto: lo de la carpeta principal son **aproximaciones**, no réplicas del anime.

---

## Carpeta principal (`perugius\skins\`)

| Archivo | Rol pensado | Qué se ve realmente (render 600 px) | Fuente (URL) | Bytes | Dim | SHA256 |
|---|---|---|---|---|---|---|
| `perugius.png` | NPC Perugius | Cabello rubio/plateado con vincha amarilla, túnica azul con ribetes dorados y una gema en el pecho. Lee como "mago real / rey dragón". **Mejor aproximación a Perugius que encontré.** | https://mskins.net/en/skin/Perugius-orgJz | 2759 | 64×64 | `09df5b31b4fb91f3dc5100dac639568f3be2064456e94df7c2c1ab6787633847` |
| `perugius_alt_orsted.png` | Alternativa / el Dios Dragón | Pelo largo blanco, ropas y armadura negras, pálido. Coincide con **Orsted** (el Dios Dragón). Perugius y Orsted son ambos del Pueblo Dragón: sirve como figura secundaria o variante. | https://mskins.net/en/skin/Orsted-g6kQG | 1378 | 64×64 | `7d58fc299ee3872227481986c056a0414872e9ed0446164ccf68b45c0af96bc7` |
| `recepcion.png` | NPC de recepción (recibidor de la fortaleza) | Persona de traje negro con cabello negro y reflejos rojos. Lee como mayordomo/administrador oscuro. | https://mskins.net/en/skin/Receptionist-XYYrk | 951 | 64×64 | `9d8cd10842b76ac79ac3878bc4dcaaf9d6257392fa688ade67f39327bba407fa` |
| `sirviente_1_dragonknight.png` | Espíritu guardián (caballero dragón) | Armadura negra/azul con cuernos rojos y rostro naranja. Un "Dragon Knight" clásico. | https://mskins.net/en/skin/DragonKnight-wnVxw | 1508 | 64×64 | `78b35220bedd37d2057c201844330682571602e937bcf8be47df2a6c245cb5a4` |
| `sirviente_2_spiritknight.png` | Espíritu guardián | Armadura oscura con grietas rojas incandescentes. Lee como "espíritu/caballero oscuro". | https://mskins.net/en/skin/Spiritknight-r4m0B | 2735 | 64×64 | `8980ae30b3b9387efd33dd80ff04c094ee087efb85a9f852a9b8ca2a9fc0bb5e` |
| `sirviente_3_knight_rojo.png` | Espíritu guardián | Caballero totalmente rojo (estilo demonio). Contraste fuerte para una guardia. | https://mskins.net/en/skin/Knight-N8PLD | 2420 | 64×64 | `0a2e2dc109d22b610043a25567ab96c5179a6338461b40725f06874dcabb145b` |
| `sirviente_4_sylphiette.png` | Sirviente (espíritu/mago) | Orejas de elfo, cabello verde, chaleco blanco/negro. Coincide con **Sylphiette** (aunque es una heroína principal, no una sirvienta). | https://mskins.net/en/skin/Sylphiette-3gBLx | 1292 | 64×64 | `b96e864521694bd63109a14cf9dec0e2df8955b446e19a304cd1068fc2c12e6e` |
| `sirviente_5_guerrero.png` | Sirviente (guerrero) | Guerrera de pelo rubio, armadura negro/blanco con azul. **NO es Ruijerd** (Ruijerd es Superd, con tercer ojo y pelo verde/azul): es un guerrero genérico. | https://mskins.net/en/skin/Ruijerd-GkQzQ | 3482 | 64×64 | `0f2876df8b72cd28381683840fb7ab5400523f419c30b78f18ced097dda7d0ad` |
| `extra_rudeus.png` | Extra (NPC temático) | Pelo castaño, ojos verdes, ropa gris con correas. Coincide bastante con **Rudeus**. | https://mskins.net/en/skin/Rudeus-X9rVg | 1515 | 64×64 | `1e37b0c2b2b2714466b2f1f0b446b970ca14fe99318c810bcccb604053bce274` |
| `extra_roxy.png` | Extra (NPC temático) | Pelo azul corto, ojos azules, túnica blanca/gris. Coincide con **Roxy Migurdia**. | https://mskins.net/en/skin/RoxyMigurdia-GJxRJ | 2974 | 64×64 | `25711e420b85be102dd369e07b8a9b52d895cf1972e68bd72fd79632db5e9453` |
| `extra_eris.png` | Extra (NPC temático) | Pelo rojo, atuendo rojo/oscuro. Coincide con **Eris Greyrat**. | https://mskins.net/en/skin/ErisGreyrat77-NGxD6 | 1799 | 64×64 | `cadbe64aee49b1ea89821484bea1a5bfd6fb100e70472441afb9d498fef49b2d` |
| `extra_mago_oscuro.png` | Extra (villano) | Figura encapuchada negra con detalles rojos. Mago/sicario oscuro genérico (podría servir de Hitogami/aprendiz). | https://mskins.net/en/skin/Mushoku_Tensei-XK3Yg | 1830 | 64×64 | `f1c42090f2aba76b8277f9bdcea8720244c40dcec4d5e6feaec49240b73d0b52` |

## `_descartados\` — apodos que NO dibujan al personaje

Movidos acá (no borrados) porque el apodo coincidía pero la skin es otra cosa:

| Archivo | Apodo/consulta | Qué es en realidad | URL |
|---|---|---|---|
| `mskins_PERUGIUS_DOLA_anciano.png` | PERUGIUS_DOLA | Un anciano con barba, anteojos y traje negro/blanco. **No es Perugius.** | https://mskins.net/en/skin/PERUGIUS_DOLA-pvjV1 |
| `mskins_PERUGIUS_DOLA-pvjV1.png` | (idem, copia) | idem | https://mskins.net/en/skin/PERUGIUS_DOLA-pvjV1 |
| `mskins_Yuruzu_soldado_camuflado.png` | Yuruzu | Soldado militar con camuflaje verde. **No es Yuruzu** (el curandero de los espíritus). | https://mskins.net/en/skin/Yuruzu-Lo6xA |
| `mskins_Ghislaine_no_verificado.png` | Ghislaine | Persona de pelo negro, top azul y prenda roja. No confirmable como Ghislaine. | https://mskins.net/en/skin/Ghislaine-lOj9V |
| `mskins_Maid_tribal_64x32.png` | Maid | Personaje tribal con túnica turquesa. **No es una maid** (y es formato viejo 64×32). | https://mskins.net/en/skin/Maid-ZWqr5 |
| `mskins_Perugius-orgJz.png` | Perugius | Copia cruda de la que quedó como `perugius.png` | https://mskins.net/en/skin/Perugius-orgJz |
| `mskins_Orsted-g6kQG.png` | Orsted | Copia cruda de `perugius_alt_orsted.png` | https://mskins.net/en/skin/Orsted-g6kQG |
| `test_mc-heads_*.png`, `test_minotar_*.png` | — | Pruebas de endpoints (`mc-heads.net/skin/<user>`, `minotar.net/skin/<user>`). Devuelven la skin real si el usuario existe y una por defecto si no. | https://mc-heads.net/skin/Perugius , https://minotar.net/skin/MushokuTensei |

## `_preview\` — renders 600 px (para inspección visual)

PNG 600 px renderizados por mskins.net (`/uploads/preview/<xx>/<yy>/<Nombre>-600.png`).
Se usaron para clasificar cada skin. No son las skins crudas.

---

## Cómo aplicarlas en Citizens

```mcfunction
/npc create Perugius
/npc skin --url <URL_pública_del_png>      # la URL debe terminar en .png y ser accesible
/npc skin Perugius                          # alternativa: copiar la skin de un jugador por nombre
```

- `/npc skin --url <png>` está **respaldado por Mineskin** por dentro (así lo dice la
  wiki). Para modelo *slim* (Alex) conviene subir el PNG a mineskin.org y usar su
  opción de modelo.
- **Trampa del proyecto:** el server corre `online-mode=false`. La wiki de Citizens
  avisa que `/npc skin <NOMBRE>` (resolver una skin de Mojang por nombre) puede
  **fallar en offline mode**. Usá `--url` (blob de Mineskin) para no depender de Mojang.
- Para que Bedrock (Geyser) la vea, no hay que configurar nada extra: Citizens ≥ la
  build con "Bedrock skin support" ya manda el blob correcto (ver abajo).
