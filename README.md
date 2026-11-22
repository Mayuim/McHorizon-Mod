# MC Horizons mod

The client mod for the **MC Horizons** event server (`play.mchorizons.net`), Minecraft **1.21.11** (Fabric).

It's optional. Players without it see the normal sidebar and tab list.

## What you get
**Always on screen**
- **PROFILE** (top left): your tier (S–F) badge, overall rank, events played, lifetime points.
- **GAME** (left): current game, map, time left, your place and points, your team's place and points, and which teams you're tied with.
- **YOUR TEAM** (top right): team name, total points, place, and how far behind the team ahead you are (or how far you lead).
- **LIVE** (right): this game's team standings, with your team highlighted.
- **Event info** (bottom right): event name, game number and multiplier, round.

**Hold Tab**: the team overview. Every team with its rank, points, this game's points, and members with their skin heads. Your team is outlined in gold, and offline players are grayed out.

While the panels are up, the server's sidebar is hidden since it shows the same info.

## Install
1. Install [Fabric Loader](https://fabricmc.net/use/installer/) for 1.21.11 (0.17.3 or newer) and [Fabric API](https://modrinth.com/mod/fabric-api).
2. Download the jar: **Actions** → latest **build** → **Artifacts** → `mchorizon-mc1.21.11`.
3. Put it in `.minecraft/mods`.

## Settings
- **F7** toggles the HUD (change it in Controls → Key Binds → MC Horizons).
- `.minecraft/config/mchorizon.json`: `hudScale` (0.5–2.0), `hideSidebar`, `replaceTabList`, and `showProfile` / `showGame` / `showYourTeam` / `showLive` / `showEventInfo`.

## How it works
MCEvents sends the event data over the `mchorizon:hud` plugin channel. See [PROTOCOL.md](PROTOCOL.md).
On any other server the mod does nothing.

Build locally with Java 25: `./gradlew build`
