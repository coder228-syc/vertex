package com.vertex.client.mixin;

import com.vertex.client.hud.HudEditor;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.Mouse;
import net.minecraft.client.gui.screen.ChatScreen;
import net.minecraft.client.util.Window;
import org.lwjgl.glfw.GLFW;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Mouse.class)
public class MouseMixin {

    @Shadow
    @Final
    private MinecraftClient client;

    @Inject(method = "onMouseButton", at = @At("HEAD"), cancellable = true)
    private void vertex$onMouseButton(long window, int button, int action, int mods, CallbackInfo ci) {
        if (window != client.getWindow().getHandle()) {
            return;
        }
        if (!(client.currentScreen instanceof ChatScreen)) {
            HudEditor.get().stopDragging();
            return;
        }
        if (button != 0) {
            return;
        }
        float[] mouse = getScaledMouse();
        if (action == GLFW.GLFW_PRESS) {
            if (HudEditor.get().tryStartDrag(mouse[0], mouse[1])) {
                ci.cancel();
            }
        } else if (action == GLFW.GLFW_RELEASE && HudEditor.get().isDragging()) {
            HudEditor.get().stopDragging();
            ci.cancel();
        }
    }

    private float[] getScaledMouse() {
        Window window = client.getWindow();
        double[] cursorX = new double[1];
        double[] cursorY = new double[1];
        GLFW.glfwGetCursorPos(window.getHandle(), cursorX, cursorY);
        float x = (float) (cursorX[0] * window.getScaledWidth() / (double) window.getFramebufferWidth());
        float y = (float) (cursorY[0] * window.getScaledHeight() / (double) window.getFramebufferHeight());
        return new float[]{x, y};
    }
}