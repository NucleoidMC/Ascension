package io.github.haykam821.ascension.game;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import io.github.haykam821.ascension.game.map.AscensionMapConfig;
import net.minecraft.SharedConstants;
import net.minecraft.util.math.intprovider.ConstantIntProvider;
import net.minecraft.util.math.intprovider.IntProvider;
import xyz.nucleoid.plasmid.api.game.common.config.WaitingLobbyConfig;

public class AscensionConfig {
	public static final MapCodec<AscensionConfig> CODEC = RecordCodecBuilder.mapCodec(instance -> {
		return instance.group(
			AscensionMapConfig.CODEC.fieldOf("map").forGetter(AscensionConfig::getMapConfig),
			WaitingLobbyConfig.CODEC.fieldOf("players").forGetter(AscensionConfig::getPlayerConfig),
			IntProvider.NON_NEGATIVE_CODEC.optionalFieldOf("ticks_until_close", ConstantIntProvider.create(SharedConstants.TICKS_PER_SECOND * 5)).forGetter(AscensionConfig::getTicksUntilClose),
			Codec.INT.optionalFieldOf("jump_boost_amplifier", 8).forGetter(AscensionConfig::getJumpBoostAmplifier)
		).apply(instance, AscensionConfig::new);
	});

	private final AscensionMapConfig mapConfig;
	private final WaitingLobbyConfig playerConfig;
	private final IntProvider ticksUntilClose;
	private final int jumpBoostAmplifier;

	public AscensionConfig(AscensionMapConfig mapConfig, WaitingLobbyConfig playerConfig, IntProvider ticksUntilClose, int jumpBoostAmplifier) {
		this.mapConfig = mapConfig;
		this.playerConfig = playerConfig;
		this.ticksUntilClose = ticksUntilClose;
		this.jumpBoostAmplifier = jumpBoostAmplifier;
	}

	public AscensionMapConfig getMapConfig() {
		return this.mapConfig;
	}

	public WaitingLobbyConfig getPlayerConfig() {
		return this.playerConfig;
	}

	public IntProvider getTicksUntilClose() {
		return this.ticksUntilClose;
	}

	public int getJumpBoostAmplifier() {
		return this.jumpBoostAmplifier;
	}
}