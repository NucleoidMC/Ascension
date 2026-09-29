package io.github.haykam821.ascension.game.map;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.feature.stateproviders.BlockStateProvider;

public class AscensionMapConfig {
	public static final Codec<AscensionMapConfig> CODEC = RecordCodecBuilder.create(instance -> {
		return instance.group(
			Codec.INT.fieldOf("x").forGetter(AscensionMapConfig::getX),
			Codec.INT.fieldOf("z").forGetter(AscensionMapConfig::getZ),
			Codec.INT.fieldOf("layers").forGetter(AscensionMapConfig::getLayers),
			Codec.INT.fieldOf("layer_spacing").forGetter(AscensionMapConfig::getLayerSpacing),
			BlockStateProvider.DIRECT_CODEC.optionalFieldOf("floor_provider", BlockStateProvider.of(Blocks.SMOOTH_QUARTZ)).forGetter(AscensionMapConfig::getFloorProvider)
		).apply(instance, AscensionMapConfig::new);
	});

	private final int x;
	private final int z;
	private final int layers;
	private final int layerSpacing;
	private final BlockStateProvider floorProvider;

	public AscensionMapConfig(int x, int z, int layers, int layerSpacing, BlockStateProvider floorProvider) {
		this.x = x;
		this.z = z;
		this.layers = layers;
		this.layerSpacing = layerSpacing;
		this.floorProvider = floorProvider;
	}

	public int getX() {
		return this.x;
	}

	public int getZ() {
		return this.z;
	}

	public int getLayers() {
		return this.layers;
	}

	public int getLayerSpacing() {
		return this.layerSpacing;
	}

	public BlockStateProvider getFloorProvider() {
		return this.floorProvider;
	}
}