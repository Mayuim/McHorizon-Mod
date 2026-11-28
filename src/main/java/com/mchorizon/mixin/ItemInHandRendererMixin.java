package com.mchorizon.mixin;

import com.mchorizon.gun.GunRenderer;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.ItemInHandRenderer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** While you hold a paint gun, our renderer draws the first-person hands instead of vanilla. */
@Mixin(ItemInHandRenderer.class)
public abstract class ItemInHandRendererMixin {
	@Shadow
	@Final
	private EntityRenderDispatcher entityRenderDispatcher;

	@Inject(method = "renderArmWithItem", at = @At("HEAD"), cancellable = true)
	private void mchorizon$paintGun(AbstractClientPlayer player, float partialTick, float pitch, InteractionHand hand,
			float swingProgress, ItemStack stack, float equipProgress, PoseStack pose, SubmitNodeCollector collector,
			int light, CallbackInfo ci) {
		if (GunRenderer.render((ItemInHandRenderer) (Object) this, entityRenderDispatcher, player, partialTick, hand,
				pose, collector, light)) {
			ci.cancel();
		}
	}
}
