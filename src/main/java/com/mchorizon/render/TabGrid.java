package com.mchorizon.render;

import com.mchorizon.state.HudData;
import com.mchorizon.state.HudState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

/** The team overview shown while holding Tab: every team, its rank, points and members. */
public final class TabGrid {
	private static final int CARD_W = 168;
	private static final int GAP = 4;
	/** Width of the two-column grid. */
	public static final int WIDTH = CARD_W * 2 + GAP + 8;
	private static final int MEMBER_ROW = 10;

	private TabGrid() {}

	public static boolean canRender() {
		return HudState.get() != null && !HudState.get().teams.isEmpty();
	}

	public static void render(GuiGraphics g, int screenW, int screenH) {
		HudData d = HudState.get();
		if (d == null || d.teams.isEmpty()) {
			return;
		}
		List<HudData.Team> teams = new ArrayList<>(d.teams);
		// Ranked teams first (by rank), hidden/unranked teams after in their normal order.
		teams.sort(Comparator.comparingInt((HudData.Team t) -> t.hidden || t.rank <= 0 ? 1 : 0)
				.thenComparingInt(t -> t.hidden || t.rank <= 0 ? t.index : t.rank));

		int columns = screenW >= CARD_W * 2 + GAP * 3 + 8 ? 2 : 1;
		int perColumn = (teams.size() + columns - 1) / columns;
		int totalW = columns * CARD_W + (columns - 1) * GAP + 8;
		int x0 = (screenW - totalW) / 2;

		// Card heights per column so both columns line up.
		int[] heights = new int[teams.size()];
		for (int i = 0; i < teams.size(); i++) {
			heights[i] = cardHeight(teams.get(i));
		}
		int colHeight = 0;
		for (int col = 0; col < columns; col++) {
			int h = 0;
			for (int i = col * perColumn; i < Math.min(teams.size(), (col + 1) * perColumn); i++) {
				h += heights[i] + GAP;
			}
			colHeight = Math.max(colHeight, h);
		}
		int titleH = 24;
		int totalH = titleH + colHeight + 4;
		int y0 = Math.max(4, Math.min(24, (screenH - totalH) / 2));

		// Frame + title
		g.fill(x0, y0, x0 + totalW, y0 + totalH, 0xD00A0A12);
		Ui.outline(g, x0, y0, x0 + totalW, y0 + totalH, 0x60FFFFFF);
		String title = (d.event.title.isEmpty() ? d.event.name : d.event.title).toUpperCase();
		Ui.text(g, title, x0 + 6, y0 + 4, 0xFFFFC23D);
		String sub = d.game.active ? d.game.name.toUpperCase() : d.event.active ? d.event.phase.toUpperCase() : "TEAMS";
		int subColor = d.game.active ? Ui.color(d.game.color) : Ui.MUTED;
		Ui.textRight(g, sub, x0 + totalW - 6, y0 + 4, subColor);
		String line2 = d.event.active && d.event.rounds > 0 ? "Game " + d.event.round + " of " + d.event.rounds : "";
		if (d.game.active && !d.game.map.isEmpty()) {
			line2 += (line2.isEmpty() ? "" : "  ·  ") + "Map: " + d.game.map;
		}
		Ui.text(g, line2, x0 + 6, y0 + 14, Ui.MUTED);
		g.fill(x0 + 4, y0 + titleH - 1, x0 + totalW - 4, y0 + titleH, 0x40FFFFFF);

		UUID me = Minecraft.getInstance().player != null ? Minecraft.getInstance().player.getUUID() : null;
		for (int col = 0; col < columns; col++) {
			int cx = x0 + 4 + col * (CARD_W + GAP);
			int cy = y0 + titleH + 4;
			for (int i = col * perColumn; i < Math.min(teams.size(), (col + 1) * perColumn); i++) {
				card(g, d, teams.get(i), cx, cy, heights[i], me);
				cy += heights[i] + GAP;
			}
		}
	}

	private static int cardHeight(HudData.Team t) {
		int rows = Math.max(1, (t.members.size() + 1) / 2);
		return Ui.HEADER + 2 + rows * MEMBER_ROW + 2;
	}

	private static void card(GuiGraphics g, HudData d, HudData.Team t, int x, int y, int h, UUID me) {
		int tc = Ui.color(t.color);
		boolean mine = t.id.equals(d.team.id);
		g.fill(x, y, x + CARD_W, y + h, 0xB0161620);
		g.fill(x, y, x + CARD_W, y + Ui.HEADER, (tc & 0x00FFFFFF) | 0x50000000);
		g.fill(x, y, x + 2, y + h, tc);
		if (mine) {
			Ui.outline(g, x - 1, y - 1, x + CARD_W + 1, y + h + 1, 0xFFFFC23D);
		}

		String rank = t.hidden || t.rank <= 0 ? "?" : String.valueOf(t.rank);
		Ui.text(g, rank, x + 5, y + 2, 0xFFFFFFFF);
		int nx = x + 7 + Ui.width(rank) + 3;
		String pts = t.hidden ? "????" : Ui.number(t.points);
		String gameBit = d.game.active ? " +" + Ui.number(t.gamePoints) : "";
		int ptsW = Ui.width(pts) + Ui.width(gameBit);
		Ui.text(g, Ui.clip(t.name, CARD_W - (nx - x) - ptsW - 8), nx, y + 2, tc);
		Ui.textRight(g, pts, x + CARD_W - 4 - Ui.width(gameBit), y + 2, Ui.GOLD);
		if (!gameBit.isEmpty()) {
			Ui.textRight(g, gameBit, x + CARD_W - 4, y + 2, 0xFF55FF55);
		}

		int my = y + Ui.HEADER + 2;
		int half = (CARD_W - 8) / 2;
		for (int i = 0; i < t.members.size(); i++) {
			HudData.Member m = t.members.get(i);
			int mx = x + 5 + (i % 2) * half;
			int rowY = my + (i / 2) * MEMBER_ROW;
			UUID id = parse(m.uuid);
			Ui.head(g, m.online ? id : null, mx, rowY, 8);
			int color = !m.online ? 0xFF5A5A66 : id != null && id.equals(me) ? 0xFFFFE066 : 0xFFE6E6EE;
			Ui.text(g, Ui.clip(m.name, half - 12), mx + 10, rowY, color);
		}
		if (t.members.isEmpty()) {
			Ui.text(g, "No players", x + 5, my, Ui.MUTED);
		}
	}

	private static UUID parse(String s) {
		try {
			return UUID.fromString(s);
		} catch (Exception e) {
			return null;
		}
	}
}
