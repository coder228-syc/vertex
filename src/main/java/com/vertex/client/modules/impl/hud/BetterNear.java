package com.vertex.client.modules.impl.hud;

import com.vertex.client.hud.HUDManager;
import com.vertex.client.modules.Module;
import com.vertex.client.modules.ModuleCategory;
import com.vertex.client.modules.ModuleInfo;
import com.vertex.client.modules.setting.SliderSetting;
import com.vertex.client.render.font.RvFont;
import com.vertex.client.render.font.RvFontManager;
import com.vertex.client.render.font.RvTheme;
import com.vertex.client.render.util.RvRenderUtils;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.MathHelper;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@ModuleInfo(name = "Better Near", desc = "Список игроков рядом, отсортированных по расстоянию", type = ModuleCategory.Display, key = GLFW.GLFW_KEY_UNKNOWN)
public class BetterNear extends Module {

    private static final int CARD_WIDTH = 175;
    private static final int ROW_HEIGHT = 18;
    private static final int HEADER_HEIGHT = 24;

    private final SliderSetting playerLimit = new SliderSetting("Лимит игроков", 6, 1, 20, 1);
    private final SliderSetting maxDistance = new SliderSetting("Максимум блоков", 100, 5, 100, 1);

    public BetterNear() {
        addSettings(playerLimit, maxDistance);
        HudRenderCallback.EVENT.register((ctx, tickCounter) -> this.draw(ctx));
    }

    @Override
    public void onEvent(Object event) {
    }

    private void draw(DrawContext ctx) {
        if (!isState()) return;
        if (mc.player == null || mc.world == null) return;

        List<PlayerEntity> players = new ArrayList<>(mc.world.getPlayers());
        double maxDist = maxDistance.get().doubleValue();
        players.removeIf(p -> p == mc.player || !p.isAlive() || p.distanceTo(mc.player) > maxDist);
        if (players.isEmpty()) return;

        players.sort(Comparator.comparingDouble(p -> p.distanceTo(mc.player)));
        int limit = (int) Math.min(playerLimit.get().doubleValue(), players.size());
        if (limit == 0) return;

        RvFont mainFont = RvFontManager.getMainFont();
        RvFont subFont = RvFontManager.getSubFont();

        int cardHeight = HEADER_HEIGHT + limit * ROW_HEIGHT + 4;

        HUDManager.Slot slot = HUDManager.betterNear;
        if (!slot.enabled) return;
        slot.resize(CARD_WIDTH, cardHeight);
        slot.beginScale(ctx);

        int x = Math.round(slot.x);
        int y = Math.round(slot.y);

        RvRenderUtils.drawRoundedRect(ctx.getMatrices(), x, y, CARD_WIDTH, cardHeight, 8.0F, 0xEE111115);
        RvRenderUtils.drawRoundedRect(ctx.getMatrices(), x + 1, y + 1, CARD_WIDTH - 2, cardHeight - 2, 7.5F, 0xFF141419);

        mainFont.drawString(ctx.getMatrices(), "Better Near", x + 10, y + 6, RvTheme.CLIENT_COLOR);
        subFont.drawString(ctx.getMatrices(), "(" + players.size() + ")", x + 10 + Math.round(mainFont.getStringWidth("Better Near")) + 5, y + 7, 0xFF656570);
        RvRenderUtils.drawRect(ctx.getMatrices(), x + 8, y + 21, CARD_WIDTH - 16, 1.0F, 0xFF22222A);

        int rowY = y + 25;
        for (int i = 0; i < limit; i++) {
            PlayerEntity player = players.get(i);
            double dist = player.distanceTo(mc.player);
            String arrow = getDirectionArrow(mc.player.getYaw(), mc.player.getX(), mc.player.getZ(), player.getX(), player.getZ());
            String name = resolveName(player);
            String distStr = String.format("%.0fm", dist);

            int maxNameW = CARD_WIDTH - 20 - 14 - Math.round(subFont.getStringWidth(distStr)) - 6;
            while (name.length() > 1 && mainFont.getStringWidth(name) > maxNameW) {
                name = name.substring(0, name.length() - 1);
            }

            mainFont.drawString(ctx.getMatrices(), name, x + 10, rowY + 2, 0xFFFFFFFF);
            subFont.drawString(ctx.getMatrices(), distStr, x + CARD_WIDTH - 30 - Math.round(subFont.getStringWidth(distStr)), rowY + 3, 0xFFA0A0AA);
            mainFont.drawString(ctx.getMatrices(), arrow, x + CARD_WIDTH - 20, rowY + 2, RvTheme.CLIENT_COLOR);

            rowY += ROW_HEIGHT;
        }

        slot.endScale(ctx);
    }

    private String resolveName(PlayerEntity player) {
        boolean isNpc = false;
        try {
            isNpc = mc.getNetworkHandler() == null || mc.getNetworkHandler().getPlayerListEntry(player.getUuid()) == null;
        } catch (Throwable ignored) {
        }

        String label = null;
        try {
            if (player.getCustomName() != null) label = player.getCustomName().getString();
        } catch (Throwable ignored) {
        }
        if (label == null || label.trim().isEmpty()) {
            try {
                label = player.getDisplayName().getString();
            } catch (Throwable ignored) {
                label = player.getName().getString();
            }
        }
        label = formatStrip(label);
        return isNpc ? "[NPC] " + label : label;
    }

    private static String formatStrip(String text) {
        return text.replaceAll("§[0-9a-fk-or]", "");
    }

    private static String getDirectionArrow(float yaw, double playerX, double playerZ, double targetX, double targetZ) {
        double dz = targetZ - playerZ;
        double dx = targetX - playerX;
        double angle = Math.toDegrees(Math.atan2(dz, dx)) - 90.0;
        double diff = MathHelper.wrapDegrees(angle - yaw);
        if (diff >= -22.5 && diff < 22.5) {
            return "\u2191";
        } else if (diff >= 22.5 && diff < 67.5) {
            return "\u2197";
        } else if (diff >= 67.5 && diff < 112.5) {
            return "\u2192";
        } else if (diff >= 112.5 && diff < 157.5) {
            return "\u2198";
        } else if (diff >= 157.5 || diff < -157.5) {
            return "\u2193";
        } else if (diff >= -157.5 && diff < -112.5) {
            return "\u2199";
        } else if (diff >= -112.5 && diff < -67.5) {
            return "\u2190";
        } else {
            return diff >= -67.5 && diff < -22.5 ? "\u2196" : "\u2191";
        }
    }
}