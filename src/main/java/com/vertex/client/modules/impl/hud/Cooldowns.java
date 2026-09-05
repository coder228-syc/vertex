package com.vertex.client.modules.impl.hud;

import com.vertex.client.hud.HUDManager;
import com.vertex.client.modules.Module;
import com.vertex.client.modules.ModuleCategory;
import com.vertex.client.modules.ModuleInfo;
import com.vertex.client.render.font.RvFont;
import com.vertex.client.render.font.RvFontManager;
import com.vertex.client.render.font.RvTheme;
import com.vertex.client.render.util.RvRenderUtils;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ChatScreen;
import net.minecraft.entity.player.ItemCooldownManager;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;
import org.lwjgl.glfw.GLFW;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@ModuleInfo(name = "Cooldowns", desc = "Отображение кулдаунов предметов", type = ModuleCategory.Display, key = GLFW.GLFW_KEY_UNKNOWN)
public class Cooldowns extends Module {

    private static final int CARD_WIDTH = 135;
    private static final int ROW_HEIGHT = 18;
    private static final int HEADER_HEIGHT = 22;

    private static final Map<Item, String> ITEM_NAMES = new HashMap<>();

    private static Field tickField = null;
    private static Field entriesField = null;
    private static Field endTickField = null;

    public Cooldowns() {
        HudRenderCallback.EVENT.register((ctx, tickCounter) -> this.draw(ctx));
    }

    @Override
    public void onEvent(Object event) {
    }

    static {
        ITEM_NAMES.put(Items.GOLDEN_APPLE, "Золотое яблоко");
        ITEM_NAMES.put(Items.ENCHANTED_GOLDEN_APPLE, "Зачарованное яблоко");
        ITEM_NAMES.put(Items.FIREWORK_ROCKET, "Фейерверк");
        ITEM_NAMES.put(Items.ENDER_PEARL, "Эндер-шар");
        ITEM_NAMES.put(Items.CHORUS_FRUIT, "Хорус");
        ITEM_NAMES.put(Items.POPPED_CHORUS_FRUIT, "Жареный хорус");
        ITEM_NAMES.put(Items.SHIELD, "Щит");
        ITEM_NAMES.put(Items.TOTEM_OF_UNDYING, "Тотем");
        ITEM_NAMES.put(Items.ENDER_EYE, "Око эндера");

        try {
            for (Field f : ItemCooldownManager.class.getDeclaredFields()) {
                if (f.getType() == int.class && tickField == null) {
                    f.setAccessible(true);
                    tickField = f;
                } else if (Map.class.isAssignableFrom(f.getType()) && entriesField == null) {
                    f.setAccessible(true);
                    entriesField = f;
                }
            }

            for (Class<?> cls : ItemCooldownManager.class.getDeclaredClasses()) {
                Field f1 = null;
                Field f2 = null;
                for (Field f : cls.getDeclaredFields()) {
                    if (f.getType() == int.class) {
                        f.setAccessible(true);
                        if (f1 == null) f1 = f;
                        else if (f2 == null) f2 = f;
                    }
                }
                if (f1 != null && f2 != null) {
                    endTickField = f2;
                    break;
                }
            }
        } catch (Exception ignored) {
        }
    }

    private void draw(DrawContext ctx) {
        if (!isState()) return;
        if (mc.player == null || mc.world == null) return;

        List<ActiveCooldown> list = getActiveCooldowns();
        if (list.isEmpty()) {
            if (!(mc.currentScreen instanceof ChatScreen)) return;
            list.add(new ActiveCooldown(new ItemStack(Items.ENDER_PEARL), "Эндер-шар", "0,0s"));
        }

        RvFont mainFont = RvFontManager.getMainFont();
        RvFont subFont = RvFontManager.getSubFont();

        int cardHeight = HEADER_HEIGHT + list.size() * ROW_HEIGHT + 4;

        HUDManager.Slot slot = HUDManager.cooldowns;
        if (!slot.enabled) return;
        slot.resize(CARD_WIDTH, cardHeight);
        slot.beginScale(ctx);

        int x = Math.round(slot.x);
        int y = Math.round(slot.y);

        RvRenderUtils.drawRoundedRect(ctx.getMatrices(), x, y, CARD_WIDTH, cardHeight, 8.0F, 0xEE111115);
        RvRenderUtils.drawRoundedRect(ctx.getMatrices(), x + 1, y + 1, CARD_WIDTH - 2, cardHeight - 2, 7.5F, 0xFF141419);

        drawClockIcon(ctx, x + 8, y + 6);
        mainFont.drawString(ctx.getMatrices(), "Cooldowns", x + 23, y + 5, 0xFFFFFFFF);

        int rowY = y + HEADER_HEIGHT;
        for (ActiveCooldown cd : list) {
            try {
                ctx.drawItem(cd.stack, x + 6, rowY + 1);
            } catch (Exception ignored) {
            }
            mainFont.drawString(ctx.getMatrices(), cd.name, x + 26, rowY + 4, 0xFFFFFFFF);

            int pillWidth = Math.round(subFont.getStringWidth(cd.timeText)) + 8;
            int pillX = x + CARD_WIDTH - 6 - pillWidth;
            int pillY = rowY + 2;
            RvRenderUtils.drawRoundedRect(ctx.getMatrices(), pillX, pillY, pillWidth, 13.0F, 4.0F, 0xFF1F1F26);
            subFont.drawString(ctx.getMatrices(), cd.timeText, pillX + 4, pillY + 3, 0xFFFFFFFF);

            rowY += ROW_HEIGHT;
        }

        slot.endScale(ctx);
    }

    private void drawClockIcon(DrawContext ctx, float x, float y) {
        RvRenderUtils.drawRoundedRect(ctx.getMatrices(), x, y, 9.0F, 9.0F, 4.5F, RvTheme.CLIENT_COLOR);
        RvRenderUtils.drawRoundedRect(ctx.getMatrices(), x + 1.5F, y + 1.5F, 6.0F, 6.0F, 3.0F, 0xFF141419);
        RvRenderUtils.drawRect(ctx.getMatrices(), x + 4, y + 2.5F, 1.0F, 2.5F, RvTheme.CLIENT_COLOR);
        RvRenderUtils.drawRect(ctx.getMatrices(), x + 4, y + 4.5F, 2.0F, 1.0F, RvTheme.CLIENT_COLOR);
    }

    private List<ActiveCooldown> getActiveCooldowns() {
        List<ActiveCooldown> list = new ArrayList<>();
        if (mc.player == null) return list;

        ItemCooldownManager manager = mc.player.getItemCooldownManager();
        try {
            int currentTick = tickField != null ? tickField.getInt(manager) : 0;
            Map<?, ?> entries = entriesField != null ? (Map<?, ?>) entriesField.get(manager) : null;
            if (entries != null && !entries.isEmpty()) {
                for (Map.Entry<?, ?> e : entries.entrySet()) {
                    Object key = e.getKey();
                    Object val = e.getValue();
                    Item item = null;
                    if (key instanceof Identifier id) {
                        item = Registries.ITEM.get(id);
                    } else if (key instanceof Item i) {
                        item = i;
                    }

                    int remainingTicks;
                    if (item != null && item != Items.AIR && val != null
                            && (remainingTicks = (endTickField != null ? endTickField.getInt(val) : 0) - currentTick) > 0) {
                        float secs = remainingTicks / 20.0F;
                        String timeStr;
                        if (secs < 10.0F) {
                            timeStr = String.format("%.1fs", secs).replace(".", ",");
                        } else {
                            int totalSecs = (int) secs;
                            timeStr = String.format("%d:%02d", totalSecs / 60, totalSecs % 60);
                        }
                        String name = ITEM_NAMES.getOrDefault(item, new ItemStack(item).getName().getString());
                        list.add(new ActiveCooldown(new ItemStack(item), name, timeStr));
                    }
                }
            }
        } catch (Exception ignored) {
        }
        return list;
    }

    public static class ActiveCooldown {
        public final ItemStack stack;
        public final String name;
        public final String timeText;

        public ActiveCooldown(ItemStack stack, String name, String timeText) {
            this.stack = stack;
            this.name = name;
            this.timeText = timeText;
        }
    }
}