package com.vertex.client.modules.impl.hud;

import com.vertex.client.hud.HUDManager;
import com.vertex.client.modules.Module;
import com.vertex.client.modules.ModuleCategory;
import com.vertex.client.modules.ModuleInfo;
import com.vertex.client.modules.ModuleManager;
import com.vertex.client.render.font.RvFont;
import com.vertex.client.render.font.RvFontManager;
import com.vertex.client.render.font.RvTheme;
import com.vertex.client.render.util.RvRenderUtils;
import com.vertex.client.util.KeyMappings;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ChatScreen;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.List;

@ModuleInfo(name = "Hot Keys", desc = "Отображение клавиш включённых модулей", type = ModuleCategory.Display, key = GLFW.GLFW_KEY_UNKNOWN)
public class Hotkeys extends Module {

    private static final int CARD_WIDTH = 118;
    private static final int ROW_HEIGHT = 16;
    private static final int HEADER_HEIGHT = 20;

    public Hotkeys() {
        HudRenderCallback.EVENT.register((ctx, tickCounter) -> this.draw(ctx));
    }

    @Override
    public void onEvent(Object event) {
    }

    private void draw(DrawContext ctx) {
        if (!isState()) return;
        if (mc.player == null || mc.world == null) return;

        List<KeybindSnapshot> list = getActiveKeybinds();
        if (list.isEmpty()) {
            if (!(mc.currentScreen instanceof ChatScreen)) return;
            list.add(new KeybindSnapshot("Armor HUD", "R"));
        }

        RvFont mainFont = RvFontManager.getMainFont();
        RvFont subFont = RvFontManager.getSubFont();

        int cardHeight = HEADER_HEIGHT + list.size() * ROW_HEIGHT + 4;

        HUDManager.Slot slot = HUDManager.hotkeys;
        if (!slot.enabled) return;
        slot.resize(CARD_WIDTH, cardHeight);
        slot.beginScale(ctx);

        int x = Math.round(slot.x);
        int y = Math.round(slot.y);

        RvRenderUtils.drawRoundedRect(ctx.getMatrices(), x, y, CARD_WIDTH, cardHeight, 7.0F, 0xEE111115);
        RvRenderUtils.drawRoundedRect(ctx.getMatrices(), x + 1, y + 1, CARD_WIDTH - 2, cardHeight - 2, 6.5F, 0xFF141419);

        drawKeyboardIcon(ctx, x + 8, y + 5);
        mainFont.drawString(ctx.getMatrices(), "Hot Keys", x + 22, y + 4, 0xFFFFFFFF);

        int rowY = y + HEADER_HEIGHT;
        for (KeybindSnapshot ks : list) {
            subFont.drawString(ctx.getMatrices(), ks.moduleName, x + 8, rowY + 3, 0xFFFFFFFF);

            int pillWidth = Math.round(subFont.getStringWidth(ks.keyName)) + 8;
            int pillX = x + CARD_WIDTH - 6 - pillWidth;
            int pillY = rowY + 1;
            RvRenderUtils.drawRoundedRect(ctx.getMatrices(), pillX, pillY, pillWidth, 12.0F, 4.0F, 0xFF1F1F26);
            subFont.drawString(ctx.getMatrices(), ks.keyName, pillX + 4, pillY + 2, 0xFFFFFFFF);

            rowY += ROW_HEIGHT;
        }

        slot.endScale(ctx);
    }

    private void drawKeyboardIcon(DrawContext ctx, float x, float y) {
        RvRenderUtils.drawRoundedRect(ctx.getMatrices(), x, y, 10.0F, 7.0F, 2.0F, RvTheme.CLIENT_COLOR);
        RvRenderUtils.drawRoundedRect(ctx.getMatrices(), x + 1.0F, y + 1.0F, 8.0F, 5.0F, 1.5F, 0xFF141419);
        RvRenderUtils.drawRect(ctx.getMatrices(), x + 2.5F, y + 2.5F, 1.0F, 1.0F, RvTheme.CLIENT_COLOR);
        RvRenderUtils.drawRect(ctx.getMatrices(), x + 4.5F, y + 2.5F, 1.0F, 1.0F, RvTheme.CLIENT_COLOR);
        RvRenderUtils.drawRect(ctx.getMatrices(), x + 6.5F, y + 2.5F, 1.0F, 1.0F, RvTheme.CLIENT_COLOR);
        RvRenderUtils.drawRect(ctx.getMatrices(), x + 3.5F, y + 4.5F, 3.0F, 1.0F, RvTheme.CLIENT_COLOR);
    }

    private static List<KeybindSnapshot> getActiveKeybinds() {
        List<KeybindSnapshot> list = new ArrayList<>();
        for (Module m : ModuleManager.MODULES) {
            if (m.isState()) {
                String keyStr = KeyMappings.keyMappings(m.bind);
                if (!keyStr.equals("NONE")) {
                    list.add(new KeybindSnapshot(m.name, keyStr));
                }
            }
        }
        return list;
    }

    public static class KeybindSnapshot {
        public final String moduleName;
        public final String keyName;

        public KeybindSnapshot(String moduleName, String keyName) {
            this.moduleName = moduleName;
            this.keyName = keyName;
        }
    }
}