package com.mchorizon.gun;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.util.Mth;

/**
 * What the server told us about your paint gun (aiming, shots, reloads), turned into smooth
 * animation values. Values move once per client tick and the renderer blends between the last
 * two ticks, so everything stays smooth at any frame rate.
 */
public final class GunState {
	private static boolean serverAim;
	private static int reloadTotal, reloadLeft;
	private static boolean pendingShot;
	private static GunKind heldKind;
	private static int heldSlot = -1;

	// animation values: previous tick and this tick
	static float aim, aimO, kick, kickO, reload, reloadO, sprint, sprintO, equip = 1F, equipO = 1F;
	static int ticks;

	private GunState() {
	}

	/** Messages from MCEvents (see PROTOCOL.md). */
	public static void handle(String json) {
		try {
			JsonObject o = JsonParser.parseString(json).getAsJsonObject();
			if (o.has("aim")) {
				serverAim = o.get("aim").getAsBoolean();
			}
			if (o.has("shot") && o.get("shot").getAsBoolean()) {
				pendingShot = true;
			}
			if (o.has("reload")) {
				int len = o.get("reload").getAsInt();
				int left = o.has("left") ? o.get("left").getAsInt() : len;
				reloadTotal = Math.max(0, len);
				reloadLeft = reloadTotal == 0 ? 0 : Mth.clamp(left, 0, reloadTotal);
			}
			if (o.has("reset") && o.get("reset").getAsBoolean()) {
				clear();
			}
		} catch (Exception ignored) {
			// a bad message just gets skipped
		}
	}

	public static void clear() {
		serverAim = false;
		reloadTotal = 0;
		reloadLeft = 0;
		pendingShot = false;
		heldKind = null;
		heldSlot = -1;
		aim = aimO = kick = kickO = reload = reloadO = sprint = sprintO = 0F;
		equip = equipO = 1F;
	}

	public static void tick(Minecraft mc) {
		LocalPlayer p = mc.player;
		aimO = aim;
		kickO = kick;
		reloadO = reload;
		sprintO = sprint;
		equipO = equip;
		ticks++;
		if (p == null) {
			clear();
			return;
		}
		GunKind kind = GunKind.of(p.getMainHandItem());
		int slot = p.getInventory().getSelectedSlot();
		if (kind == null) {
			heldKind = null;
			heldSlot = slot;
			serverAim = false;
			aim = aimO = 0F;
			return;
		}
		if (kind != heldKind || slot != heldSlot) {
			// just pulled this gun out: raise it, start un-aimed
			heldKind = kind;
			heldSlot = slot;
			equip = equipO = 0F;
			aim = aimO = 0F;
		}

		if (reloadLeft > 0) {
			reloadLeft--;
		}
		boolean reloading = reloadTotal > 0 && reloadLeft > 0;
		reload = reloading ? 1F - reloadLeft / (float) reloadTotal : 0F;
		if (!reloading) {
			reloadTotal = 0;
		}

		boolean wantAim = (serverAim || p.isShiftKeyDown()) && !reloading;
		float aimStep = 1F / Math.max(1F, GunTuning.aimTicks);
		aim = Mth.clamp(aim + (wantAim ? aimStep : -aimStep), 0F, 1F);

		kick *= GunTuning.kickRecover;
		if (pendingShot) {
			kick = 1F;
			pendingShot = false;
		}

		boolean sprinting = p.isSprinting() && !wantAim && !reloading;
		sprint = Mth.clamp(sprint + (sprinting ? 0.2F : -0.25F), 0F, 1F);
		equip = Mth.clamp(equip + 1F / Math.max(1F, GunTuning.equipTicks), 0F, 1F);
	}

	static float lerp(float partial, float o, float n) {
		return o + (n - o) * partial;
	}

	static float smooth(float t) {
		t = Mth.clamp(t, 0F, 1F);
		return t * t * (3F - 2F * t);
	}
}
