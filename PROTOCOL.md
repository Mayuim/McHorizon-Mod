# MC Horizons gun protocol (v1)

MCEvents → mod, plugin channel **`mchorizon:gun`**. The message body is UTF-8 JSON, no length prefix.
Sent by `dev.mcevents.game.paintball.GunSync` in [MCEVENT](https://github.com/Mayuim/MCEVENT), only to
players whose client registered the channel (the mod does this automatically, and the server uses it to
check the mod is installed).

Every field is optional; a message can carry several.

| Field | Meaning |
|---|---|
| `"aim": true/false` | You're aiming down sights (left-click toggle). Sneaking with a gun also aims on the client. |
| `"zoom": <level>` | Zoom level of the held gun while aiming (rifle 2, pistol 1, more with a scope). View = 1 / (1 + 0.25 × level). |
| `"shot": true` | You just fired: play the recoil kick. |
| `"reload": <ticks>` | A reload started that lasts this many ticks. `0` = reload finished or cancelled. |
| `"left": <ticks>` | With `reload`: ticks still to go (for a reload already in progress). |
| `"reset": true` | Forget everything (sent when you leave a game). |

Paint guns are recognized by the item tag MCEvents puts on them:
`minecraft:custom_data` → `PublicBukkitValues` → `"mcevents:paint_gun": "RIFLE" | "PISTOL"`.
