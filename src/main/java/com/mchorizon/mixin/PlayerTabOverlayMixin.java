package com.mchorizon.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mchorizon.render.TeamOutline;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.PlayerTabOverlay;
import net.minecraft.util.FormattedCharSequence;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Notes where each header/footer line of the server's tab list is drawn, then draws
 * the gold outline around your team's block once the list is done.
 */
@Mixin(PlayerTabOverlay.class)
public class PlayerTabOverlayMixin {
	@Inject(method = "render", at = @At("HEAD"))
	private void mchorizon$start(CallbackInfo ci) {
		TeamOutline.begin();
	}

	@WrapOperation(
			method = "render",
			at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GuiGraphics;drawString(Lnet/minecraft/client/gui/Font;Lnet/minecraft/util/FormattedCharSequence;III)V")
	)
	private void mchorizon$recordLine(GuiGraphics graphics, Font font, FormattedCharSequence line, int x, int y, int color,
			Operation<Void> original) {
		TeamOutline.line(graphics, x, y, font.width(line));
		original.call(graphics, font, line, x, y, color);
	}

	@Inject(method = "render", at = @At("TAIL"))
	private void mchorizon$outline(CallbackInfo ci) {
		TeamOutline.draw();
	}
}
