package com.mchorizon.mixin;

import com.mchorizon.gun.GunState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.AbstractClientPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** Aim-down-sights zoom: narrows your view smoothly as the gun comes up (vanilla eases FOV changes too). */
@Mixin(AbstractClientPlayer.class)
public abstract class AbstractClientPlayerMixin {
	@Inject(method = "getFieldOfViewModifier", at = @At("RETURN"), cancellable = true)
	private void mchorizon$aimZoom(boolean firstPerson, float fovEffectScale, CallbackInfoReturnable<Float> cir) {
		if ((Object) this != Minecraft.getInstance().player) {
			return;
		}
		float m = GunState.fovMultiplier();
		if (m < 0.999F) {
			cir.setReturnValue(cir.getReturnValueF() * m);
		}
	}
}
