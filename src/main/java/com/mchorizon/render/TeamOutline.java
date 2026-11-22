package com.mchorizon.render;

import com.mchorizon.config.HorizonConfig;
import com.mchorizon.state.HudData;
import com.mchorizon.state.HudState;
import net.minecraft.client.gui.GuiGraphics;

import java.util.ArrayList;
import java.util.List;

/**
 * Gold outline around your team in the server's own tab list. MCEvents sends where the
 * team's block sits (which header lines, which column); we line that up with where the
 * client actually drew those lines.
 */
public final class TeamOutline {
	private static final int GOLD = 0xFFFFC23D;
	private static final List<int[]> lines = new ArrayList<>(); // {x, y, width} in draw order
	private static GuiGraphics graphics;

	private TeamOutline() {}

	public static void begin() {
		lines.clear();
		graphics = null;
	}

	public static void line(GuiGraphics g, int x, int y, int width) {
		graphics = g;
		lines.add(new int[]{x, y, width});
	}

	public static void draw() {
		GuiGraphics g = graphics;
		HudData d = HudState.get();
		HorizonConfig c = HorizonConfig.get();
		if (g == null || d == null || !c.hudEnabled || !c.outlineTeamInTab) {
			return;
		}
		HudData.TabBox box = d.team.tabBox;
		if (box == null || box.lines <= 0 || box.lineWidth <= 0) {
			return;
		}
		int last = box.line + box.lines - 1;
		if (box.line < 0 || last >= lines.size()) {
			return;
		}
		int[] first = lines.get(box.line);
		int[] end = lines.get(last);
		// The server estimates text widths; scale to what the client really drew.
		double scale = first[2] > 0 ? (double) first[2] / box.lineWidth : 1.0;
		int left = first[0] + (int) Math.round(box.x * scale) - 3;
		int right = first[0] + (int) Math.round((box.x + box.width) * scale) + 3;
		int top = first[1] - 2;
		int bottom = end[1] + 9 + 1;
		Ui.outline(g, left, top, right, bottom, GOLD);
	}
}
