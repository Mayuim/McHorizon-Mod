package com.mchorizon.net;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

import java.nio.charset.StandardCharsets;

/**
 * The mchorizon:gun plugin message from MCEvents: paint gun state (aiming, shots, reloads).
 * The whole message is UTF-8 JSON (Paper's sendPluginMessage sends raw bytes, no length prefix).
 */
public record GunPayload(String json) implements CustomPacketPayload {
	public static final Type<GunPayload> TYPE = new Type<>(Identifier.fromNamespaceAndPath("mchorizon", "gun"));
	public static final StreamCodec<FriendlyByteBuf, GunPayload> CODEC =
			CustomPacketPayload.codec(GunPayload::write, GunPayload::read);

	private static GunPayload read(FriendlyByteBuf buf) {
		byte[] bytes = new byte[buf.readableBytes()];
		buf.readBytes(bytes);
		return new GunPayload(new String(bytes, StandardCharsets.UTF_8));
	}

	private void write(FriendlyByteBuf buf) {
		buf.writeBytes(json.getBytes(StandardCharsets.UTF_8));
	}

	@Override
	public Type<? extends CustomPacketPayload> type() {
		return TYPE;
	}
}
