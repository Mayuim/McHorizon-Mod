package com.mchorizon.gun;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import net.fabricmc.loader.api.FabricLoader;

import java.io.Reader;
import java.io.Writer;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Every number that shapes how the guns and arms sit on screen. Tweak them live in game with
 * /gunpose (positions in blocks from your eyes: +x right, +y up, -z forward; angles in degrees),
 * then /gunpose save writes config/mchorizon-gun.json.
 */
public final class GunTuning {
	// hip-fire position of the gun (the vanilla hand spot)
	public static float hipX = 0.56F, hipY = -0.52F, hipZ = -0.72F;
	// aim down sights: where the sight lands (0,0 = the middle of the screen)
	public static float aimX = 0F, aimY = -0.025F, aimZ = -0.42F;
	/** Ticks to bring the gun up to your eye. */
	public static float aimTicks = 4F;
	// shoulders the arms come from
	public static float rightShoulderX = 0.75F, rightShoulderY = -1.0F, rightShoulderZ = 0.1F;
	public static float leftShoulderX = -0.25F, leftShoulderY = -0.95F, leftShoulderZ = 0.05F;
	/** Twist of each arm around itself. */
	public static float rightArmRoll = 0F, leftArmRoll = 0F;
	// recoil
	public static float kickBack = 0.07F, kickUp = 0.015F, kickPitch = 7F, kickRecover = 0.55F, kickAimScale = 0.5F;
	// reload: tilt the gun and take the left hand off to load a pod
	public static float reloadRoll = 30F, reloadPitch = 14F, reloadDrop = 0.12F;
	public static float podX = 0.05F, podY = -0.75F, podZ = -0.45F;
	// sprinting: gun held across the chest
	public static float sprintYaw = 28F, sprintPitch = -10F, sprintDropY = -0.08F, sprintX = -0.06F;
	// pulling the gun out after switching
	public static float equipTicks = 5F, equipDrop = 0.45F, equipPitch = -30F;
	// breathing while standing still
	public static float breathe = 0.006F;

	private static final Map<String, Float> DEFAULTS = new LinkedHashMap<>();
	private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

	static {
		for (Field f : fields()) {
			try {
				DEFAULTS.put(f.getName(), f.getFloat(null));
			} catch (IllegalAccessException ignored) {
			}
		}
	}

	private GunTuning() {
	}

	public static List<Field> fields() {
		List<Field> out = new ArrayList<>();
		for (Field f : GunTuning.class.getFields()) {
			if (f.getType() == float.class && Modifier.isStatic(f.getModifiers())) {
				out.add(f);
			}
		}
		return out;
	}

	public static List<String> names() {
		return new ArrayList<>(DEFAULTS.keySet());
	}

	public static Float get(String name) {
		try {
			return GunTuning.class.getField(name).getFloat(null);
		} catch (ReflectiveOperationException e) {
			return null;
		}
	}

	public static boolean set(String name, float value) {
		try {
			Field f = GunTuning.class.getField(name);
			if (f.getType() != float.class) {
				return false;
			}
			f.setFloat(null, value);
			return true;
		} catch (ReflectiveOperationException e) {
			return false;
		}
	}

	public static void reset() {
		DEFAULTS.forEach(GunTuning::set);
	}

	private static Path file() {
		return FabricLoader.getInstance().getConfigDir().resolve("mchorizon-gun.json");
	}

	public static void load() {
		Path p = file();
		if (!Files.exists(p)) {
			return;
		}
		try (Reader r = Files.newBufferedReader(p)) {
			JsonObject o = GSON.fromJson(r, JsonObject.class);
			if (o != null) {
				for (String k : o.keySet()) {
					set(k, o.get(k).getAsFloat());
				}
			}
		} catch (Exception e) {
			System.err.println("[MC Horizons] Couldn't read " + p + ": " + e);
		}
	}

	public static boolean save() {
		JsonObject o = new JsonObject();
		for (String k : DEFAULTS.keySet()) {
			o.addProperty(k, get(k));
		}
		try (Writer w = Files.newBufferedWriter(file())) {
			GSON.toJson(o, w);
			return true;
		} catch (Exception e) {
			return false;
		}
	}
}
