package de.hasait.mcmod.client;

import net.fabricmc.api.ClientModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.hasait.mcmod.McMod;

public class McModClient implements ClientModInitializer {
    public static final Logger LOGGER = LoggerFactory.getLogger(McMod.MOD_ID);

    @Override
    public void onInitializeClient() {
        // This entrypoint is suitable for setting up client-specific logic, such as rendering.
        LOGGER.info("onInitializeClient");
    }
}
