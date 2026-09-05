package com.vertex.client.mixin;

import com.vertex.client.modules.impl.render.ShaderSky;
import net.minecraft.client.render.BackgroundRenderer;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.Fog;
import org.joml.Vector4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(BackgroundRenderer.class)
public class BackgroundRendererMixin {

    @Inject(method = "applyFog", at = @At("TAIL"), cancellable = true)
    private static void vertex$disableFog(Camera camera, BackgroundRenderer.FogType fogType, Vector4f fogColor, float viewDistance, boolean thickFog, float tickDelta, CallbackInfoReturnable<Fog> cir) {
        if (ShaderSky.isActive()) {
            cir.setReturnValue(Fog.DUMMY);
        }
    }
}