package com.vertex.client.mixin;

import com.vertex.client.modules.impl.render.ShaderBlock;
import com.vertex.client.modules.impl.render.ShaderSky;
import net.minecraft.block.BlockState;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.client.util.ObjectAllocator;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.BlockPos;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(WorldRenderer.class)
public class WorldRendererMixin {

    @Inject(method = "render", at = @At("TAIL"))
    private void vertex$renderBlockOutline(ObjectAllocator allocator, RenderTickCounter tickCounter, boolean renderBlockOutline, Camera camera, GameRenderer gameRenderer, Matrix4f positionMatrix, Matrix4f projectionMatrix, CallbackInfo ci) {
        ShaderBlock feature = ShaderBlock.INSTANCE;
        if (feature != null && feature.isState()) {
            feature.render(camera, positionMatrix);
        }
        ShaderSky skyFeature = ShaderSky.INSTANCE;
        if (skyFeature != null && skyFeature.isState()) {
            skyFeature.render(camera, positionMatrix, projectionMatrix);
        }
    }

    @Inject(method = "drawBlockOutline", at = @At("HEAD"), cancellable = true)
    private void vertex$cancelBlockOutline(MatrixStack matrices, VertexConsumer vertexConsumer, Entity entity, double cameraX, double cameraY, double cameraZ, BlockPos blockPos, BlockState blockState, int color, CallbackInfo ci) {
        ShaderBlock feature = ShaderBlock.INSTANCE;
        if (feature != null && feature.isState()) {
            ci.cancel();
        }
    }
}