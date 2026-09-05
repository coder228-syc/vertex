package com.vertex.client.hud;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.vertex.client.modules.ModuleManager;
import com.vertex.client.render.util.RenderUtil;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ChatScreen;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

public class HudEditor {

    private static final float SNAP_DISTANCE = 10.0F;
    private static final float FADE_SPEED = 0.18F;

    private static final HudEditor INSTANCE = new HudEditor();
    private final MinecraftClient mc = MinecraftClient.getInstance();
    private final Gson gson = new GsonBuilder().setPrettyPrinting().create();

    private HUDManager.Slot dragging;
    private float dragOffsetX;
    private float dragOffsetY;
    private float overlayAlpha;

    private HudEditor() {
    }

    public static HudEditor get() {
        return INSTANCE;
    }

    public boolean isActive() {
        return mc.currentScreen instanceof ChatScreen;
    }

    public boolean isDragging() {
        return dragging != null;
    }

    public boolean tryStartDrag(float mouseX, float mouseY) {
        if (!isActive()) {
            return false;
        }
        HUDManager.Slot[] slots = HUDManager.getAll();
        for (int i = slots.length - 1; i >= 0; i--) {
            HUDManager.Slot slot = slots[i];
            if (!slot.enabled) continue;
            if (mouseX >= slot.x && mouseX <= slot.x + slot.width && mouseY >= slot.y && mouseY <= slot.y + slot.height) {
                dragging = slot;
                dragOffsetX = mouseX - slot.x;
                dragOffsetY = mouseY - slot.y;
                return true;
            }
        }
        return false;
    }

    public void stopDragging() {
        if (dragging == null) {
            return;
        }
        dragging = null;
        save();
    }

    public void updateAndRender(DrawContext ctx, int mouseX, int mouseY) {
        if (!isActive()) {
            if (isDragging()) {
                stopDragging();
            }
            overlayAlpha -= FADE_SPEED;
            if (overlayAlpha < 0F) {
                overlayAlpha = 0F;
            }
            return;
        }

        overlayAlpha += FADE_SPEED;
        if (overlayAlpha > 1F) {
            overlayAlpha = 1F;
        }

        float screenWidth = ctx.getScaledWindowWidth();
        float screenHeight = ctx.getScaledWindowHeight();

        if (isDragging()) {
            applyDragPosition(mouseX, mouseY, screenWidth, screenHeight);
        }

        renderOverlay(ctx, mouseX, mouseY, screenWidth, screenHeight);
    }

    private void applyDragPosition(float mouseX, float mouseY, float screenWidth, float screenHeight) {
        if (dragging == null) {
            return;
        }
        float newX = mouseX - dragOffsetX;
        float newY = mouseY - dragOffsetY;

        if (!isCtrlDown()) {
            float centerX = newX + dragging.width / 2.0F;
            float centerY = newY + dragging.height / 2.0F;
            float screenCenterX = screenWidth / 2.0F;
            float screenCenterY = screenHeight / 2.0F;
            if (Math.abs(centerX - screenCenterX) <= SNAP_DISTANCE) {
                newX = screenCenterX - dragging.width / 2.0F;
            }
            if (Math.abs(centerY - screenCenterY) <= SNAP_DISTANCE) {
                newY = screenCenterY - dragging.height / 2.0F;
            }
        }

        newX = clamp(newX, 0F, Math.max(0F, screenWidth - dragging.width));
        newY = clamp(newY, 0F, Math.max(0F, screenHeight - dragging.height));

        dragging.x = newX;
        dragging.y = newY;
    }

    private void renderOverlay(DrawContext ctx, int mouseX, int mouseY, float screenWidth, float screenHeight) {
        float alpha = overlayAlpha;
        if (alpha <= 0.02F) {
            return;
        }
        int accent = ModuleManager.STYLE_MANAGER.getFirstColor();
        float centerX = screenWidth / 2.0F;
        float centerY = screenHeight / 2.0F;

        if (dragging != null) {
            RenderUtil.drawRoundedRect(ctx.getMatrices(), 0F, centerY - 0.5F, screenWidth, 1F, 0F, RenderUtil.applyOpacity(accent, alpha));
            RenderUtil.drawRoundedRect(ctx.getMatrices(), centerX - 0.5F, 0F, 1F, screenHeight, 0F, RenderUtil.applyOpacity(accent, alpha));
        }

        for (HUDManager.Slot slot : HUDManager.getAll()) {
            if (!slot.enabled) continue;
            boolean hovered = mouseX >= slot.x && mouseX <= slot.x + slot.width
                    && mouseY >= slot.y && mouseY <= slot.y + slot.height;
            boolean selected = slot == dragging;
            float boxAlpha = selected ? alpha : alpha * 0.55F;
            int borderColor = selected || hovered ? 0xFFFFFFFF : accent;
            RenderUtil.drawRoundedBorder(ctx.getMatrices(), slot.x, slot.y, slot.width, slot.height, 3F, 1F, RenderUtil.applyOpacity(borderColor, boxAlpha * 0.8F));
        }
    }

    private boolean isCtrlDown() {
        long handle = mc.getWindow().getHandle();
        return org.lwjgl.glfw.GLFW.glfwGetKey(handle, org.lwjgl.glfw.GLFW.GLFW_KEY_LEFT_CONTROL) == org.lwjgl.glfw.GLFW.GLFW_PRESS
                || org.lwjgl.glfw.GLFW.glfwGetKey(handle, org.lwjgl.glfw.GLFW.GLFW_KEY_RIGHT_CONTROL) == org.lwjgl.glfw.GLFW.GLFW_PRESS;
    }

    private static float clamp(float value, float min, float max) {
        return Math.max(min, Math.min(max, value));
    }

    public void save() {
        try {
            Path dir = FabricLoader.getInstance().getConfigDir().resolve("vertexclient");
            Files.createDirectories(dir);
            JsonObject root = new JsonObject();
            HUDManager.saveAll(root);
            Files.write(dir.resolve("hud.json"), gson.toJson(root).getBytes(StandardCharsets.UTF_8));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void load() {
        try {
            Path path = FabricLoader.getInstance().getConfigDir().resolve("vertexclient").resolve("hud.json");
            if (Files.exists(path)) {
                JsonObject root = (JsonObject) JsonParser.parseString(Files.readString(path, StandardCharsets.UTF_8));
                HUDManager.loadAll(root);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}