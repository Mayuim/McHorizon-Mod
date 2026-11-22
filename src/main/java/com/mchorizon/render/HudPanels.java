package com.mchorizon.render;

import com.mchorizon.config.HorizonConfig;
import com.mchorizon.state.HudData;
import com.mchorizon.state.HudState;
import net.minecraft.client.gui.GuiGraphics;

import java.util.ArrayList;
import java.util.List;

/**
 * The always-on panels:
 *   left:  PROFILE, GAME
 *   right: YOUR TEAM, LIVE
 *   bottom right: EVENT info
 */
public final class HudPanels {
	private static final int W = 132;

	private HudPanels() {}

	public static void render(GuiGraphics g, int screenW, int screenH) {
		HudData d = HudState.get();
		HorizonConfig c = HorizonConfig.get();
		if (d == null || !c.hudEnabled) {
			return;
		}
		int x = 4;
		int y = 4;
		if (c.showProfile) {
			y = profile(g, d, x, y) + 4;
		}
		if (c.showGame && (d.game.active || d.event.active)) {
			game(g, d, x, y);
		}

		int rx = screenW - W - 4;
		int ry = 4;
		if (c.showYourTeam && !d.team.id.isEmpty()) {
			ry = yourTeam(g, d, rx, ry) + 4;
		}
		if (c.showLive && d.game.active && !d.live.isEmpty()) {
			live(g, d, rx, ry);
		}
		if (c.showEventInfo && d.event.active) {
			eventInfo(g, d, screenW, screenH);
		}
	}

	// ---------- PROFILE ----------

	private static int profile(GuiGraphics g, HudData d, int x, int y) {
		HudData.You you = d.you;
		boolean hasTier = you.tier != null && !you.tier.isEmpty();
		boolean hasStats = you.events != null;
		if (!hasTier && !hasStats) {
			return y - 4;
		}
		int rows = 2 + (hasStats ? 2 : 0);
		int h = Ui.HEADER + 3 + rows * Ui.ROW + 3;
		int cy = Ui.panel(g, x, y, W, h, "PROFILE", 0xFFFF77DD);

		// Tier badge
		String tier = hasTier ? you.tier : "-";
		int tc = Ui.tierColor(tier);
		g.fill(x + Ui.PAD, cy, x + Ui.PAD + 18, cy + 18, 0xFF1E1E2A);
		Ui.outline(g, x + Ui.PAD, cy, x + Ui.PAD + 18, cy + 18, tc);
		Ui.textCenter(g, tier, x + Ui.PAD + 9, cy + 5, tc);
		Ui.text(g, hasTier ? tier + " Tier" : "Unranked", x + Ui.PAD + 23, cy, tc);
		if (you.overallRank != null && you.overallRank > 0) {
			Ui.text(g, "#" + you.overallRank + " of " + you.overallOf, x + Ui.PAD + 23, cy + Ui.ROW, Ui.MUTED);
		}
		cy += 2 * Ui.ROW;
		if (hasStats) {
			Ui.row(g, x, W, cy, "Events played", Ui.LABEL, String.valueOf(you.events), Ui.VALUE);
			cy += Ui.ROW;
			Ui.row(g, x, W, cy, "Lifetime points", Ui.LABEL, Ui.number(you.lifetime == null ? 0 : you.lifetime), Ui.GOLD);
		}
		return y + h;
	}

	// ---------- GAME ----------

	private static int game(GuiGraphics g, HudData d, int x, int y) {
		HudData.You you = d.you;
		HudData.MyTeam team = d.team;
		boolean inGame = d.game.active;
		boolean onTeam = !team.id.isEmpty();

		List<HudData.Team> tied = new ArrayList<>();
		if (inGame && onTeam && team.tiedWith != null) {
			for (String id : team.tiedWith) {
				HudData.Team t = d.teamById(id);
				if (t != null) {
					tied.add(t);
				}
			}
		}

		int rows = 0;
		boolean showMap = inGame && !d.game.map.isEmpty();
		rows += showMap ? 1 : 0;
		rows += 1; // time
		boolean showYou = !you.spectator && you.of > 0;
		rows += showYou ? 2 : 0;
		rows += onTeam ? 2 : 0;
		rows += tied.isEmpty() ? 0 : tied.size();
		int gaps = (showYou ? 1 : 0) + (onTeam ? 1 : 0);
		int h = Ui.HEADER + 3 + rows * Ui.ROW + gaps * 3 + 2;

		String title = inGame && !d.game.name.isEmpty() ? d.game.name.toUpperCase() : d.event.phase.toUpperCase();
		int accent = inGame ? Ui.color(d.game.color) : 0xFFFFC23D;
		int cy = Ui.panel(g, x, y, W, h, Ui.clip(title, W - 8), accent);

		if (showMap) {
			Ui.row(g, x, W, cy, "Map", Ui.LABEL, Ui.clip(d.game.map.toUpperCase(), W / 2), Ui.VALUE);
			cy += Ui.ROW;
		}
		boolean paused = d.event.paused;
		int seconds = inGame ? HudState.tick(d.game.timeLeft, paused) : HudState.tick(d.event.timeLeft, paused);
		String label = inGame ? "Time left" : d.event.phase;
		Ui.row(g, x, W, cy, label, Ui.LABEL, paused ? "PAUSED" : Ui.time(seconds),
				seconds <= 10 && !paused ? 0xFFFF5555 : Ui.VALUE);
		cy += Ui.ROW;

		if (showYou) {
			cy += 3;
			int place = inGame && you.gamePlace != null ? you.gamePlace : you.place;
			int of = inGame && you.gameOf != null ? you.gameOf : you.of;
			int pts = inGame && you.gamePoints != null ? you.gamePoints : you.points;
			Ui.row(g, x, W, cy, "YOU", 0xFF55FFFF, "#" + place + " / " + of, Ui.LABEL);
			cy += Ui.ROW;
			Ui.row(g, x, W, cy, "Points", Ui.LABEL, Ui.number(pts), Ui.GOLD);
			cy += Ui.ROW;
		}

		if (onTeam) {
			cy += 3;
			int tc = Ui.color(team.color);
			int place = inGame && team.gamePlace != null ? team.gamePlace : team.rank;
			int of = inGame && team.gameOf != null ? team.gameOf : team.of;
			int pts = inGame ? team.gamePoints : team.points;
			Ui.swatch(g, x + Ui.PAD, cy, tc);
			Ui.text(g, "TEAM", x + Ui.PAD + 9, cy, tc);
			Ui.textRight(g, team.hidden ? "#? / " + of : "#" + place + " / " + of, x + W - Ui.PAD, cy, Ui.LABEL);
			cy += Ui.ROW;
			Ui.row(g, x, W, cy, "Team points", Ui.LABEL, team.hidden && !inGame ? "????" : Ui.number(pts), Ui.GOLD);
			cy += Ui.ROW;
			boolean first = true;
			for (HudData.Team t : tied) {
				if (first) {
					Ui.text(g, "Tied with", x + Ui.PAD, cy, 0xFFFFAA55);
					first = false;
				}
				int c2 = Ui.color(t.color);
				String name = Ui.clip(t.name, W / 2 - 4);
				Ui.textRight(g, name, x + W - Ui.PAD, cy, c2);
				Ui.swatch(g, x + W - Ui.PAD - Ui.width(name) - 9, cy, c2);
				cy += Ui.ROW;
			}
		}
		return y + h;
	}

	// ---------- YOUR TEAM ----------

	private static int yourTeam(GuiGraphics g, HudData d, int x, int y) {
		HudData.MyTeam t = d.team;
		int tc = Ui.color(t.color);
		boolean extra = (t.behindTeam != null && t.behindBy != null) || (t.leadTeam != null && t.leadBy != null);
		int rows = 2 + (extra ? 1 : 0);
		int h = Ui.HEADER + 3 + rows * Ui.ROW + 2;
		int cy = Ui.panel(g, x, y, W, h, "YOUR TEAM", tc);

		Ui.swatch(g, x + Ui.PAD, cy, tc);
		String pts = t.hidden ? "????" : Ui.number(t.points);
		Ui.text(g, Ui.clip(t.name, W - Ui.width(pts) - 22), x + Ui.PAD + 9, cy, tc);
		Ui.textRight(g, pts, x + W - Ui.PAD, cy, Ui.GOLD);
		cy += Ui.ROW;

		String place = t.hidden || t.rank <= 0 ? "Hidden" : Ui.ordinal(t.rank) + " place";
		Ui.text(g, place, x + Ui.PAD, cy, Ui.LABEL);
		if (t.of > 0) {
			Ui.textRight(g, "of " + t.of, x + W - Ui.PAD, cy, Ui.MUTED);
		}
		cy += Ui.ROW;

		if (t.behindTeam != null && t.behindBy != null) {
			HudData.Team ahead = d.teamById(t.behindTeam);
			String name = ahead == null ? t.behindTeam : ahead.name;
			Ui.text(g, Ui.number(t.behindBy) + " behind ", x + Ui.PAD, cy, Ui.MUTED);
			int nx = x + Ui.PAD + Ui.width(Ui.number(t.behindBy) + " behind ");
			Ui.text(g, Ui.clip(name, x + W - Ui.PAD - nx), nx, cy, ahead == null ? Ui.LABEL : Ui.color(ahead.color));
		} else if (t.leadTeam != null && t.leadBy != null) {
			HudData.Team next = d.teamById(t.leadTeam);
			String name = next == null ? t.leadTeam : next.name;
			Ui.text(g, Ui.number(t.leadBy) + " ahead of ", x + Ui.PAD, cy, 0xFF55FF55);
			int nx = x + Ui.PAD + Ui.width(Ui.number(t.leadBy) + " ahead of ");
			Ui.text(g, Ui.clip(name, x + W - Ui.PAD - nx), nx, cy, next == null ? Ui.LABEL : Ui.color(next.color));
		}
		return y + h;
	}

	// ---------- LIVE ----------

	private static int live(GuiGraphics g, HudData d, int x, int y) {
		int h = Ui.HEADER + 3 + d.live.size() * Ui.ROW + 2;
		long now = System.currentTimeMillis();
		boolean blink = (now / 600) % 2 == 0;
		int cy = Ui.panel(g, x, y, W, h, (blink ? "● " : "  ") + "LIVE", 0xFFFF5555);
		for (HudData.LiveRow row : d.live) {
			HudData.Team t = d.teamById(row.id);
			if (t == null) {
				continue;
			}
			int tc = Ui.color(t.color);
			boolean mine = row.id.equals(d.team.id);
			if (mine) {
				g.fill(x + 2, cy - 1, x + W - 2, cy + Ui.ROW - 1, 0x40FFFFFF);
				Ui.outline(g, x + 2, cy - 1, x + W - 2, cy + Ui.ROW - 1, tc);
			}
			Ui.swatch(g, x + Ui.PAD, cy, tc);
			String pts = "+" + Ui.number(row.points);
			Ui.text(g, Ui.clip(t.name, W - Ui.width(pts) - 22), x + Ui.PAD + 9, cy, tc);
			Ui.textRight(g, pts, x + W - Ui.PAD, cy, Ui.GOLD);
			cy += Ui.ROW;
		}
		return y + h;
	}

	// ---------- EVENT info (bottom right) ----------

	private static void eventInfo(GuiGraphics g, HudData d, int screenW, int screenH) {
		String title = (d.event.title.isEmpty() ? d.event.name : d.event.title).toUpperCase();
		if (d.event.mini) {
			title += " · MINI";
		}
		List<String> lines = new ArrayList<>();
		if (d.event.rounds > 0) {
			lines.add("Game " + d.event.round + "/" + d.event.rounds + (d.event.multiplier.isEmpty() ? "" : "  ·  " + d.event.multiplier));
		}
		if (d.game.active && d.game.rounds > 1) {
			lines.add("Round " + d.game.round + "/" + d.game.rounds);
		}
		int w = Ui.width(title) + 12;
		for (String l : lines) {
			w = Math.max(w, Ui.width(l) + 12);
		}
		int h = 6 + Ui.ROW * (1 + lines.size());
		int x = screenW - w - 4;
		int y = screenH - h - 4;
		g.fill(x, y, x + w, y + h, Ui.BG);
		g.fill(x, y, x + 2, y + h, 0xFFFFC23D);
		int cy = y + 3;
		Ui.text(g, title, x + 6, cy, 0xFFFFC23D);
		for (String l : lines) {
			cy += Ui.ROW;
			Ui.text(g, l, x + 6, cy, Ui.MUTED);
		}
	}
}
