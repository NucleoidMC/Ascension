package io.github.haykam821.ascension.game.phase;

import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.Set;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.level.GameType;
import net.minecraft.world.phys.Vec3;
import com.google.common.collect.Sets;

import io.github.haykam821.ascension.game.AscensionConfig;
import io.github.haykam821.ascension.game.map.AscensionMap;
import xyz.nucleoid.plasmid.api.game.GameActivity;
import xyz.nucleoid.plasmid.api.game.GameCloseReason;
import xyz.nucleoid.plasmid.api.game.GameSpace;
import xyz.nucleoid.plasmid.api.game.event.GameActivityEvents;
import xyz.nucleoid.plasmid.api.game.event.GamePlayerEvents;
import xyz.nucleoid.plasmid.api.game.player.JoinAcceptor;
import xyz.nucleoid.plasmid.api.game.player.JoinAcceptorResult;
import xyz.nucleoid.plasmid.api.game.player.JoinOffer;
import xyz.nucleoid.plasmid.api.game.rule.GameRuleType;
import xyz.nucleoid.stimuli.event.EventResult;
import xyz.nucleoid.stimuli.event.player.PlayerDeathEvent;

public class AscensionActivePhase {
	private static final DecimalFormat FORMAT = new DecimalFormat("0.##");

	private final GameSpace gameSpace;
	private final ServerLevel world;
	private final AscensionMap map;
	private final AscensionConfig config;
	private final Set<ServerPlayer> players;
	private int ticksElapsed = 0;
	private int ticksUntilClose = -1;

	public AscensionActivePhase(GameSpace gameSpace, ServerLevel world, AscensionMap map, AscensionConfig config, Set<ServerPlayer> players) {
		this.gameSpace = gameSpace;
		this.world = world;
		this.map = map;
		this.config = config;
		this.players = players;
	}

	public static void setRules(GameActivity activity) {
		activity.deny(GameRuleType.CRAFTING);
		activity.deny(GameRuleType.FALL_DAMAGE);
		activity.deny(GameRuleType.HUNGER);
		activity.deny(GameRuleType.PORTALS);
		activity.deny(GameRuleType.PVP);
	}

	public static void open(GameSpace gameSpace, ServerLevel world, AscensionMap map, AscensionConfig config) {
		AscensionActivePhase phase = new AscensionActivePhase(gameSpace, world, map, config, Sets.newHashSet(gameSpace.getPlayers().participants()));

		gameSpace.setActivity(activity -> {
			AscensionActivePhase.setRules(activity);

			// Listeners
			activity.listen(GameActivityEvents.ENABLE, phase::enable);
			activity.listen(GameActivityEvents.TICK, phase::tick);
			activity.listen(GamePlayerEvents.ACCEPT, phase::onAcceptPlayers);
			activity.listen(GamePlayerEvents.OFFER, JoinOffer::acceptSpectators);
			activity.listen(GamePlayerEvents.REMOVE, phase::removePlayer);
			activity.listen(PlayerDeathEvent.EVENT, phase::onPlayerDeath);
		});
	}

	public void enable() {
 		for (ServerPlayer player : this.players) {
			player.setGameMode(GameType.ADVENTURE);
		
			MobEffectInstance jumpBoost = new MobEffectInstance(MobEffects.JUMP_BOOST, MobEffectInstance.INFINITE_DURATION, this.config.getJumpBoostAmplifier(), true, false, false);
			player.addEffect(jumpBoost);
		
			AscensionActivePhase.spawn(this.world, this.map, player);
		}

		for (ServerPlayer player : this.gameSpace.getPlayers().spectators()) {
			AscensionActivePhase.spawn(this.world, this.map, player);
			this.setSpectator(player);
		}
	}

	private int getEndY() {
		return this.map.getBounds().max().getY();
	}

	private boolean isFinished(ServerPlayer player) {
		return player.getY() > this.getEndY();
	}

	private Component getWinMessage(ServerPlayer player) {
		String time = AscensionActivePhase.FORMAT.format(this.ticksElapsed / (double) 20);
		return player.getDisplayName().copy().append(" has won the game in " + time + " seconds!").withStyle(ChatFormatting.GOLD);
	}

	private void tick() {
		// Decrease ticks until game end to zero
		if (this.isGameEnding()) {
			if (this.ticksUntilClose == 0) {
				this.gameSpace.close(GameCloseReason.FINISHED);
			}

			this.ticksUntilClose -= 1;
		} else {
			for (ServerPlayer player : this.players) {
				if (this.isFinished(player)) {
					// Send win message
					Component message = this.getWinMessage(player);
					this.gameSpace.getPlayers().sendMessage(message);

					this.ticksUntilClose = this.config.getTicksUntilClose().sample(this.world.getRandom());
					break;
				}
			}
		}

		int minY = this.map.getBounds().min().getY();

		for (ServerPlayer player : this.players) {
			if (player.getY() < minY) {
				AscensionActivePhase.spawn(this.world, this.map, player);
			}

			player.experienceProgress = (float) Mth.clamp(player.getY() / this.getEndY(), 0, 1);
			player.setExperienceLevels((int) Math.max(0, this.getEndY() - player.getY()));
		}

		this.ticksElapsed += 1;
	}

	private boolean isGameEnding() {
		return this.ticksUntilClose >= 0;
	}

	private void setSpectator(ServerPlayer player) {
		player.setGameMode(GameType.SPECTATOR);
	}

	public JoinAcceptorResult onAcceptPlayers(JoinAcceptor acceptor) {
		return acceptor.teleport(this.world, AscensionActivePhase.getSpawnPos(this.world, this.map)).thenRunForEach(player -> {
			this.setSpectator(player);
		});
	}

	private void removePlayer(ServerPlayer player) {
		this.players.remove(player);
	}

	private EventResult onPlayerDeath(ServerPlayer player, DamageSource source) {
		AscensionActivePhase.spawn(this.world, this.map, player);
		return EventResult.ALLOW;
	}

	public static Vec3 getSpawnPos(ServerLevel world, AscensionMap map) {
		BlockPos min = map.getBounds().min();
		BlockPos max = map.getBounds().max();

		int x = world.getRandom().nextInt(max.getX() - min.getX()) + min.getX();
		int z = world.getRandom().nextInt(max.getZ() - min.getZ()) + min.getZ();

		return new Vec3(x + 0.5, 1, z + 0.5);
	}

	public static void spawn(ServerLevel world, AscensionMap map, ServerPlayer player) {
		Vec3 spawnPos = AscensionActivePhase.getSpawnPos(world, map);
		player.teleportTo(world, spawnPos.x(), spawnPos.y(), spawnPos.z(), Set.of(), 0, 0, true);
	}

	static {
		FORMAT.setRoundingMode(RoundingMode.DOWN);
	}
}
