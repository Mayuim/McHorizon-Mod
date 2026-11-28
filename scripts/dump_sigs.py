#!/usr/bin/env python3
"""Prints method signatures of Minecraft classes (for writing mixins against the exact version)."""
import os, subprocess, sys, zipfile
from pathlib import Path
CLASSES = sys.argv[1:]
jar = None
for base in [Path.home() / ".gradle", Path(".gradle")]:
    for dp, _, fs in os.walk(base):
        for f in fs:
            if f.endswith(".jar") and "minecraft" in f and "sources" not in f:
                p = Path(dp) / f
                try:
                    if "net/minecraft/client/renderer/ItemInHandRenderer.class" in zipfile.ZipFile(p).namelist():
                        jar = p
                except Exception:
                    pass
print("JAR", jar)
for c in CLASSES:
    print("=====", c)
    print(subprocess.run(["javap", "-c", "-p", "-cp", str(jar), c], capture_output=True, text=True).stdout)
