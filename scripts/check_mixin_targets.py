#!/usr/bin/env python3
"""Fails the build if a call our mixins wrap isn't in Minecraft's code (it would crash on launch)."""
import os, subprocess, sys, zipfile
from pathlib import Path

CHECKS = [
    # (class, method, exact call that must appear inside it)
    ("net.minecraft.client.gui.components.PlayerTabOverlay", "render",
     "net/minecraft/client/gui/GuiGraphics.drawString:(Lnet/minecraft/client/gui/Font;Lnet/minecraft/util/FormattedCharSequence;III)V"),
]

jars = []
for base in [Path.home() / ".gradle", Path(".gradle")]:
    for dp, _, fs in os.walk(base):
        for f in fs:
            if f.endswith(".jar") and "minecraft" in f and "sources" not in f:
                p = Path(dp) / f
                try:
                    if "net/minecraft/client/gui/components/PlayerTabOverlay.class" in zipfile.ZipFile(p).namelist():
                        jars.append(p)
                except Exception:
                    pass

for jar in jars:
    ok = True
    for cls, method, call in CHECKS:
        out = subprocess.run(["javap", "-c", "-p", "-cp", str(jar), cls], capture_output=True, text=True).stdout
        body, inside = [], False
        for l in out.splitlines():
            if l.startswith("  ") and not l.startswith("   ") and "(" in l:
                inside = f" {method}(" in l
            elif inside:
                body.append(l)
        if not any(call in l for l in body):
            ok = False
    if ok:
        print(f"All {len(CHECKS)} mixin targets found in {jar.name}")
        sys.exit(0)
print("Mixin target check FAILED: a wrapped call is missing from Minecraft's code")
sys.exit(1)
