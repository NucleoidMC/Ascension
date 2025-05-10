package io.github.haykam821.ascension.game.map;

import io.github.haykam821.ascension.game.AscensionConfig;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.registry.Registries;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;
import xyz.nucleoid.map_templates.BlockBounds;
import xyz.nucleoid.map_templates.MapTemplate;

public class AscensionMapBuilder {
	private static final BlockState AIR_STATE = Blocks.AIR.getDefaultState();

	private final AscensionConfig config;

	public AscensionMapBuilder(AscensionConfig config) {
		this.config = config;
	}

	public AscensionMap create() {
		MapTemplate template = MapTemplate.createEmpty();
		AscensionMapConfig mapConfig = this.config.getMapConfig();

		int y = ((mapConfig.getLayers() - 1) * mapConfig.getLayerSpacing()) + 1;
		BlockBounds bounds = BlockBounds.of(BlockPos.ORIGIN, new BlockPos(mapConfig.getX(), y, mapConfig.getZ()));
		this.build(bounds, template, mapConfig);

		return new AscensionMap(template, bounds);
	}

	private BlockState getBlockState(BlockPos pos, Random random) {
		if (pos.getY() == 0) {
			return this.config.getMapConfig().getFloorProvider().get(random, pos);
		} else if (random.nextInt(12) == 0) {
			return Registries.BLOCK.getOptional(BlockTags.WOOL)
				.flatMap(wool -> wool.getRandom(random))
				.map(RegistryEntry::value)
				.map(Block::getDefaultState)
				.orElse(AIR_STATE);
		} else {
			return AIR_STATE;
		}
	}

	public void build(BlockBounds bounds, MapTemplate template, AscensionMapConfig mapConfig) {
		Random random = Random.createLocal();

		for (BlockPos pos : bounds) {
			if (pos.getY() % mapConfig.getLayerSpacing() == 0) {
				BlockState state = this.getBlockState(pos, random);
				template.setBlockState(pos, state);
			}
		}
	}
}
