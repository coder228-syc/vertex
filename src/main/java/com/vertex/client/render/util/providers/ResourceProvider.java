package com.vertex.client.render.util.providers;
import net.minecraft.client.gl.Defines;
import net.minecraft.client.gl.ShaderProgramKey;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.util.Identifier;

public final class ResourceProvider {
	public static final ShaderProgramKey TEXTURE_SHADER_KEY = new ShaderProgramKey(getShaderIdentifier("texture"), VertexFormats.POSITION_TEXTURE_COLOR, Defines.EMPTY);
	public static final ShaderProgramKey RECTANGLE_SHADER_KEY = new ShaderProgramKey(getShaderIdentifier("rectangle"), VertexFormats.POSITION_COLOR, Defines.EMPTY);
	public static final ShaderProgramKey BLUR_SHADER_KEY = new ShaderProgramKey(getShaderIdentifier("blur"), VertexFormats.POSITION_COLOR, Defines.EMPTY);
	public static final ShaderProgramKey RECTANGLE_BORDER_SHADER_KEY = new ShaderProgramKey(getShaderIdentifier("border"), VertexFormats.POSITION_COLOR, Defines.EMPTY);
	public static final ShaderProgramKey GLASS_SHADER_KEY = new ShaderProgramKey(getGlass("data"), VertexFormats.POSITION_TEXTURE_COLOR, Defines.EMPTY);
	public static final ShaderProgramKey METANOISE_SHADER_KEY = new ShaderProgramKey(getShaderIdentifier("metanoise"), VertexFormats.POSITION_COLOR, Defines.EMPTY);
	public static final ShaderProgramKey ZOOMBLUR_SHADER_KEY = new ShaderProgramKey(getShaderIdentifier("zoomblur"), VertexFormats.POSITION_COLOR, Defines.EMPTY);
	public static final ShaderProgramKey HBLUR_SHADER_KEY = new ShaderProgramKey(getShaderIdentifier("hblur"), VertexFormats.POSITION_COLOR, Defines.EMPTY);
	public static final ShaderProgramKey VBLUR_SHADER_KEY = new ShaderProgramKey(getShaderIdentifier("vblur"), VertexFormats.POSITION_COLOR, Defines.EMPTY);
	public static final ShaderProgramKey JUMPCIRCLE_SHADER_KEY = new ShaderProgramKey(getShaderIdentifier("jumpcircle"), VertexFormats.POSITION_COLOR, Defines.EMPTY);
	public static final ShaderProgramKey KAWASE_DOWN_SHADER_KEY = new ShaderProgramKey(getShaderIdentifier("kawase_down"), VertexFormats.POSITION_COLOR, Defines.EMPTY);
	public static final ShaderProgramKey KAWASE_UP_SHADER_KEY = new ShaderProgramKey(getShaderIdentifier("kawase_up"), VertexFormats.POSITION_COLOR, Defines.EMPTY);
	public static final ShaderProgramKey BLURRED_ROUND_RECT_SHADER_KEY = new ShaderProgramKey(getShaderIdentifier("blurred_round_rect"), VertexFormats.POSITION_COLOR, Defines.EMPTY);

	public static final Identifier firefly = Identifier.of("vertexclient", "images/particles/firefly.png");
	public static final Identifier bloom = Identifier.of("vertexclient", "images/particles/bloom.png");
	public static final Identifier snowflake = Identifier.of("vertexclient", "images/particles/snowflake.png");
	public static final Identifier dollar = Identifier.of("vertexclient", "images/particles/dollar.png");
	public static final Identifier heart = Identifier.of("vertexclient", "images/particles/heart.png");
	public static final Identifier star = Identifier.of("vertexclient", "images/particles/star.png");
	public static final Identifier spark = Identifier.of("vertexclient", "images/particles/spark.png");
	public static final Identifier crown = Identifier.of("vertexclient", "images/particles/crown.png");
	public static final Identifier lightning = Identifier.of("vertexclient", "images/particles/lightning.png");
	public static final Identifier line = Identifier.of("vertexclient", "images/particles/line.png");
	public static final Identifier point = Identifier.of("vertexclient", "images/particles/point.png");
	public static final Identifier rhombus = Identifier.of("vertexclient", "images/particles/rhombus.png");


	public static final Identifier targetEspGlow = Identifier.of("vertexclient", "images/targetesp/glow.png");
	public static final Identifier targetEsp1 = Identifier.of("vertexclient", "images/targetesp/target1.png");
	public static final Identifier targetEsp2 = Identifier.of("vertexclient", "images/targetesp/target2.png");
	public static final Identifier targetEsp3 = Identifier.of("vertexclient", "images/targetesp/target3.png");
	public static final Identifier targetEsp4 = Identifier.of("vertexclient", "images/targetesp/target4.png");


	public static final Identifier CUSTOM_CAPE = Identifier.of("vertexclient", "cape/cape.png");
	public static final Identifier CUSTOM_ELYTRA = Identifier.of("vertexclient", "cape/elytra.png");

	public static final Identifier container = Identifier.of("vertexclient", "images/hud/container.png");

	public static final Identifier color_image = Identifier.of("vertexclient", "images/gui/pick.png");


	private static Identifier getGlass(String name) {
		return Identifier.of("vertexclient", "core/glass/" + name);
	}
	private static Identifier getShaderIdentifier(String name) {
		return Identifier.of("vertexclient", "core/" + name);
	}
}