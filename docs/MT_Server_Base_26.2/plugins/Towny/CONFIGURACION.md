# Towny: facciones y dos alianzas fijas

No reemplazar el `config.yml` completo: Towny cambia claves entre builds. Estas
son las decisiones que deben trasladarse al archivo generado por la versión
instalada.

## Modelo

- `town` = facción de jugadores.
- `nation` = alianza fija.
- Naciones permitidas: `Orsted_Corp` y `Apostoles_Humanos`.
- Solo administradores conservan permisos para crear, borrar o gobernar
  naciones. Los jugadores sí pueden crear y administrar pueblos.
- Mantener `town_block_size: 16`.
- Desactivar la eliminación automática hasta probar backups y avisos.
- PvP, fuego, explosiones y mobs dentro de cada pueblo: apagados por defecto.
- Impuestos y coste de claim: empezar en cero durante la beta y definirlos solo
  después de observar la economía real.

## Creación inicial

1. Entrar con las dos cuentas administrativas y crear dos pueblos capitales en
   una zona protegida: `Capital_Orsted` y `Capital_Apostoles`.
2. Desde cada capital crear su nación con `/nation new <nombre>`.
3. Mantener los pueblos capitales cerrados a invitaciones.
4. Cuando una facción sea aprobada, invitar su pueblo a la nación elegida.
5. Asignar a sus miembros el grupo LuckPerms correspondiente:

```text
lp user JUGADOR parent add alianza_orsted
lp user JUGADOR parent remove alianza_apostoles
```

o la operación inversa. El cambio debe anotarse; no permitir otro durante 14
días y aplicar 7 días sin beneficio tras desertar.

## Límites operativos

- Mínimo 3 y máximo 8 miembros por pueblo.
- Claims: 8 base + 5 por miembro, máximo 48.
- Los límites se auditan semanalmente hasta implementar `MushokuCore.jar`.
- Prohibir outposts durante el lanzamiento.
- Deshabilitar comandos de creación/edición de naciones para `default` con los
  comandos de LuckPerms incluidos en el paquete.

Towny no puede expresar por sí solo el bloqueo temporal de alianza ni la fórmula
exacta de claims sin un addon o plugin propio. Hasta compilar MushokuCore, ambos
controles son administrativos y deliberadamente aparecen como tales.
