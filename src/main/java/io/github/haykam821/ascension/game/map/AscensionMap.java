package io.github.haykam821.ascension.game.map;

import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.chunk.ChunkGenerator;
import xyz.nucleoid.map_templates.BlockBounds;
import xyz.nucleoid.map_templates.MapTemplate;
import xyz.nucleoid.plasmid.api.game.level.generator.TemplateChunkGenerator;

public class AscensionMap {
	private final MapTemplate template;
	private final BlockBounds bounds;

	public AscensionMap(MapTemplate template, BlockBounds bounds) {
		this.template = template;
		this.bounds = bounds;
	}

	public BlockBounds getBounds() {
		return this.bounds;
	}

	public ChunkGenerator createGenerator(MinecraftServer server) {
		return new TemplateChunkGenerator(server, this.template);
	}
}
