package mors.museumguide;

import mors.museumguide.entity.ModEntityTypes;
import mors.museumguide.tools.signTools;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.loader.api.FabricLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MuseumGuide implements ModInitializer {
	public static final String MOD_ID = "museumguide";

	// This logger is used to write text to the console and the log file.
	// It is considered best practice to use your mod id as the logger's name.
	// That way, it's clear which mod wrote info, warnings, and errors.
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitialize() {
		// Load and log Wikidata API configuration
		LOGGER.info("Wikidata API URL: {}", Config.WIKIDATA_API_URL);
		LOGGER.info("Wikidata Max Results: {}", Config.WIKIDATA_MAX_RESULTS);
		LOGGER.info("Wikidata User Agent: {}", Config.WIKIDATA_USER_AGENT);
		if (FabricLoader.getInstance().getEnvironmentType() == EnvType.CLIENT) {
			// Client-only code here
			LOGGER.info("Hello Fabric world!");
		}
		ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
			signTools.cacheSignsAroundPlayer(handler.getPlayer());
		});
		ModEntityTypes.registerModEntityTypes();
		ModEntityTypes.registerAttributes();
		// This code runs as soon as Minecraft is in a mod-load-ready state.
		// However, some things (like resources) may still be uninitialized.
		// Proceed with mild caution.
	}
}

