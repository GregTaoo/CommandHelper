package com.everlastsino.comhelper;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v1.CommandRegistrationCallback;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class comhelper implements ModInitializer {
	public static final Logger LOGGER = LogManager.getLogger("comhelper");

	@Override
	public void onInitialize() {
		LOGGER.info("Hello CommandHelper world!");
		try {
			CHConfig.loadConfigs();
		} catch (Exception e) {
			e.printStackTrace();
		}
		CommandRegistrationCallback.EVENT.register((dispatcher, dedicated) -> HelperCommands.register(dispatcher, LOGGER));
	}
}
