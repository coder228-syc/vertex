package com.vertex.client.modules.impl.hud;

import com.vertex.client.hud.HUDManager;
import com.vertex.client.modules.Module;
import com.vertex.client.modules.ModuleCategory;
import com.vertex.client.modules.ModuleInfo;
import com.vertex.client.render.font.RvFont;
import com.vertex.client.render.font.RvFontManager;
import com.vertex.client.render.util.RvRenderUtils;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ChatScreen;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.texture.Sprite;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.List;

@ModuleInfo(name = "Potions HUD", desc = "Отображение активных эффектов в виде карточки", type = ModuleCategory.Display, key = GLFW.GLFW_KEY_UNKNOWN)
public class PotionsHud extends Module {

    private static final int CARD_WIDTH = 120;
    private static final int ROW_HEIGHT = 16;
    private static final int HEADER_HEIGHT = 22;

    public PotionsHud() {
        HudRenderCallback.EVENT.register((ctx, tickCounter) -> this.draw(ctx));
    }

    @Override
    public void onEvent(Object event) {
    }

    private void draw(DrawContext ctx) {
        if (!isState()) return;
        if (mc.player == null || mc.world == null) return;

        List<ActivePotion> list = this.getActivePotions();
        if (list.isEmpty()) {
            if (!(mc.currentScreen instanceof ChatScreen)) return;
            list.add(new ActivePotion(null, "Strength", "3:12"));
            list.add(new ActivePotion(null, "Fire Resi", "1:20"));
            list.add(new ActivePotion(null, "Speed II", "0:45"));
        }

        RvFont mainFont = RvFontManager.getMainFont();
        RvFont subFont = RvFontManager.getSubFont();

        int cardHeight = HEADER_HEIGHT + list.size() * ROW_HEIGHT + 4;

        HUDManager.Slot slot = HUDManager.potions;
        if (!slot.enabled) return;
        slot.resize(CARD_WIDTH, cardHeight);
        slot.beginScale(ctx);

        int x = Math.round(slot.x);
        int y = Math.round(slot.y);

        RvRenderUtils.drawRoundedRect(ctx.getMatrices(), x, y, CARD_WIDTH, cardHeight, 7.0F, 0xEE111115);
        RvRenderUtils.drawRoundedRect(ctx.getMatrices(), x + 1, y + 1, CARD_WIDTH - 2, cardHeight - 2, 6.5F, 0xFF16161C);

        drawPotionFlaskIcon(ctx, x + 8, y + 6);
        mainFont.drawString(ctx.getMatrices(), "Potions", x + 22, y + 5, 0xFFFFFFFF);

        int rowY = y + HEADER_HEIGHT;
        for (ActivePotion ps : list) {
            drawStatusEffectSprite(ctx, ps.effect, x + 8, rowY + 2, 10);
            subFont.drawString(ctx.getMatrices(), ps.name, x + 22, rowY + 2, 0xFFFFFFFF);

            int pillWidth = Math.round(subFont.getStringWidth(ps.durationText)) + 8;
            int pillX = x + CARD_WIDTH - 6 - pillWidth;
            int pillY = rowY + 1;
            RvRenderUtils.drawRoundedRect(ctx.getMatrices(), pillX, pillY, pillWidth, 12.0F, 4.0F, 0xFF1E1E26);
            subFont.drawString(ctx.getMatrices(), ps.durationText, pillX + 4, pillY + 2, 0xFFFFFFFF);

            rowY += ROW_HEIGHT;
        }

        slot.endScale(ctx);
    }

    private void drawPotionFlaskIcon(DrawContext ctx, float x, float y) {
        RvFont iconFont = RvFontManager.getIconFont();
        ctx.getMatrices().push();
        float scale = 0.55F;
        ctx.getMatrices().scale(scale, scale, 1.0F);
        float invS = 1.0F / scale;
        iconFont.drawString(ctx.getMatrices(), "\ue917", (x + 2.0F) * invS, (y + 1.0F) * invS, 0xFFFF6012);
        ctx.getMatrices().pop();
    }

    private void drawStatusEffectSprite(DrawContext ctx, RegistryEntry<StatusEffect> entry, float x, float y, float size) {
        try {
            Sprite sprite;
            if (entry != null && mc.getStatusEffectSpriteManager() != null
                    && (sprite = mc.getStatusEffectSpriteManager().getSprite(entry)) != null) {
                ctx.drawSpriteStretched(RenderLayer::getGuiTextured, sprite, Math.round(x), Math.round(y), Math.round(size), Math.round(size));
                return;
            }
        } catch (Throwable ignored) {
        }
        RvRenderUtils.drawRoundedRect(ctx.getMatrices(), x, y + 1, size - 2, size - 2, 2.0F, 0xFFFF6012);
    }

    private List<ActivePotion> getActivePotions() {
        List<ActivePotion> list = new ArrayList<>();
        if (mc.player == null) return list;

        for (StatusEffectInstance instance : mc.player.getStatusEffects()) {
            StatusEffect effect = instance.getEffectType().value();
            String name = Text.translatable(effect.getTranslationKey()).getString();
            int amp = instance.getAmplifier();
            if (amp > 0) name = name + " " + toRoman(amp + 1);

            boolean isInf = instance.getDuration() > 32000 || instance.isInfinite();
            String durStr = isInf ? "Inf" : String.format("%d:%02d", instance.getDuration() / 20 / 60, instance.getDuration() / 20 % 60);
            list.add(new ActivePotion(instance.getEffectType(), name, durStr));
        }
        return list;
    }

    private static String toRoman(int n) {
        return switch (n) {
            case 1 -> "I";
            case 2 -> "II";
            case 3 -> "III";
            case 4 -> "IV";
            case 5 -> "V";
            default -> String.valueOf(n);
        };
    }

    public static class ActivePotion {
        public final RegistryEntry<StatusEffect> effect;
        public final String name;
        public final String durationText;

        public ActivePotion(RegistryEntry<StatusEffect> effect, String name, String durationText) {
            this.effect = effect;
            this.name = name;
            this.durationText = durationText;
        }
    }
}