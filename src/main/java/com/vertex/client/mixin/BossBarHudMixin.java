package com.vertex.client.mixin;

import com.vertex.client.hud.HUDManager;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.hud.BossBarHud;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(BossBarHud.class)
public class BossBarHudMixin {

    @Inject(method = "render", at = @At("HEAD"), require = 0)
    private void vertex$onRenderStart(DrawContext context, CallbackInfo ci) {
        HUDManager.Slot slot = HUDManager.bossbar;
        float dx = slot.x - slot.defaultX;
        float dy = slot.y - slot.defaultY;
        if (dx != 0f || dy != 0f) {
            context.getMatrices().push();
            context.getMatrices().translate(dx, dy, 0);
        }
    }

    @Inject(method = "render", at = @At("RETURN"), require = 0)
    private void vertex$onRenderEnd(DrawContext context, CallbackInfo ci) {
        HUDManager.Slot slot = HUDManager.bossbar;
        float dx = slot.x - slot.defaultX;
        float dy = slot.y - slot.defaultY;
        if (dx != 0f || dy != 0f) {
            context.getMatrices().pop();
        }
    }
}