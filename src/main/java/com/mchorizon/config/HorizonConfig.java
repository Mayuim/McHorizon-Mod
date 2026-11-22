package com.mchorizon.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;

import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;

/** .minecraft/config/mchorizon.json */
public class HorizonConfig {
	private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
	private static final Path FILE = FabricLoader.getInstance().getConfigDir().resolve("mchorizon.json");
	private static HorizonConfig instance = new HorizonConfig();

	public boolean hudEnabled = true;
	/** true = panels only while holding Tab (like MCC). false = panels always on screen. */
	public boolean panelsOnlyOnTab = true;
	/** Size of the panels (1.0 = normal GUI scale). */
	public float hudScale = 1.0F;
	/** Hide the server's sidebar while the panels show the same info. */
	public boolean hideSidebar = true;
	/** Show the mod's own team overview instead of the server's tab list when holding Tab. */
	public boolean replaceTabList = false;
	/** Draw a gold outline around your team in the server's tab list. */
	public boolean outlineTeamInTab = true;
	/** Bumped when defaults change, so older config files get the new defaults once. */
	public int configVersion;
	public boolean showProfile = true;
	public boolean showGame = true;
	public boolean showYourTeam = true;
	public boolean showLive = true;
	public boolean showEventInfo = true;

	public static HorizonConfig get() {
		return instance;
	}

	public static void load() {
		if (Files.exists(FILE)) {
			try (Reader r = Files.newBufferedReader(FILE)) {
				HorizonConfig c = GSON.fromJson(r, HorizonConfig.class);
				if (c != null) {
					instance = c;
				}
			} catch (Exception ignored) {
				// keep defaults
			}
		}
		if (instance.configVersion < 2) {
			// v2: keep the server's tab list and outline your team instead of replacing it
			instance.replaceTabList = false;
			instance.outlineTeamInTab = true;
			instance.configVersion = 2;
		}
		if (instance.hudScale < 0.5F || instance.hudScale > 2.0F) {
			instance.hudScale = 1.0F;
		}
		save();
	}

	public static void save() {
		try {
			Files.createDirectories(FILE.getParent());
			try (Writer w = Files.newBufferedWriter(FILE)) {
				GSON.toJson(instance, w);
			}
		} catch (Exception ignored) {
			// not critical
		}
	}
}
