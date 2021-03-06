package io.github.haykam821.ascension.game.map;

import net.minecraft.server.MinecraftServer;
import net.minecraft.world.gen.chunk.ChunkGenerator;
import xyz.nucleoid.plasmid.map.template.MapTemplate;
import xyz.nucleoid.plasmid.map.template.TemplateChunkGenerator;
import xyz.nucleoid.plasmid.util.BlockBounds;

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
