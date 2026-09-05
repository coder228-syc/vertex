package com.vertex.client.clickgui;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.MinecraftClient;
import org.lwjgl.glfw.GLFW;

public final class ClickGuiKeyListener {

    private static int openKey = GLFW.GLFW_KEY_RIGHT_SHIFT;

    private static boolean wasPressed = false;
    private static boolean registered = false;

    private ClickGuiKeyListener() {}

    public static void register() {
        if (registered) return;
        registered = true;

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            long window = client.getWindow().getHandle();
            boolean pressed = GLFW.glfwGetKey(window, openKey) == GLFW.GLFW_PRESS;

            if (pressed && !wasPressed) {
                onKeyPressed(client);
            }
            wasPressed = pressed;
        });
    }

    private static void onKeyPressed(MinecraftClient client) {
        if (client.currentScreen instanceof ClickGuiScreen) {
            return;
        }

        if (!ClickGuiOpenHelper.canOpen(client)) return;

        client.setScreen(ClickGuiOpenHelper.open(client));
    }

    public static void setOpenKey(int glfwKeyCode) {
        openKey = glfwKeyCode;
    }

    public static int getOpenKey() {
        return openKey;
    }
}