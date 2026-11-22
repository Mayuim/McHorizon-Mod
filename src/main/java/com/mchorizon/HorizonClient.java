package com.mchorizon;

import com.mchorizon.config.HorizonConfig;
import com.mchorizon.net.HudPayload;
import com.mchorizon.render.HudPanels;
import com.mchorizon.render.TabGrid;
import com.mchorizon.state.HudState;
import com.mojang.blaze3d.platform.InputConstants;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.hud.VanillaHudElements;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.Identifier;

public class HorizonClient implements ClientModInitializer {
	public static final String MOD_ID = "mchorizon";

	private static final KeyMapping.Category CATEGORY =
			KeyMapping.Category.register(Identifier.fromNamespaceAndPath(MOD_ID, "main"));
	public static final KeyMapping TOGGLE = new KeyMapping("key.mchorizon.toggle",
			InputConstants.Type.KEYSYM, InputConstants.KEY_F7, CATEGORY);

	@Override
	public void onInitializeClient() {
		HorizonConfig.load();
		KeyBindingHelper.registerKeyBinding(TOGGLE);

		// Registering a receiver also tells the server we listen on mchorizon:hud,
		// which is how MCEvents knows to send us data.
		PayloadTypeRegistry.playS2C().register(HudPayload.TYPE, HudPayload.CODEC);
		ClientPlayNetworking.registerGlobalReceiver(HudPayload.TYPE, (payload, context) -> HudState.update(payload.json()));
		ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> HudState.clear());

		ClientTickEvents.END_CLIENT_TICK.register(client -> {
			while (TOGGLE.consumeClick()) {
				HorizonConfig.get().hudEnabled = !HorizonConfig.get().hudEnabled;
				HorizonConfig.save();
			}
		});

		// Everything shows while holding Tab: team grid in the middle, panels on the sides
		// (like the MCC tab screen). Let go and you're back to the normal game + server sidebar.
		HudElementRegistry.addLast(Identifier.fromNamespaceAndPath(MOD_ID, "panels"), (g, delta) -> {
			HorizonConfig c = HorizonConfig.get();
			if (!c.hudEnabled || HudState.get() == null) {
				return;
			}
			if (c.panelsOnlyOnTab && !tabHeld()) {
				return;
			}
			scaled(g, (gg, w, h) -> {
				// Leave room for the grid in the middle; on very narrow screens show just the grid.
				if (!tabHeld() || !showingTabGrid() || w >= TabGrid.WIDTH + 2 * HudPanels.WIDTH + 24) {
					HudPanels.render(gg, w, h);
				}
			});
		});

		// Tab: team overview instead of the player list (only on servers sending MC Horizons data)
		HudElementRegistry.replaceElement(VanillaHudElements.PLAYER_LIST, vanilla -> (g, delta) -> {
			if (showingTabGrid()) {
				if (tabHeld()) {
					scaled(g, TabGrid::render);
				}
				return;
			}
			vanilla.render(g, delta);
		});

		// While the panels are on screen they show the same info as the server's sidebar, so hide it then.
		HudElementRegistry.replaceElement(VanillaHudElements.SCOREBOARD, vanilla -> (g, delta) -> {
			HorizonConfig c = HorizonConfig.get();
			boolean panelsUp = c.hudEnabled && HudState.get() != null && (!c.panelsOnlyOnTab || tabHeld());
			if (panelsUp && c.hideSidebar) {
				return;
			}
			vanilla.render(g, delta);
		});
	}

	private static boolean tabHeld() {
		return Minecraft.getInstance().options.keyPlayerList.isDown();
	}

	private static boolean showingTabGrid() {
		HorizonConfig c = HorizonConfig.get();
		return c.hudEnabled && c.replaceTabList && TabGrid.canRender();
	}

	private interface Drawer {
		void draw(GuiGraphics g, int width, int height);
	}

	private static void scaled(GuiGraphics g, Drawer drawer) {
		float s = HorizonConfig.get().hudScale;
		if (Math.abs(s - 1.0F) < 0.001F) {
			drawer.draw(g, g.guiWidth(), g.guiHeight());
			return;
		}
		g.pose().pushMatrix();
		g.pose().scale(s, s);
		drawer.draw(g, (int) (g.guiWidth() / s), (int) (g.guiHeight() / s));
		g.pose().popMatrix();
	}
}
