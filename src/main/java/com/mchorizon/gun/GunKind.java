package com.mchorizon.gun;

import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import org.joml.Vector3f;

/**
 * The two MCEvents paint guns and where things are on their 3D models (model pixels, 0..16 box,
 * barrel toward -Z), plus the first-person display transform from the resource pack. The renderer
 * uses these to put the hands on the grips and to line the sights up with the middle of the screen.
 */
public enum GunKind {
	//           display translation (px)   yaw  scale  right-hand grip             left hand                  sight (rifle: the red dot)
	RIFLE (new Vector3f(1.5F, 3F, 0F),   12F, 0.5F,  new Vector3f(8F, 3.2F, 11.2F), new Vector3f(8F, 4.2F, -4F)  , new Vector3f(8F, 13.95F, 5.45F)),
	PISTOL(new Vector3f(-1.5F, 4F, -1F), 15F, 0.65F, new Vector3f(8F, 4.3F, 8.2F),  new Vector3f(5.4F, 3.8F, 8.0F), new Vector3f(8F, 11.6F, 8.2F));

	final Vector3f translation;
	final float yaw, scale;
	final Vector3f grip, support, sight;

	GunKind(Vector3f translation, float yaw, float scale, Vector3f grip, Vector3f support, Vector3f sight) {
		this.translation = translation;
		this.yaw = yaw;
		this.scale = scale;
		this.grip = grip;
		this.support = support;
		this.sight = sight;
	}

	/** Where a model point ends up after the item's first-person display transform (before the hand transform). */
	Vector3f place(Vector3f modelPx) {
		Vector3f v = new Vector3f(modelPx).div(16F).sub(0.5F, 0.5F, 0.5F).mul(scale);
		v.rotateY((float) Math.toRadians(yaw));
		return v.add(translation.x / 16F, translation.y / 16F, translation.z / 16F);
	}

	private static ItemStack lastStack;
	private static GunKind lastKind;

	/** The paint gun this item is (MCEvents marks them with the mcevents:paint_gun item tag), or null. */
	public static GunKind of(ItemStack stack) {
		if (stack == null || stack.isEmpty()) {
			return null;
		}
		if (stack == lastStack) {
			return lastKind;
		}
		GunKind kind = null;
		CustomData data = stack.get(DataComponents.CUSTOM_DATA);
		if (data != null) {
			CompoundTag bukkit = data.copyTag().getCompoundOrEmpty("PublicBukkitValues");
			String id = bukkit.getStringOr("mcevents:paint_gun", "");
			if (id.equalsIgnoreCase("RIFLE")) {
				kind = RIFLE;
			} else if (id.equalsIgnoreCase("PISTOL")) {
				kind = PISTOL;
			}
		}
		lastStack = stack;
		lastKind = kind;
		return kind;
	}
}
