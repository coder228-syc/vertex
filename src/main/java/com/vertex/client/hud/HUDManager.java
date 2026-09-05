package com.vertex.client.hud;

import com.google.gson.JsonObject;
import net.minecraft.client.gui.DrawContext;


public class HUDManager {

    public static final Slot inventoryHud = new Slot("Inventory HUD", 10f, 175f, 188f, 68f);
    public static final Slot targetHud = new Slot("Target HUD", 80f, 120f, 108f, 40f);
    public static final Slot potions = new Slot("Potions", 15f, 60f, 115f, 60f);
    public static final Slot cooldowns = new Slot("Cooldowns", 140f, 120f, 110f, 45f);
    public static final Slot hotkeys = new Slot("Hot Keys", 140f, 60f, 110f, 50f);
    public static final Slot betterNear = new Slot("Better Near", 15f, 140f, 145f, 136f);
    public static final Slot watermark = new Slot("Watermark", 260f, 10f, 140f, 20f);
    public static final Slot bossbar = new Slot("Bossbar", 180f, 10f, 180f, 18f);
    public static final Slot scoreboard = new Slot("Scoreboard", 320f, 100f, 100f, 100f);


    private static final Slot[] ALL = {
            inventoryHud, targetHud, potions, cooldowns, hotkeys, betterNear, watermark, bossbar, scoreboard
    };

    private HUDManager() {
    }

    public static Slot get(String name) {
        for (Slot s : ALL) {
            if (s.name.equalsIgnoreCase(name)) return s;
        }
        return null;
    }

    public static Slot[] getAll() {
        return ALL;
    }

    public static void saveAll(JsonObject root) {
        for (Slot s : ALL) {
            root.add(s.name, s.save());
        }
    }

    public static void loadAll(JsonObject root) {
        for (Slot s : ALL) {
            if (root.has(s.name)) {
                s.load(root.getAsJsonObject(s.name));
            }
        }
    }


    public static final class Slot {
        public final String name;
        public float x;
        public float y;
        public final float defaultX;
        public final float defaultY;
        public float width;
        public float height;
        public float scale = 1.0f;
        public boolean enabled = true;

        Slot(String name, float defaultX, float defaultY, float width, float height) {
            this.name = name;
            this.defaultX = defaultX;
            this.defaultY = defaultY;
            this.x = defaultX;
            this.y = defaultY;
            this.width = width;
            this.height = height;
        }

        public void resize(float contentWidth, float contentHeight) {
            this.width = contentWidth * scale;
            this.height = contentHeight * scale;
        }


        public void beginScale(DrawContext ctx) {
            ctx.getMatrices().push();
            ctx.getMatrices().translate(x, y, 0);
            ctx.getMatrices().scale(scale, scale, 1f);
            ctx.getMatrices().translate(-x, -y, 0);
        }

        public void endScale(DrawContext ctx) {
            ctx.getMatrices().pop();
        }

        public JsonObject save() {
            JsonObject o = new JsonObject();
            o.addProperty("x", x);
            o.addProperty("y", y);
            o.addProperty("scale", scale);
            o.addProperty("enabled", enabled);
            return o;
        }

        public void load(JsonObject o) {
            if (o.has("x")) x = o.get("x").getAsFloat();
            if (o.has("y")) y = o.get("y").getAsFloat();
            if (o.has("scale")) scale = o.get("scale").getAsFloat();
            if (o.has("enabled")) enabled = o.get("enabled").getAsBoolean();
        }
    }
}
