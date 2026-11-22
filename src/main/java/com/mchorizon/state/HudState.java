package com.mchorizon.state;

import com.google.gson.Gson;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** The latest data from the server, plus when it arrived (so timers can keep ticking smoothly). */
public final class HudState {
	private static final Logger LOG = LoggerFactory.getLogger("MCHorizon");
	private static final Gson GSON = new Gson();
	private static final long STALE_MS = 5000;

	private static HudData data;
	private static long receivedAt;
	private static boolean warnedNewer;

	private HudState() {}

	public static void update(String json) {
		try {
			HudData parsed = GSON.fromJson(json, HudData.class);
			if (parsed == null) {
				return;
			}
			if (parsed.v > 1 && !warnedNewer) {
				warnedNewer = true;
				LOG.warn("Server sends HUD protocol v{}; this mod understands v1. Update the mod for new features.", parsed.v);
			}
			data = parsed;
			receivedAt = System.currentTimeMillis();
		} catch (Exception e) {
			LOG.warn("Bad HUD data from server: {}", e.toString());
		}
	}

	public static void clear() {
		data = null;
	}

	/** Data, or null when the server isn't sending any (not MC Horizons, or MCEvents without the bridge). */
	public static HudData get() {
		if (data == null || System.currentTimeMillis() - receivedAt > STALE_MS) {
			return null;
		}
		return data;
	}

	/** A server countdown, ticked down locally between the once-per-second updates. */
	public static int tick(int serverSeconds, boolean paused) {
		if (paused) {
			return serverSeconds;
		}
		int elapsed = (int) ((System.currentTimeMillis() - receivedAt) / 1000L);
		return Math.max(0, serverSeconds - elapsed);
	}
}
