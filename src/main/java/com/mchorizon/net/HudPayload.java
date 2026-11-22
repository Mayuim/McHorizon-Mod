package com.mchorizon.net;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

import java.nio.charset.StandardCharsets;

/**
 * The mchorizon:hud plugin message from MCEvents. The whole message is UTF-8 JSON
 * (Paper's sendPluginMessage sends raw bytes, so there's no length prefix).
 */
public record HudPayload(String json) implements CustomPacketPayload {
	public static final Type<HudPayload> TYPE = new Type<>(Identifier.fromNamespaceAndPath("mchorizon", "hud"));
	public static final StreamCodec<FriendlyByteBuf, HudPayload> CODEC =
			CustomPacketPayload.codec(HudPayload::write, HudPayload::read);

	private static HudPayload read(FriendlyByteBuf buf) {
		byte[] bytes = new byte[buf.readableBytes()];
		buf.readBytes(bytes);
		return new HudPayload(new String(bytes, StandardCharsets.UTF_8));
	}

	private void write(FriendlyByteBuf buf) {
		buf.writeBytes(json.getBytes(StandardCharsets.UTF_8));
	}

	@Override
	public Type<? extends CustomPacketPayload> type() {
		return TYPE;
	}
}
