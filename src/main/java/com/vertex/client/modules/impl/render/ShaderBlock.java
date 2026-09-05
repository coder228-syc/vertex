package com.vertex.client.modules.impl.render;

import com.mojang.blaze3d.systems.RenderSystem;
import com.vertex.client.modules.Module;
import com.vertex.client.modules.ModuleCategory;
import com.vertex.client.modules.ModuleInfo;
import com.vertex.client.modules.ModuleManager;
import com.vertex.client.modules.setting.BooleanSetting;
import com.vertex.client.modules.setting.ModeSetting;
import com.vertex.client.modules.setting.SliderSetting;
import com.vertex.client.render.util.RenderUtil;
import com.vertex.client.util.IMinecraft;
import net.minecraft.block.BlockState;
import net.minecraft.client.gl.Defines;
import net.minecraft.client.gl.GlUniform;
import net.minecraft.client.gl.ShaderProgram;
import net.minecraft.client.gl.ShaderProgramKey;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.BufferRenderer;
import net.minecraft.client.render.BuiltBuffer;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.VertexFormat;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.util.Identifier;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.World;
import org.joml.Matrix4f;
import org.joml.Matrix4fStack;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.opengl.GL11;

@ModuleInfo(name = "ShaderBlock", desc = "Stylish animated block outline", type = ModuleCategory.Render, key = GLFW.GLFW_KEY_UNKNOWN)
public class ShaderBlock extends Module {
    public static ShaderBlock INSTANCE;

    public final BooleanSetting ignoreDepth = new BooleanSetting("Ignore Depth", false);

    public final SliderSetting animationSpeed = new SliderSetting("Animation Speed", 15, 1, 30, 1);
    public final SliderSetting transitionSpeed = new SliderSetting("Transition Speed", 10, 1, 30, 0.5);
    public final SliderSetting shaderSpeed = new SliderSetting("Shader Speed", 1.0, 0.1, 3.0, 0.05);
    public final SliderSetting shaderIntensity = new SliderSetting("Shader Intensity", 1.5, 0.1, 3.0, 0.05);

    public final ModeSetting style = new ModeSetting("Style", "Night", "Night", "Classic", "Caustics", "Prismatic", "Glossy", "Deep Space", "Nebula");

    private static final float BOX_EPSILON = 0.0025F;
    private static final float FADE_RATE = 7.5F;

    private static final ShaderProgramKey NIGHT_KEY = key("night");
    private static final ShaderProgramKey CLASSIC_KEY = key("classic");
    private static final ShaderProgramKey CAUSTICS_KEY = key("caustics");
    private static final ShaderProgramKey PRISMATIC_KEY = key("prismatic");
    private static final ShaderProgramKey GLOSSY_KEY = key("glossy");
    private static final ShaderProgramKey DEEP_SPACE_KEY = key("deep_space");
    private static final ShaderProgramKey NEBULA_KEY = key("nebula");

    private BlockPos selectedPos;
    private BlockState selectedState;
    private long lastFrameNanos;
    private float transition;
    private float fade;

    public ShaderBlock() {
        INSTANCE = this;
        addSettings(ignoreDepth, animationSpeed, transitionSpeed, shaderSpeed, shaderIntensity, style);
    }

    @Override
    public void onEvent(Object event) {}

    @Override
    protected void onDisable() {
        this.release();
    }

    private static ShaderProgramKey key(String path) {
        return new ShaderProgramKey(Identifier.of("vertexclient", "core/block_outline/" + path), VertexFormats.POSITION, Defines.EMPTY);
    }

    public void render(Camera camera, Matrix4f positionMatrix) {
        if (mc.world != null && mc.getFramebuffer() != null) {
            BlockPos blockPos = null;
            BlockState state = null;
            if (mc.crosshairTarget instanceof BlockHitResult hit && hit.getType() == HitResult.Type.BLOCK) {
                BlockPos hitPos = hit.getBlockPos();
                if (!mc.world.getBlockState(hitPos).isAir()) {
                    blockPos = hitPos;
                    state = mc.world.getBlockState(hitPos);
                }
            }

            float seconds = this.stepSeconds(System.nanoTime());
            if (blockPos == null) {
                if (this.selectedPos == null) {
                    return;
                }
                this.fade = Math.max(0.0F, this.fade - seconds * FADE_RATE);
                if (this.fade <= 0.0F) {
                    this.resetSelection();
                    return;
                }
            } else {
                if (this.selectedPos == null) {
                    this.selectedPos = new BlockPos(blockPos);
                    this.transition = 0.0F;
                } else if (!blockPos.equals(this.selectedPos)) {
                    this.selectedPos = new BlockPos(blockPos);
                }

                this.selectedState = state;

                this.fade = Math.min(1.0F, this.fade + seconds * FADE_RATE);
                this.transition = Math.min(1.0F, this.transition + seconds * this.animationSpeed.get().floatValue() * 0.12F);
            }

            if (this.selectedPos != null) {
                Box target = this.resolveTargetBox(mc.world, this.selectedPos);
                if (target != null) {
                    Vec3d camPos = camera.getPos();
                    Box relative = target.expand(BOX_EPSILON)
                            .offset(-camPos.x, -camPos.y, -camPos.z);
                    this.draw(positionMatrix, relative);
                }
            }
        } else {
            this.resetSelection();
        }
    }

    private void draw(Matrix4f viewMatrix, Box relative) {
        int width = mc.getFramebuffer().textureWidth;
        int height = mc.getFramebuffer().textureHeight;
        if (width <= 0 || height <= 0) {
            return;
        }

        Matrix4fStack modelViewStack = RenderSystem.getModelViewStack();
        modelViewStack.pushMatrix();
        modelViewStack.identity();
        modelViewStack.mul(viewMatrix);

        boolean ignoreDepth = this.ignoreDepth.get();

        RenderUtil.enableRender();
        if (ignoreDepth) {
            RenderSystem.disableDepthTest();
        } else {
            RenderSystem.enableDepthTest();
            RenderSystem.depthFunc(GL11.GL_LEQUAL);
            RenderSystem.depthMask(false);
        }
        try {
            ShaderProgram program = RenderSystem.setShader(this.pipeline());
            if (program == null) {
                return;
            }
            this.writeUniforms(program, width, height);
            BuiltBuffer mesh = this.buildMesh(relative);
            if (mesh != null) {
                try {
                    BufferRenderer.drawWithGlobalProgram(mesh);
                } finally {
                    mesh.close();
                }
            }
        } finally {
            if (ignoreDepth) {
                RenderSystem.enableDepthTest();
            }
            RenderSystem.depthMask(true);
            RenderUtil.disableRender();
            modelViewStack.popMatrix();
        }
    }

    public void resetSelection() {
        this.selectedPos = null;
        this.selectedState = null;
        this.transition = 0.0F;
        this.fade = 0.0F;
        this.lastFrameNanos = 0L;
    }

    public void release() {
        this.resetSelection();
    }

    private float stepSeconds(long now) {
        if (this.lastFrameNanos == 0L) {
            this.lastFrameNanos = now;
            return 0.0F;
        } else {
            float seconds = Math.min(0.1F, (float) (now - this.lastFrameNanos) / 1.0E9F);
            this.lastFrameNanos = now;
            return seconds;
        }
    }

    private void writeUniforms(ShaderProgram program, int width, int height) {
        float[] accent = accentRgb();
        setUniform(program, "Tint", accent[0], accent[1], accent[2], this.fade * 0.82F);
        setUniform(program, "Params", width, height,
                shaderTime() * this.shaderSpeed.get().floatValue(),
                this.shaderIntensity.get().floatValue());
    }

    private float[] accentRgb() {
        int c = ModuleManager.STYLE_MANAGER != null ? ModuleManager.STYLE_MANAGER.getFirstColor() : 0xFFFFFFFF;
        return new float[]{(c >> 16 & 255) / 255.0F, (c >> 8 & 255) / 255.0F, (c & 255) / 255.0F};
    }

    private ShaderProgramKey pipeline() {
        return switch (this.style.get()) {
            case "Classic" -> CLASSIC_KEY;
            case "Caustics" -> CAUSTICS_KEY;
            case "Prismatic" -> PRISMATIC_KEY;
            case "Glossy" -> GLOSSY_KEY;
            case "Deep Space" -> DEEP_SPACE_KEY;
            case "Nebula" -> NEBULA_KEY;
            default -> NIGHT_KEY;
        };
    }

    private BuiltBuffer buildMesh(Box relative) {
        if (relative == null || relative.isNaN()) {
            return null;
        }

        BufferBuilder builder = IMinecraft.tessellator().begin(VertexFormat.DrawMode.TRIANGLES, VertexFormats.POSITION);

        float minX = (float) relative.minX;
        float minY = (float) relative.minY;
        float minZ = (float) relative.minZ;
        float maxX = (float) relative.maxX;
        float maxY = (float) relative.maxY;
        float maxZ = (float) relative.maxZ;
        this.face(builder, minX, minY, minZ, maxX, maxY, minZ);
        this.face(builder, maxX, minY, maxZ, minX, maxY, maxZ);
        this.face(builder, minX, minY, maxZ, minX, maxY, minZ);
        this.face(builder, maxX, minY, minZ, maxX, maxY, maxZ);
        this.face(builder, minX, maxY, minZ, maxX, maxY, maxZ);
        this.face(builder, minX, minY, maxZ, maxX, minY, minZ);

        return builder.endNullable();
    }

    private Box resolveTargetBox(World world, BlockPos pos) {
        BlockState state = world.getBlockState(pos);
        VoxelShape outline = state.getOutlineShape(world, pos);
        if (outline == null || outline.isEmpty()) {
            return new Box(pos);
        }

        Box shape = outline.getBoundingBox();
        if (shape.isNaN()
                || (shape.minX >= shape.maxX - 1.0E-7d
                && shape.minY >= shape.maxY - 1.0E-7d
                && shape.minZ >= shape.maxZ - 1.0E-7d)) {
            return new Box(pos);
        }

        return shape.offset(pos.getX(), pos.getY(), pos.getZ());
    }

    private void face(BufferBuilder builder, float x1, float y1, float z1, float x2, float y2, float z2) {
        boolean constantX = x1 == x2;
        boolean constantY = y1 == y2;
        if (constantX) {
            this.vertex(builder, x1, y1, z1);
            this.vertex(builder, x1, y1, z2);
            this.vertex(builder, x1, y2, z2);
            this.vertex(builder, x1, y2, z2);
            this.vertex(builder, x1, y2, z1);
            this.vertex(builder, x1, y1, z1);
        } else if (constantY) {
            this.vertex(builder, x1, y1, z1);
            this.vertex(builder, x2, y1, z1);
            this.vertex(builder, x2, y1, z2);
            this.vertex(builder, x2, y1, z2);
            this.vertex(builder, x1, y1, z2);
            this.vertex(builder, x1, y1, z1);
        } else {
            this.vertex(builder, x1, y1, z1);
            this.vertex(builder, x2, y1, z1);
            this.vertex(builder, x2, y2, z1);
            this.vertex(builder, x2, y2, z1);
            this.vertex(builder, x1, y2, z1);
            this.vertex(builder, x1, y1, z1);
        }
    }

    private void vertex(BufferBuilder builder, float x, float y, float z) {
        builder.vertex(x, y, z);
    }

    private static float shaderTime() {
        return (float) (System.nanoTime() % 1000000000000L) / 1.0E9F;
    }

    private static void setUniform(ShaderProgram program, String name, float... values) {
        try {
            GlUniform uniform = program.getUniform(name);
            if (uniform == null) {
                return;
            }
            if (values.length == 1) {
                uniform.set(values[0]);
            } else if (values.length == 2) {
                uniform.set(values[0], values[1]);
            } else if (values.length == 3) {
                uniform.set(values[0], values[1], values[2]);
            } else if (values.length == 4) {
                uniform.set(values[0], values[1], values[2], values[3]);
            }
        } catch (Exception ignored) {
        }
    }
}