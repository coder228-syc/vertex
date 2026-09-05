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
import net.minecraft.client.model.ModelPart;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.render.entity.LivingEntityRenderer;
import net.minecraft.client.render.entity.state.LivingEntityRenderState;
import net.minecraft.client.util.DefaultSkinHelper;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Identifier;
import org.lwjgl.glfw.GLFW;

import java.lang.reflect.Field;
import java.util.List;

@ModuleInfo(name = "Target HUD", desc = "Отображение здоровья цели в виде карточки с лицом", type = ModuleCategory.Display, key = GLFW.GLFW_KEY_UNKNOWN)
public class TargetHud extends Module {

    private float animatedHealth = 20f;

    public TargetHud() {
        HudRenderCallback.EVENT.register((ctx, tickCounter) -> this.draw(ctx));
    }

    @Override
    public void onEvent(Object event) {
    }

    private void draw(DrawContext ctx) {
        if (!isState()) return;
        if (mc.player == null || mc.world == null) return;

        LivingEntity target = (mc.targetedEntity instanceof LivingEntity le && le.isAlive()) ? le : null;
        boolean editorOpen = mc.currentScreen instanceof ChatScreen;
        if (target == null && !editorOpen) return;

        String name = target != null ? resolveName(target) : mc.player.getDisplayName().getString();
        float health = target != null ? Math.max(0f, target.getHealth()) : 13f;
        float maxHealth = target != null ? Math.max(1f, target.getMaxHealth()) : 20f;
        animatedHealth += (health - animatedHealth) * 0.15f;
        if (Math.abs(animatedHealth - health) < 0.01f) animatedHealth = health;

        RvFont mainFont = RvFontManager.getMainFont();
        RvFont subFont = RvFontManager.getSubFont();

        HUDManager.Slot slot = HUDManager.targetHud;
        if (!slot.enabled) return;

        int cardWidth = 108;
        int cardHeight = 40;
        float radius = 5f;
        slot.resize(cardWidth, cardHeight);
        slot.beginScale(ctx);

        int x = Math.round(slot.x);
        int y = Math.round(slot.y);

        RvRenderUtils.drawRoundedRect(ctx.getMatrices(), x, y, cardWidth, cardHeight, radius, 0xEE111115);
        RvRenderUtils.drawRoundedRect(ctx.getMatrices(), x + 1, y + 1, cardWidth - 2, cardHeight - 2, radius - 0.5f, 0xFF16161F);

        int avatarX = x + 4;
        int avatarY = y + 4;
        int avatarSize = 28;

        RvRenderUtils.drawRoundedRect(ctx.getMatrices(), avatarX, avatarY, avatarSize, avatarSize, 3.5f, 0xFF141419);

        boolean showHeadFace = target instanceof PlayerEntity || (target == null && editorOpen);
        if (showHeadFace) {
            PlayerEntity player = target instanceof PlayerEntity pe ? pe : mc.player;
            drawPlayerHeadFace(ctx, player, avatarX, avatarY, avatarSize);
        } else if (target != null) {
            drawMobFace(ctx, target, avatarX, avatarY, avatarSize);
        }

        int maxNameW = cardWidth - 4 - avatarSize - 6 - 6;
        String displayName = name;
        if (subFont.getStringWidth(displayName) > maxNameW) {
            while (displayName.length() > 1 && subFont.getStringWidth(displayName + "...") > maxNameW) {
                displayName = displayName.substring(0, displayName.length() - 1);
            }
            displayName = displayName + "...";
        }
        subFont.drawString(ctx.getMatrices(), displayName, avatarX + avatarSize + 6, avatarY, 0xFFFFFFFF);

        int textX = avatarX + avatarSize + 6;
        int textY = avatarY + 13;
        ctx.getMatrices().push();
        ctx.getMatrices().translate(textX, textY, 0.0);
        ctx.getMatrices().scale(0.85f, 0.85f, 1.0f);
        subFont.drawString(ctx.getMatrices(), "HP", 0.0f, 0.0f, 0xFFB6B6BF);
        int hpW = subFont.getStringWidth("HP");
        RvRenderUtils.drawRoundedRect(ctx.getMatrices(), hpW + 4, 3.5f, 2f, 2f, 1f, 0xFF7A7A85);
        subFont.drawString(ctx.getMatrices(), String.valueOf((int) Math.ceil(health)), hpW + 10, 0.0f, 0xFFB6B6BF);
        ctx.getMatrices().pop();

        int barX = avatarX + avatarSize + 6;
        int barY = y + cardHeight - 10;
        int barWidth = cardWidth - 10 - avatarSize - 6;
        int barHeight = 3;
        RvRenderUtils.drawRoundedRect(ctx.getMatrices(), barX, barY, barWidth, barHeight, 1.5f, 0xFF111115);
        float hpPercent = Math.max(0.02f, Math.min(1f, animatedHealth / maxHealth));
        int filledWidth = (int) (barWidth * hpPercent);
        RvRenderUtils.drawRoundedRect(ctx.getMatrices(), barX, barY, filledWidth, barHeight, 1.5f, RvTheme.CLIENT_COLOR);

        slot.endScale(ctx);
    }

    private String resolveName(LivingEntity target) {
        String custom = null;
        try {
            if (target.getCustomName() != null) custom = target.getCustomName().getString();
        } catch (Throwable ignored) {
        }
        String display;
        try {
            display = target.getDisplayName().getString();
        } catch (Throwable e) {
            display = "Unknown";
        }
        String result = custom != null && !custom.trim().isEmpty() ? custom : display;
        return stripFormatting(result);
    }

    private void drawMobFace(DrawContext ctx, LivingEntity entity, int x, int y, int size) {
        try {
            EntityRenderer<?, ?> renderer = mc.getEntityRenderDispatcher().getRenderer(entity);
            if (!(renderer instanceof LivingEntityRenderer)) {
                drawFallbackFace(ctx, entity, x, y, size);
                return;
            }

            LivingEntityRenderer livingRenderer = (LivingEntityRenderer) renderer;
            Object headPart = findHeadPart(livingRenderer.getModel());
            if (headPart == null) {
                drawFallbackFace(ctx, entity, x, y, size);
                return;
            }

            Object cuboidsValue = getFieldValue(headPart, "cuboids", "field_3663");
            if (!(cuboidsValue instanceof List) || ((List<?>) cuboidsValue).isEmpty()) {
                drawFallbackFace(ctx, entity, x, y, size);
                return;
            }

            ModelPart.Cuboid cuboid = findHeadCuboid((List<?>) cuboidsValue);
            ModelPart.Quad[] sides = cuboid == null ? null : cuboid.sides;
            if (sides == null || sides.length < 6) {
                drawFallbackFace(ctx, entity, x, y, size);
                return;
            }

            ModelPart.Quad front = findFrontQuad(sides);
            if (front == null) {
                drawFallbackFace(ctx, entity, x, y, size);
                return;
            }

            ModelPart.Vertex[] vertices = front.vertices();
            if (vertices == null || vertices.length < 4) {
                drawFallbackFace(ctx, entity, x, y, size);
                return;
            }

            float minU = 1000.0F;
            float minV = 1000.0F;
            float maxU = -1000.0F;
            float maxV = -1000.0F;
            for (ModelPart.Vertex vertex : vertices) {
                float vu = vertex.u();
                float vv = vertex.v();
                if (vu < minU) minU = vu;
                if (vv < minV) minV = vv;
                if (vu > maxU) maxU = vu;
                if (vv > maxV) maxV = vv;
            }

            Object state = livingRenderer.getAndUpdateRenderState(entity, 0.0F);
            Identifier tex = state instanceof LivingEntityRenderState ?
                    livingRenderer.getTexture((LivingEntityRenderState) state) : null;
            if (tex == null || maxU - minU <= 0.0F || maxV - minV <= 0.0F) {
                drawFallbackFace(ctx, entity, x, y, size);
                return;
            }

            float faceW = Math.abs(cuboid.maxX - cuboid.minX);
            float faceH = Math.abs(cuboid.maxY - cuboid.minY);
            float aspect = faceW > 0.0F && faceH > 0.0F ? faceW / faceH : (maxU - minU) / (maxV - minV);
            int drawW;
            int drawH;
            if (aspect >= 1.0F) {
                drawW = size;
                drawH = Math.max(1, (int) (size / aspect));
            } else {
                drawH = size;
                drawW = Math.max(1, (int) (size * aspect));
            }

            int drawX = x + (size - drawW) / 2;
            int drawY = y + (size - drawH) / 2;
            RvRenderUtils.drawTextureUV(ctx.getMatrices(), tex, drawX, drawY, drawW, drawH, minU, minV, maxU, maxV);
        } catch (Throwable e) {
            drawFallbackFace(ctx, entity, x, y, size);
        }
    }

    private ModelPart.Cuboid findHeadCuboid(List<?> cuboids) {
        ModelPart.Cuboid best = null;
        float bestArea = -1.0F;
        for (Object c : cuboids) {
            if (c instanceof ModelPart.Cuboid cuboid) {
                float w = Math.abs(cuboid.maxX - cuboid.minX);
                float h = Math.abs(cuboid.maxY - cuboid.minY);
                float area = w * h;
                if (area > bestArea) {
                    bestArea = area;
                    best = cuboid;
                }
            }
        }
        return best;
    }

    private ModelPart.Quad findFrontQuad(ModelPart.Quad[] sides) {
        for (ModelPart.Quad quad : sides) {
            if (quad != null && quad.direction() != null && quad.direction().z() < -0.5F) {
                return quad;
            }
        }
        return null;
    }

    private Object findHeadPart(net.minecraft.client.model.Model model) {
        try {
            var headOpt = model.getPart("head");
            if (headOpt.isPresent()) {
                return headOpt.get();
            }
        } catch (Throwable ignored) {
        }

        try {
            ModelPart root = model.getRootPart();
            if (root.hasChild("head")) {
                return root.getChild("head");
            }
        } catch (Throwable ignored) {
        }

        try {
            for (ModelPart part : model.getParts()) {
                if (part != null && part.hasChild("head")) {
                    return part.getChild("head");
                }
            }
        } catch (Throwable ignored) {
        }

        return null;
    }

    private static Object getFieldValue(Object obj, String yarnName, String obfName) {
        for (Class<?> c = obj.getClass(); c != null; c = c.getSuperclass()) {
            try {
                Field f = c.getDeclaredField(yarnName);
                f.setAccessible(true);
                return f.get(obj);
            } catch (Throwable ignored) {
            }
            try {
                Field f = c.getDeclaredField(obfName);
                f.setAccessible(true);
                return f.get(obj);
            } catch (Throwable ignored) {
            }
        }
        return null;
    }

    private void drawFallbackFace(DrawContext ctx, LivingEntity entity, int x, int y, int size) {
        try {
            Identifier tex = DefaultSkinHelper.getTexture();
            EntityRenderer<?, ?> renderer = mc.getEntityRenderDispatcher().getRenderer(entity);
            if (renderer instanceof LivingEntityRenderer) {
                Object state = ((LivingEntityRenderer) renderer).getAndUpdateRenderState(entity, 0.0F);
                if (state instanceof LivingEntityRenderState) {
                    Identifier t = ((LivingEntityRenderer) renderer).getTexture((LivingEntityRenderState) state);
                    if (t != null) tex = t;
                }
            }
            RvRenderUtils.drawTextureUV(ctx.getMatrices(), tex, x, y, size, size, 0.0F, 0.0F, 16 / 64.0F, 16 / 64.0F);
        } catch (Throwable ignored) {
        }
    }

    private void drawPlayerHeadFace(DrawContext ctx, PlayerEntity player, int x, int y, int size) {
        try {
            Identifier skinTexture = null;
            if (player instanceof AbstractClientPlayerEntity ac) {
                skinTexture = ac.getSkinTextures().texture();
            }
            if (skinTexture == null) skinTexture = DefaultSkinHelper.getTexture();
            RvRenderUtils.drawTextureUV(ctx.getMatrices(), skinTexture, x, y, size, size, 8 / 64.0F, 8 / 64.0F, 16 / 64.0F, 16 / 64.0F);
            RvRenderUtils.drawTextureUV(ctx.getMatrices(), skinTexture, x, y, size, size, 40 / 64.0F, 8 / 64.0F, 48 / 64.0F, 16 / 64.0F);
        } catch (Throwable ignored) {
        }
    }

    private static String stripFormatting(String text) {
        return text.replaceAll("§[0-9a-fk-or]", "");
    }
}