package com.mchorizon;

import com.mchorizon.gun.GunState;
import com.mchorizon.gun.GunTuning;
import com.mchorizon.net.GunPayload;
import com.mojang.brigadier.arguments.FloatArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.minecraft.network.chat.Component;

import java.util.Locale;

public class HorizonClient implements ClientModInitializer {
	public static final String MOD_ID = "mchorizon";

	@Override
	public void onInitializeClient() {
		GunTuning.load();

		// Registering the receiver also tells the server we listen on mchorizon:gun,
		// which is how MCEvents knows the mod is installed.
		PayloadTypeRegistry.playS2C().register(GunPayload.TYPE, GunPayload.CODEC);
		ClientPlayNetworking.registerGlobalReceiver(GunPayload.TYPE, (payload, context) -> GunState.handle(payload.json()));
		ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> GunState.clear());
		ClientTickEvents.END_CLIENT_TICK.register(GunState::tick);

		// /gunpose: tune how the guns and arms sit, live
		ClientCommandRegistrationCallback.EVENT.register((dispatcher, registry) -> dispatcher.register(
				ClientCommandManager.literal("gunpose")
						.executes(ctx -> {
							StringBuilder sb = new StringBuilder("Gun pose: ");
							for (String n : GunTuning.names()) {
								sb.append(n).append('=').append(fmt(GunTuning.get(n))).append("  ");
							}
							ctx.getSource().sendFeedback(Component.literal(sb.toString().trim()));
							return 1;
						})
						.then(ClientCommandManager.literal("save").executes(ctx -> {
							ctx.getSource().sendFeedback(Component.literal(GunTuning.save()
									? "Saved to config/mchorizon-gun.json" : "Couldn't save the gun pose"));
							return 1;
						}))
						.then(ClientCommandManager.literal("reset").executes(ctx -> {
							GunTuning.reset();
							ctx.getSource().sendFeedback(Component.literal("Gun pose back to defaults (not saved yet)"));
							return 1;
						}))
						.then(ClientCommandManager.argument("name", StringArgumentType.word())
								.suggests((ctx, b) -> {
									GunTuning.names().forEach(b::suggest);
									return b.buildFuture();
								})
								.executes(ctx -> {
									String n = StringArgumentType.getString(ctx, "name");
									Float v = GunTuning.get(n);
									ctx.getSource().sendFeedback(Component.literal(v == null ? "No setting called " + n : n + " = " + fmt(v)));
									return 1;
								})
								.then(ClientCommandManager.argument("value", FloatArgumentType.floatArg()).executes(ctx -> {
									String n = StringArgumentType.getString(ctx, "name");
									float v = FloatArgumentType.getFloat(ctx, "value");
									ctx.getSource().sendFeedback(Component.literal(GunTuning.set(n, v)
											? n + " = " + fmt(v) : "No setting called " + n));
									return 1;
								})))));
	}

	private static String fmt(Float v) {
		return v == null ? "?" : String.format(Locale.ROOT, "%.3f", v).replaceAll("0+$", "").replaceAll("\\.$", "");
	}
}
