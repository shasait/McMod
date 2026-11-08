package de.hasait.mcmod.client;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.hasait.mcmod.McMod;
import de.hasait.mcmod.config.McModConfig;
import de.hasait.mcmod.config.McModConfigNetworking;

public class McModClient implements ClientModInitializer {
    public static final Logger LOGGER = LoggerFactory.getLogger(McMod.MOD_ID);

    private static volatile McModConfig serverConfig;
    private static volatile boolean serverConfigEditable;

    /**
     * @return the config last received from the connected server, or null if not connected to a server with this mod
     */
    public static McModConfig getServerConfig() {
        return serverConfig;
    }

    public static boolean isServerConfigEditable() {
        return serverConfigEditable;
    }

    public static void sendConfigUpdate(McModConfig config) {
        ClientPlayNetworking.send(new McModConfigNetworking.ConfigUpdatePayload(config));
    }

    @Override
    public void onInitializeClient() {
        // This entrypoint is suitable for setting up client-specific logic, such as rendering.
        LOGGER.info("Initializing client...");

        ClientPlayNetworking.registerGlobalReceiver(McModConfigNetworking.ConfigSyncPayload.TYPE, (payload, context) -> {
            serverConfig = payload.config();
            serverConfigEditable = payload.editable();
        });
        ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> {
            serverConfig = null;
            serverConfigEditable = false;
        });
    }
}
