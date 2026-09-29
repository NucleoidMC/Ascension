package io.github.haykam821.ascension.game.phase;

import io.github.haykam821.ascension.game.AscensionConfig;
import io.github.haykam821.ascension.game.map.AscensionMap;
import io.github.haykam821.ascension.game.map.AscensionMapBuilder;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.level.GameType;
import xyz.nucleoid.fantasy.RuntimeLevelConfig;
import xyz.nucleoid.plasmid.api.game.GameOpenContext;
import xyz.nucleoid.plasmid.api.game.GameOpenProcedure;
import xyz.nucleoid.plasmid.api.game.GameResult;
import xyz.nucleoid.plasmid.api.game.GameSpace;
import xyz.nucleoid.plasmid.api.game.common.GameWaitingLobby;
import xyz.nucleoid.plasmid.api.game.event.GameActivityEvents;
import xyz.nucleoid.plasmid.api.game.event.GamePlayerEvents;
import xyz.nucleoid.plasmid.api.game.player.JoinAcceptor;
import xyz.nucleoid.plasmid.api.game.player.JoinAcceptorResult;
import xyz.nucleoid.plasmid.api.game.player.JoinOffer;
import xyz.nucleoid.stimuli.event.EventResult;
import xyz.nucleoid.stimuli.event.player.PlayerDeathEvent;

public class AscensionWaitingPhase {
	private final GameSpace gameSpace;
	private final ServerLevel world;
	private final AscensionMap map;
	private final AscensionConfig config;

	public AscensionWaitingPhase(GameSpace gameSpace, ServerLevel world, AscensionMap map, AscensionConfig config) {
		this.gameSpace = gameSpace;
		this.world = world;
		this.map = map;
		this.config = config;
	}

	public static GameOpenProcedure open(GameOpenContext<AscensionConfig> context) {
		AscensionMapBuilder mapBuilder = new AscensionMapBuilder(context.config());
		AscensionMap map = mapBuilder.create();

		RuntimeLevelConfig worldConfig = new RuntimeLevelConfig()
			.setGenerator(map.createGenerator(context.server()));

		return context.openWithLevel(worldConfig, (activity, world) -> {
			AscensionWaitingPhase phase = new AscensionWaitingPhase(activity.getGameSpace(), world, map, context.config());

			GameWaitingLobby.addTo(activity, context.config().getPlayerConfig());

			AscensionActivePhase.setRules(activity);

			// Listeners
			activity.listen(GameActivityEvents.TICK, phase::tick);
			activity.listen(GamePlayerEvents.ACCEPT, phase::onAcceptPlayers);
			activity.listen(GamePlayerEvents.OFFER, JoinOffer::accept);
			activity.listen(PlayerDeathEvent.EVENT, phase::onPlayerDeath);
			activity.listen(GameActivityEvents.REQUEST_START, phase::requestStart);
		});
	}

	private void tick() {
		int minY = map.getBounds().min().getY();
		for (ServerPlayer player : this.gameSpace.getPlayers()) {
			if (player.getY() < minY) {
				AscensionActivePhase.spawn(this.world, map, player);
			}
		}
	}

	public GameResult requestStart() {
		AscensionActivePhase.open(this.gameSpace, this.world, this.map, this.config);
		return GameResult.ok();
	}

	public JoinAcceptorResult onAcceptPlayers(JoinAcceptor acceptor) {
		return acceptor.teleport(this.world, AscensionActivePhase.getSpawnPos(this.world, this.map)).thenRunForEach(player -> {
			player.setGameMode(GameType.ADVENTURE);
		});
	}

	public EventResult onPlayerDeath(ServerPlayer player, DamageSource source) {
		// Respawn player at the start
		AscensionActivePhase.spawn(this.world, this.map, player);
		return EventResult.DENY;
	}
}
