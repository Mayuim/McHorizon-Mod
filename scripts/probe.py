#!/usr/bin/env python3
"""Temporary: prints Minecraft / Fabric API signatures for one version folder."""
import os, re, subprocess, sys, zipfile
from pathlib import Path
vd = Path(sys.argv[1]).resolve()
jars = []
for base in [Path.home()/".gradle", vd/".gradle", vd/"build"]:
    for dp, _, fs in os.walk(base):
        for f in fs:
            if f.endswith(".jar") and "sources" not in f:
                jars.append(Path(dp)/f)
index = {}
for j in jars:
    try:
        for n in zipfile.ZipFile(j).namelist():
            if n.endswith(".class"):
                index.setdefault(n[:-6].replace("/", "."), []).append(j)
    except Exception:
        pass
def pick(cls):
    js = index.get(cls, [])
    # Prefer jars with readable names (largest minecraft jar / fabric module)
    js = sorted(js, key=lambda p: ("merged" not in p.name and "client" not in p.name, -p.stat().st_size))
    return js[0] if js else None
def javap(cls, flags="-p", grep=None):
    j = pick(cls)
    print(f"\n##### {cls}  [{j.name if j else 'NOT FOUND'}]")
    if not j: return
    out = subprocess.run(["javap", *flags.split(), "-cp", str(j), cls], capture_output=True, text=True).stdout
    for l in out.splitlines():
        if grep is None or re.search(grep, l):
            print(l)
print("PROBE", vd.name)
print("classes matching patterns:")
for pat in ["GuiGraphics", "HudElement", "HudRender", "DeltaTracker", "DownloadedPackSource", "ResourcePackPush", "BossEventPacket", "TeleportToEntity", "SetTitleText", "SetActionBar", "PlayerScoreEntry", "GameProfile", "ClientReceiveMessage", "KeyMappingHelper", "KeyBindingHelper"]:
    hits = sorted(c for c in index if c.split(".")[-1].split("$")[0].find(pat) >= 0 and "$$" not in c)
    print(f"  {pat}: {hits[:12]}")
M = "net.minecraft."
for cls, grep in [
    (M+"client.gui.components.PlayerFaceRenderer", r"draw"),
    (M+"client.multiplayer.PlayerInfo", r"getSkin"),
    (M+"client.gui.GuiGraphics", r"pose\(|drawString\(|guiWidth"),
    ("net.fabricmc.fabric.api.client.rendering.v1.hud.VanillaHudElements", r"public static"),
    ("net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry", None),
    ("net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry", r"playS2C|register"),
    (M+"network.protocol.common.custom.CustomPacketPayload", r"codec"),
    (M+"client.Options", r"keyPlayerList"),
]:
    javap(cls, grep=grep)
