#!/usr/bin/env python3
"""Fails the build if a method our mixins inject into isn't in Minecraft's code (it would crash on launch)."""
import os, subprocess, sys, zipfile
from pathlib import Path

CHECKS = [
    # (class, exact method signature as javap prints it)
    ("net.minecraft.client.renderer.ItemInHandRenderer",
     "private void renderArmWithItem(net.minecraft.client.player.AbstractClientPlayer, float, float, "
     "net.minecraft.world.InteractionHand, float, net.minecraft.world.item.ItemStack, float, "
     "com.mojang.blaze3d.vertex.PoseStack, net.minecraft.client.renderer.SubmitNodeCollector, int);"),
    ("net.minecraft.client.renderer.ItemInHandRenderer",
     "public void renderItem(net.minecraft.world.entity.LivingEntity, net.minecraft.world.item.ItemStack, "
     "net.minecraft.world.item.ItemDisplayContext, com.mojang.blaze3d.vertex.PoseStack, "
     "net.minecraft.client.renderer.SubmitNodeCollector, int);"),
    ("net.minecraft.client.player.AbstractClientPlayer",
     "public float getFieldOfViewModifier(boolean, float);"),
]

jars = []
for base in [Path.home() / ".gradle", Path(".gradle")]:
    for dp, _, fs in os.walk(base):
        for f in fs:
            if f.endswith(".jar") and "minecraft" in f and "sources" not in f:
                p = Path(dp) / f
                try:
                    if "net/minecraft/client/renderer/ItemInHandRenderer.class" in zipfile.ZipFile(p).namelist():
                        jars.append(p)
                except Exception:
                    pass

for jar in jars:
    ok = True
    for cls, sig in CHECKS:
        out = subprocess.run(["javap", "-p", "-cp", str(jar), cls], capture_output=True, text=True).stdout
        if not any(l.strip() == sig for l in out.splitlines()):
            print(f"missing in {cls}: {sig}")
            ok = False
    if ok:
        print(f"All {len(CHECKS)} mixin targets found in {jar.name}")
        sys.exit(0)
print("Mixin target check FAILED: a method we hook is missing from Minecraft's code")
sys.exit(1)
