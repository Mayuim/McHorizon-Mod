# MC Horizons mod

The **required** client mod for the **MC Horizons** event server (`play.mchorizons.net`), Minecraft **1.21.11** (Fabric).

## What it does
Paint guns look and feel like real first-person guns:
- **Your own skin's arms** hold the gun: right hand on the grip, left hand on the handguard.
- **Aim down sights**: left-click (or sneak) and the gun glides up so the rifle's red dot sight (or the pistol's iron sights) sits in the middle of your screen, with a smooth zoom and no crosshair in the way.
- **Recoil** kick on every shot, softer while aiming.
- **Reload**: the gun tilts and your left hand grabs a fresh paint pod.
- **Sprint carry**, a quick **raise** when you switch guns, and a little breathing sway.
- No vanilla re-equip bob every time the ammo bar changes.

Anything that isn't a paint gun renders exactly like vanilla.

## Install
1. Install [Fabric Loader](https://fabricmc.net/use/installer/) for 1.21.11 (0.17.3 or newer) and [Fabric API](https://modrinth.com/mod/fabric-api).
2. Download the jar: **Actions** → latest **build** → **Artifacts** → `mchorizon-mc1.21.11`.
3. Put it in `.minecraft/mods`.

## Tuning the pose (`/gunpose`)
Every position and angle can be changed live in game:
- `/gunpose` lists every setting and its value.
- `/gunpose <setting> <value>` changes one (Tab shows the names), e.g. `/gunpose aimZ -0.4`.
- `/gunpose save` writes `config/mchorizon-gun.json`. `/gunpose reset` goes back to the defaults.

Positions are in blocks from your eyes (+x right, +y up, -z forward); angles are in degrees.

## How it works
MCEvents sends aim/shot/reload state over the `mchorizon:gun` plugin channel. See [PROTOCOL.md](PROTOCOL.md).

Build locally with Java 25: `./gradlew build`
