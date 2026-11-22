package com.mchorizon.render;

import com.google.gson.JsonElement;
import com.mchorizon.state.HudData;
import com.mojang.serialization.JsonOps;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;

import java.util.HashMap;
import java.util.Map;

/**
 * Draws a team's icon: the glyph the server uses (rendered with the server resource pack),
 * or a team-colored box when the team has no icon.
 */
public final class TeamIcons {
	private static final Map<String, Component> CACHE = new HashMap<>();
	private static final Component NONE = Component.empty();

	private TeamIcons() {}

	private static Component icon(HudData.Team t) {
		JsonElement json = t.icon;
		if (json == null || json.isJsonNull()) {
			return null;
		}
		String key = json.toString();
		Component c = CACHE.computeIfAbsent(key, k -> ComponentSerialization.CODEC.parse(JsonOps.INSTANCE, json)
				.result().map(x -> (Component) x).orElse(NONE));
		if (CACHE.size() > 64) {
			CACHE.clear();
		}
		return c == NONE || c.getString().isBlank() ? null : c;
	}

	/** Width the icon takes at scale 1 (for laying out text after it). */
	public static int width(HudData.Team t) {
		Component c = icon(t);
		return c == null ? 6 : Ui.font().width(c);
	}

	/** Small icon on a text row (about 8px tall). Returns how wide it was. */
	public static int drawSmall(GuiGraphics g, HudData.Team t, int x, int y) {
		Component c = icon(t);
		if (c == null) {
			Ui.swatch(g, x, y, Ui.color(t.color));
			return 6;
		}
		g.drawString(Ui.font(), c, x, y, 0xFFFFFFFF, false);
		return Ui.font().width(c);
	}

	/** Big icon filling a square of the given size (MCC-style team card). */
	public static void drawBig(GuiGraphics g, HudData.Team t, int x, int y, int size) {
		int tc = Ui.color(t.color);
		Component c = icon(t);
		if (c == null) {
			g.fill(x, y, x + size, y + size, (tc & 0x00FFFFFF) | 0x60000000);
			Ui.outline(g, x, y, x + size, y + size, tc);
			Ui.textCenter(g, t.name.isEmpty() ? "?" : t.name.substring(0, 1).toUpperCase(), x + size / 2, y + size / 2 - 4, tc);
			return;
		}
		int w = Math.max(1, Ui.font().width(c));
		int h = Ui.font().lineHeight;
		float scale = Math.min((float) size / w, (float) size / h);
		g.pose().pushMatrix();
		g.pose().translate(x + (size - w * scale) / 2F, y + (size - h * scale) / 2F);
		g.pose().scale(scale, scale);
		g.drawString(Ui.font(), c, 0, 0, 0xFFFFFFFF, false);
		g.pose().popMatrix();
	}
}
