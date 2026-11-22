# MC Horizons HUD protocol (v1)

MCEvents → mod, plugin channel **`mchorizon:hud`**, once per second, only to players whose client
registered the channel (the mod does this automatically). The message body is UTF-8 JSON, no length prefix.
Sent by `dev.mcevents.display.HorizonHud` in [MCEVENT](https://github.com/Mayuim/MCEVENT).

```jsonc
{
  "v": 1,
  "event": { "name", "title", "active", "paused", "mini", "phase", "phaseId",
             "round", "rounds", "multiplier", "timeLeft" },
  "game":  { "active", "name", "color": "#RRGGBB", "map", "round", "rounds", "timeLeft" },
  "teams": [ { "id", "name", "color", "hidden", "rank", "points", "gamePoints", "index",
               "members": [ { "uuid", "name", "online", "points" } ] } ],
  "live":  [ { "id", "points" } ],            // this game's standings, best first
  "you":   { "name", "spectator", "points", "place", "of",
             "gamePoints", "gamePlace", "gameOf",            // only during a game
             "tier", "events", "lifetime", "bestPlace", "overallRank", "overallOf" },  // from StatsStore
  "team":  { ...team fields..., "of", "behindTeam", "behindBy", "leadTeam", "leadBy",
             "gamePoints", "gamePlace", "gameOf", "tiedWith": ["teamId"] }
}
```
Timers are in seconds; the mod counts them down locally between updates.
Adding fields is safe (older mods ignore them). Bump `v` only for breaking changes.
