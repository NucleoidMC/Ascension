package io.github.haykam821.ascension.game.map;

import io.github.haykam821.ascension.game.AscensionConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import xyz.nucleoid.map_templates.BlockBounds;
import xyz.nucleoid.map_templates.MapTemplate;

public class AscensionMapBuilder {
	private static final BlockState AIR_STATE = Blocks.AIR.defaultBlockState();

	private final AscensionConfig config;

	public AscensionMapBuilder(AscensionConfig config) {
		this.config = config;
	}

	public AscensionMap create() {
		MapTemplate template = MapTemplate.createEmpty();
		AscensionMapConfig mapConfig = this.config.getMapConfig();

		int y = ((mapConfig.getLayers() - 1) * mapConfig.getLayerSpacing()) + 1;
		BlockBounds bounds = BlockBounds.of(BlockPos.ZERO, new BlockPos(mapConfig.getX(), y, mapConfig.getZ()));
		this.build(bounds, template, mapConfig);

		return new AscensionMap(template, bounds);
	}

	private BlockState getBlockState(BlockPos pos, RandomSource random) {
		if (pos.getY() == 0) {
			return this.config.getMapConfig().getFloorProvider().getState(null, random, pos);
		} else if (random.nextInt(12) == 0) {
			return BuiltInRegistries.BLOCK.get(BlockTags.WOOL)
				.flatMap(wool -> wool.getRandomElement(random))
				.map(Holder::value)
				.map(Block::defaultBlockState)
				.orElse(AIR_STATE);
		} else {
			return AIR_STATE;
		}
	}

	public void build(BlockBounds bounds, MapTemplate template, AscensionMapConfig mapConfig) {
		RandomSource random = RandomSource.createThreadLocalInstance();

		for (BlockPos pos : bounds) {
			if (pos.getY() % mapConfig.getLayerSpacing() == 0) {
				BlockState state = this.getBlockState(pos, random);
				template.setBlockState(pos, state);
			}
		}
	}
}
