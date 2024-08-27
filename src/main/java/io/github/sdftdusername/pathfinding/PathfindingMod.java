package io.github.sdftdusername.pathfinding;

import dev.crmodders.cosmicquilt.api.entrypoint.ModInitializer;
import org.quiltmc.loader.api.ModContainer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PathfindingMod implements ModInitializer {
	public static final Logger LOGGER = LoggerFactory.getLogger("Pathfinding");

	@Override
	public void onInitialize(ModContainer mod) {
		LOGGER.info("Pathfinding Initialized!");
		CommandsMod.RegisterCommands();
		EntitiesMod.RegisterEntities();
	}
}

