package com.vertex.client.render.font;

import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;
import com.mojang.blaze3d.systems.RenderSystem;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.client.texture.NativeImageBackedTexture;
import net.minecraft.util.Identifier;
import org.lwjgl.opengl.GL11;

import java.io.InputStream;
import java.io.Reader;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

public final class MsdfFont {

    private static final Gson GSON = new Gson();
    private static final ConcurrentHashMap<String, MsdfFont> CACHE = new ConcurrentHashMap<>();

    private final String name;
    private final Identifier textureId;
    private final float atlasSize;
    private final float ascenderEm;
    private final float descenderEm;
    private final float lineHeightEm;
    private final Int2ObjectMap<Glyph> glyphs;
    private final Glyph fallback;

    private MsdfFont(String name, FontFile file) {
        this.name = name;
        this.textureId = Identifier.of("vertexclient", "fonts/baked/" + name);
        this.atlasSize = file.atlas.size;
        this.ascenderEm = (float) file.metrics.ascender;
        this.descenderEm = (float) file.metrics.descender;
        this.lineHeightEm = (float) file.metrics.lineHeight;
        this.glyphs = new Int2ObjectOpenHashMap<>(file.glyphs.size());
        for (RawGlyph raw : file.glyphs) {
            this.glyphs.put(raw.unicode, Glyph.bake(raw, file.atlas.width, file.atlas.height));
        }
        Glyph fb = this.glyphs.get(63);
        this.fallback = fb != null ? fb : new Glyph(0.26f, null, 0f, 0f, 0f, 0f);
        bakeCoverage(file.atlas.width, file.atlas.height);
    }

    public static MsdfFont get(String family, int weight) {
        String name = family + "_" + styleName(weight);
        return CACHE.computeIfAbsent(name, MsdfFont::readFont);
    }

    private static String styleName(int weight) {
        if (weight >= 700) return "bold";
        if (weight >= 600) return "semibold";
        if (weight >= 500) return "medium";
        return "regular";
    }

    private static MsdfFont readFont(String name) {
        try {
            Reader reader = MinecraftClient.getInstance().getResourceManager()
                    .getResource(Identifier.of("vertexclient", "fonts/" + name + ".json")).get().getReader();
            try (reader) {
                FontFile file = GSON.fromJson(reader, FontFile.class);
                if (file == null || file.atlas == null || file.metrics == null || file.glyphs == null
                        || file.atlas.width <= 0 || file.atlas.height <= 0) {
                    throw new IllegalStateException("Invalid MSDF font metadata: " + name);
                }
                return new MsdfFont(name, file);
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to read MSDF font " + name, e);
        }
    }

    private void bakeCoverage(int width, int height) {
        MinecraftClient mc = MinecraftClient.getInstance();
        try (InputStream in = mc.getResourceManager().open(Identifier.of("vertexclient", "fonts/" + name + ".png"));
             NativeImage img = NativeImage.read(in)) {
            int w = img.getWidth();
            int h = img.getHeight();
            if (w <= 0 || h <= 0) return;

            NativeImage out = new NativeImage(NativeImage.Format.RGBA, w, h, false);
            for (int x = 0; x < w; x++) {
                for (int y = 0; y < h; y++) {
                    int c = img.getColorArgb(x, y);
                    int a = (c >>> 24) & 0xFF;
                    out.setColorArgb(x, y, (0xFF << 24) | (a << 16) | (a << 8) | a);
                }
            }

            NativeImageBackedTexture texture = new NativeImageBackedTexture(out);
            texture.upload();
            mc.getTextureManager().registerTexture(textureId, texture);

            RenderSystem.bindTexture(texture.getGlId());
            GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR);
            GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_LINEAR);
        } catch (Exception e) {
            throw new RuntimeException("Failed to bake MSDF font " + name, e);
        }
    }

    public Identifier textureId() {
        return textureId;
    }

    public float atlasSize() {
        return atlasSize;
    }

    public float ascenderEm() {
        return ascenderEm;
    }

    public float descenderEm() {
        return descenderEm;
    }

    public float lineHeightEm() {
        return lineHeightEm;
    }

    public Glyph glyph(int codePoint) {
        Glyph glyph = this.glyphs.get(codePoint);
        return glyph != null ? glyph : fallback;
    }

    public record Bounds(float left, float bottom, float right, float top) {
    }

    public record Glyph(float advanceEm, Bounds plane, float u0, float v0, float u1, float v1) {

        static Glyph bake(RawGlyph raw, int atlasWidth, int atlasHeight) {
            Bounds plane = raw.planeBounds != null && raw.atlasBounds != null
                    ? new Bounds((float) raw.planeBounds.left, (float) raw.planeBounds.bottom,
                    (float) raw.planeBounds.right, (float) raw.planeBounds.top)
                    : null;
            float u0 = 0f;
            float v0 = 0f;
            float u1 = 0f;
            float v1 = 0f;
            if (plane != null) {
                u0 = (float) (raw.atlasBounds.left / atlasWidth);
                v0 = (float) (1.0 - raw.atlasBounds.top / atlasHeight);
                u1 = (float) (raw.atlasBounds.right / atlasWidth);
                v1 = (float) (1.0 - raw.atlasBounds.bottom / atlasHeight);
            }
            return new Glyph((float) raw.advance, plane, u0, v0, u1, v1);
        }
    }

    private static final class FontFile {
        private Atlas atlas;
        private Metrics metrics;
        private List<RawGlyph> glyphs;
    }

    private static final class Atlas {
        private double distanceRange;
        private int width;
        private int height;
        private int size;
    }

    private static final class Metrics {
        private double lineHeight;
        private double ascender;
        private double descender;
    }

    private static final class RawBounds {
        private double left;
        private double bottom;
        private double right;
        private double top;
    }

    private static final class RawGlyph {
        private int unicode;
        private double advance;
        private RawBounds planeBounds;
        private RawBounds atlasBounds;
    }
}