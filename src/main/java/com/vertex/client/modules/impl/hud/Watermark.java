package com.vertex.client.modules.impl.hud;

import com.vertex.client.hud.HUDManager;
import com.vertex.client.modules.Module;
import com.vertex.client.modules.ModuleCategory;
import com.vertex.client.modules.ModuleInfo;
import com.vertex.client.render.font.RvFont;
import com.vertex.client.render.font.RvFontManager;
import com.vertex.client.render.util.RvRenderUtils;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.util.Identifier;
import org.lwjgl.glfw.GLFW;

@ModuleInfo(name = "Watermark", desc = "Отображение клиента в углу экрана", type = ModuleCategory.Display, key = GLFW.GLFW_KEY_UNKNOWN)
public class Watermark extends Module {

    private static final Identifier LOGO_TEXTURE = Identifier.of("vertexclient", "textures/logo/rv_colored.png");

    public Watermark() {
        HudRenderCallback.EVENT.register((ctx, tickCounter) -> this.draw(ctx));
    }

    @Override
    public void onEvent(Object event) {
    }

    private void draw(DrawContext ctx) {
        if (!isState()) return;
        if (mc.player == null) return;

        HUDManager.Slot slot = HUDManager.watermark;
        if (!slot.enabled) return;

        RvFont font = RvFontManager.getSubFont();
        int fps = this.getFps(mc);
        int ping = 54;
        PlayerListEntry entry;
        if (mc.getNetworkHandler() != null && (entry = mc.getNetworkHandler().getPlayerListEntry(mc.player.getUuid())) != null && entry.getLatency() >= 0) {
            ping = entry.getLatency();
        }

        String titleText = "Really Visuals";
        String fpsText = fps + " FPS";
        String pingText = ping + " ms";
        int titleW = Math.round(font.getStringWidth(titleText));
        int fpsW = Math.round(font.getStringWidth(fpsText));
        int pingW = Math.round(font.getStringWidth(pingText));

        int dotSize = 3;
        int gap = 6;
        int logoW = 19;
        int logoH = 10;
        int cardHeight = 19;
        int cardWidth = 8 + logoW + 8 + titleW + gap + dotSize + gap + fpsW + gap + dotSize + gap + pingW + 8;

        slot.resize(cardWidth, cardHeight);
        slot.beginScale(ctx);

        int x = Math.round(slot.x);
        int y = Math.round(slot.y);
        float radius = 6.0F;

        RvRenderUtils.drawRoundedRect(ctx.getMatrices(), x, y, cardWidth, cardHeight, radius, 0xEE111115);
        RvRenderUtils.drawRoundedRect(ctx.getMatrices(), x + 1, y + 1, cardWidth - 2, cardHeight - 2, radius - 0.5F, 0xFF141419);

        int curX = x + 8;
        try {
            RvRenderUtils.drawTexture(ctx.getMatrices(), LOGO_TEXTURE, curX, y + (cardHeight - logoH) / 2.0F, logoW, logoH);
        } catch (Exception e) {
            RvRenderUtils.drawRoundedRect(ctx.getMatrices(), curX, y + (cardHeight - logoH) / 2.0F, logoW, logoH, 3.0F, 0xFFFFFFFF);
        }

        float textY = y + (cardHeight - font.getHeight()) / 2.0F;
        int accentDot = 0xFF7A7A85;

        font.drawString(ctx.getMatrices(), titleText, curX + logoW + 8, textY, 0xFFFFFFFF);
        curX = curX + logoW + 8 + titleW + gap;
        RvRenderUtils.drawRoundedRect(ctx.getMatrices(), curX, y + 8.0F, dotSize, dotSize, dotSize / 2.0F, accentDot);
        curX += dotSize + gap;
        font.drawString(ctx.getMatrices(), fpsText, curX, textY, 0xFFFFFFFF);
        curX += fpsW + gap;
        RvRenderUtils.drawRoundedRect(ctx.getMatrices(), curX, y + 8.0F, dotSize, dotSize, dotSize / 2.0F, accentDot);
        curX += dotSize + gap;
        font.drawString(ctx.getMatrices(), pingText, curX, textY, 0xFFFFFFFF);

        slot.endScale(ctx);
    }

    private int getFps(MinecraftClient mc) {
        try {
            String debug = mc.fpsDebugString;
            if (debug != null && debug.contains(" ")) {
                return Integer.parseInt(debug.split(" ")[0]);
            }
        } catch (Exception ignored) {
        }
        return 60;
    }
}