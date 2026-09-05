package com.vertex.client.mixin;

import com.vertex.client.modules.impl.render.ShaderHand;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.GameRenderer;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GameRenderer.class)
public class GameRendererMixin {

    @Inject(method = "renderHand", at = @At("HEAD"))
    private void vertex$onHandRenderStart(Camera camera, float tickDelta, Matrix4f modelViewProjectionMatrix, CallbackInfo ci) {
        ShaderHand m = ShaderHand.INSTANCE;
        if (m != null && m.isState()) {
            m.captureBefore();
        }
    }

    @Inject(method = "renderHand", at = @At("RETURN"))
    private void vertex$onHandRenderEnd(Camera camera, float tickDelta, Matrix4f modelViewProjectionMatrix, CallbackInfo ci) {
        ShaderHand m = ShaderHand.INSTANCE;
        if (m != null && m.isState()) {
            m.captureAfter();
            m.renderPipeline();
        }
    }
}
