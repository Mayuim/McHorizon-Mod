package com.mchorizon.gun;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.ItemInHandRenderer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.entity.player.AvatarRenderer;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.PlayerModelPart;
import net.minecraft.world.entity.player.PlayerModelType;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;

/**
 * First-person paint guns: your own skin's arms holding the gun, smooth aim down sights,
 * recoil, a reload animation, sprint carry and a quick raise when you switch.
 * Replaces the vanilla hand rendering only while you hold a paint gun.
 */
public final class GunRenderer {
	// the player model's arm: pivot (shoulder) and the middle of the hand, in model pixels
	private static final float ARM_PIVOT_X = 5F, ARM_PIVOT_Y = 2F, HAND_Y = 8.5F;

	private GunRenderer() {
	}

	/** @return true if we drew this hand (so vanilla shouldn't). */
	public static boolean render(ItemInHandRenderer self, EntityRenderDispatcher dispatcher, AbstractClientPlayer player,
			float partial, InteractionHand hand, PoseStack pose, SubmitNodeCollector collector, int light) {
		ItemStack main = player.getMainHandItem();
		GunKind kind = GunKind.of(main);
		if (kind == null) {
			return false;
		}
		if (hand != InteractionHand.MAIN_HAND) {
			return true;   // both arms are drawn with the gun; nothing in the off hand
		}

		float aim = GunState.smooth(GunState.lerp(partial, GunState.aimO, GunState.aim));
		float kick = GunState.lerp(partial, GunState.kickO, GunState.kick);
		float rl = GunState.lerp(partial, GunState.reloadO, GunState.reload);
		float sprint = GunState.smooth(GunState.lerp(partial, GunState.sprintO, GunState.sprint));
		float equip = GunState.lerp(partial, GunState.equipO, GunState.equip);
		float time = GunState.ticks + partial;

		// reload: ease in, hold, ease out
		float rEnv = rl <= 0F ? 0F : GunState.smooth(rl / 0.18F) * (1F - GunState.smooth((rl - 0.8F) / 0.2F));
		float kickAmt = kick * (1F - aim * (1F - GunTuning.kickAimScale));
		float up = 1F - equip;
		up = up * up;

		Vector3f grip = kind.place(kind.grip);
		Vector3f sight = kind.place(kind.sight);

		Matrix4f m = new Matrix4f();
		// camera-space offsets
		float breathe = Mth.sin(time * 0.08F) * GunTuning.breathe * (1F - aim * 0.8F);
		m.translate(sprint * GunTuning.sprintX,
				breathe + kickAmt * GunTuning.kickUp - rEnv * GunTuning.reloadDrop - up * GunTuning.equipDrop + sprint * GunTuning.sprintDropY,
				kickAmt * GunTuning.kickBack);
		// hip -> aim: slide the sight line onto the middle of the screen and straighten the barrel
		m.translate(Mth.lerp(aim, GunTuning.hipX, GunTuning.aimX), Mth.lerp(aim, GunTuning.hipY, GunTuning.aimY),
				Mth.lerp(aim, GunTuning.hipZ, GunTuning.aimZ));
		m.rotateY((float) Math.toRadians(-kind.yaw * aim));
		m.translate(-sight.x * aim, -sight.y * aim, -sight.z * aim);
		// tilts around the grip: recoil, reload, sprint, raise
		m.translate(grip);
		m.rotateY((float) Math.toRadians(GunTuning.sprintYaw * sprint));
		m.rotateX((float) Math.toRadians(GunTuning.kickPitch * kickAmt + GunTuning.reloadPitch * rEnv
				+ GunTuning.sprintPitch * sprint + GunTuning.equipPitch * up));
		m.rotateZ((float) Math.toRadians(GunTuning.reloadRoll * rEnv));
		m.translate(-grip.x, -grip.y, -grip.z);

		pose.pushPose();
		pose.mulPose(m);
		self.renderItem(player, main, ItemDisplayContext.FIRST_PERSON_RIGHT_HAND, pose, collector, light);
		pose.popPose();

		if (player.isInvisible()) {
			return true;
		}

		// arms: hands on the grips, reaching back to the shoulders
		Vector3f rightHand = m.transformPosition(new Vector3f(grip));
		Vector3f leftHand = m.transformPosition(kind.place(kind.support));
		// reload: the left hand drops to grab a paint pod and comes back
		float off = rl <= 0F ? 0F : GunState.smooth((rl - 0.12F) / 0.2F) * (1F - GunState.smooth((rl - 0.62F) / 0.22F));
		leftHand.lerp(new Vector3f(GunTuning.podX, GunTuning.podY, GunTuning.podZ), off);

		AvatarRenderer<AbstractClientPlayer> avatar = dispatcher.getPlayerRenderer(player);
		Identifier skin = player.getSkin().body().texturePath();
		boolean slim = player.getSkin().model() == PlayerModelType.SLIM;
		arm(pose, collector, light, avatar, skin, slim, true, rightHand,
				new Vector3f(GunTuning.rightShoulderX, GunTuning.rightShoulderY, GunTuning.rightShoulderZ), GunTuning.rightArmRoll,
				player.isModelPartShown(PlayerModelPart.RIGHT_SLEEVE));
		arm(pose, collector, light, avatar, skin, slim, false, leftHand,
				new Vector3f(GunTuning.leftShoulderX, GunTuning.leftShoulderY, GunTuning.leftShoulderZ), GunTuning.leftArmRoll,
				player.isModelPartShown(PlayerModelPart.LEFT_SLEEVE));
		return true;
	}

	/** Draws one arm of the player's skin with its hand at {@code hand}, pointing back to {@code shoulder}. */
	private static void arm(PoseStack pose, SubmitNodeCollector collector, int light, AvatarRenderer<AbstractClientPlayer> avatar,
			Identifier skin, boolean slim, boolean right, Vector3f hand, Vector3f shoulder, float rollDeg, boolean sleeve) {
		float side = right ? -1F : 1F;
		float pivotX = side * ARM_PIVOT_X / 16F;
		float pivotY = (slim ? ARM_PIVOT_Y + 0.5F : ARM_PIVOT_Y) / 16F;
		float handX = side * (ARM_PIVOT_X + (slim ? 0.5F : 1F)) / 16F;
		float handY = (ARM_PIVOT_Y + HAND_Y) / 16F;

		Vector3f dir = new Vector3f(hand).sub(shoulder);
		if (dir.lengthSquared() < 1.0E-6F) {
			return;
		}
		dir.normalize();
		Quaternionf q = new Quaternionf().rotationTo(0F, 1F, 0F, dir.x, dir.y, dir.z);
		if (rollDeg != 0F) {
			q.premul(new Quaternionf().rotationAxis((float) Math.toRadians(rollDeg), dir.x, dir.y, dir.z));
		}

		pose.pushPose();
		pose.translate(hand.x, hand.y, hand.z);
		pose.mulPose(q);
		// thinner than vanilla so the hand wraps the grip instead of swallowing the gun
		pose.scale(GunTuning.armThickness, GunTuning.armLength, GunTuning.armThickness);
		pose.translate(-handX, -handY, 0F);
		// the vanilla first-person arm leans 0.1 rad outward; undo that so the hand lands on the grip
		pose.translate(pivotX, pivotY, 0F);
		pose.mulPose(Axis.ZP.rotation(right ? -0.1F : 0.1F));
		pose.translate(-pivotX, -pivotY, 0F);
		if (right) {
			avatar.renderRightHand(pose, collector, light, skin, sleeve);
		} else {
			avatar.renderLeftHand(pose, collector, light, skin, sleeve);
		}
		pose.popPose();
	}
}
