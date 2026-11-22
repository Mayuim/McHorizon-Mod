package com.mchorizon.render;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.PlayerFaceRenderer;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.multiplayer.PlayerInfo;

import java.util.Locale;
import java.util.UUID;

/** Shared look for every panel: dark glass background, colored header, label/value rows. */
public final class Ui {
	public static final int BG = 0xC80C0C14;
	public static final int HEADER_BG = 0xE0161622;
	public static final int LABEL = 0xFFD0D0D8;
	public static final int VALUE = 0xFFFFE066;
	public static final int MUTED = 0xFF8A8A99;
	public static final int GOLD = 0xFFFFC23D;
	public static final int ROW = 10;
	public static final int HEADER = 12;
	public static final int PAD = 4;

	private Ui() {}

	public static Font font() {
		return Minecraft.getInstance().font;
	}

	public static int width(String s) {
		return font().width(s);
	}

	public static void text(GuiGraphics g, String s, int x, int y, int color) {
		g.drawString(font(), s, x, y, color, true);
	}

	public static void textRight(GuiGraphics g, String s, int rightX, int y, int color) {
		text(g, s, rightX - width(s), y, color);
	}

	public static void textCenter(GuiGraphics g, String s, int centerX, int y, int color) {
		text(g, s, centerX - width(s) / 2, y, color);
	}

	/** Panel with a header strip. Returns the y where content starts. */
	public static int panel(GuiGraphics g, int x, int y, int w, int h, String title, int accent) {
		g.fill(x, y, x + w, y + h, BG);
		g.fill(x, y, x + w, y + HEADER, HEADER_BG);
		g.fill(x, y + HEADER - 1, x + w, y + HEADER, accent);
		textCenter(g, title, x + w / 2, y + 2, accent);
		return y + HEADER + 3;
	}

	public static void outline(GuiGraphics g, int x1, int y1, int x2, int y2, int color) {
		g.fill(x1, y1, x2, y1 + 1, color);
		g.fill(x1, y2 - 1, x2, y2, color);
		g.fill(x1, y1, x1 + 1, y2, color);
		g.fill(x2 - 1, y1, x2, y2, color);
	}

	/** Label on the left, value on the right. */
	public static void row(GuiGraphics g, int x, int w, int y, String label, int labelColor, String value, int valueColor) {
		text(g, label, x + PAD, y, labelColor);
		textRight(g, value, x + w - PAD, y, valueColor);
	}

	/** A little colored square, used as a team marker. */
	public static void swatch(GuiGraphics g, int x, int y, int color) {
		g.fill(x, y + 1, x + 6, y + 7, color);
		g.fill(x + 1, y + 2, x + 5, y + 6, (color & 0x00FFFFFF) | 0xFF000000);
	}

	/** Player face from the tab list skin, or a gray square if they're offline. */
	public static void head(GuiGraphics g, UUID id, int x, int y, int size) {
		ClientPacketListener conn = Minecraft.getInstance().getConnection();
		PlayerInfo info = conn == null || id == null ? null : conn.getPlayerInfo(id);
		if (info == null) {
			g.fill(x, y, x + size, y + size, 0xFF3A3A44);
			return;
		}
		PlayerFaceRenderer.draw(g, info.getSkin(), x, y, size);
	}

	public static int color(String hex) {
		try {
			String h = hex.startsWith("#") ? hex.substring(1) : hex;
			return 0xFF000000 | Integer.parseInt(h, 16);
		} catch (Exception e) {
			return 0xFFFFFFFF;
		}
	}

	public static String number(long n) {
		return String.format(Locale.US, "%,d", n);
	}

	public static String ordinal(int n) {
		int mod100 = n % 100;
		String suffix = mod100 >= 11 && mod100 <= 13 ? "th" : switch (n % 10) {
			case 1 -> "st";
			case 2 -> "nd";
			case 3 -> "rd";
			default -> "th";
		};
		return n + suffix;
	}

	public static String time(int seconds) {
		seconds = Math.max(0, seconds);
		return String.format("%02d:%02d", seconds / 60, seconds % 60);
	}

	public static int tierColor(String tier) {
		return switch (tier == null ? "" : tier.toUpperCase(Locale.ROOT)) {
			case "S" -> 0xFFFFC23D;
			case "A" -> 0xFFFF5555;
			case "B" -> 0xFFFF9933;
			case "C" -> 0xFFFFFF55;
			case "D" -> 0xFF55FF55;
			case "E" -> 0xFF55FFFF;
			case "F" -> 0xFFAAAAAA;
			default -> 0xFF8A8A99;
		};
	}

	/** "Lime Llamas" -> keeps it short for tight rows. */
	public static String clip(String s, int maxWidth) {
		if (width(s) <= maxWidth) {
			return s;
		}
		String out = s;
		while (out.length() > 1 && width(out + "…") > maxWidth) {
			out = out.substring(0, out.length() - 1);
		}
		return out + "…";
	}
}
