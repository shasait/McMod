package de.hasait.mcmod.config;

import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.permissions.Permissions;
import org.slf4j.Logger;

import de.hasait.mcmod.McMod;

/**
 * Synchronizes the server config with clients: the server sends its config to every player on join and after each
 * change, and accepts config updates from privileged players.
 */
public final class McModConfigNetworking {

    private static final Logger LOGGER = McMod.LOGGER;

    private McModConfigNetworking() {
    }

    /**
     * Server to client: current server config, plus whether the receiving player may change it.
     */
    public record ConfigSyncPayload(McModConfig config, boolean editable) implements CustomPacketPayload {
        public static final Type<ConfigSyncPayload> TYPE = new Type<>(McMod.id("config_sync"));
        public static final StreamCodec<RegistryFriendlyByteBuf, ConfigSyncPayload> CODEC = StreamCodec.of(
                (buf, payload) -> {
                    payload.config().toBuf(buf);
                    buf.writeBoolean(payload.editable());
                },
                buf -> new ConfigSyncPayload(new McModConfigBuilder().fromBuf(buf).build(), buf.readBoolean())
        );

        @Override
        public Type<? extends CustomPacketPayload> type() {
            return TYPE;
        }
    }

    /**
     * Client to server: new config values.
     */
    public record ConfigUpdatePayload(McModConfig config) implements CustomPacketPayload {
        public static final Type<ConfigUpdatePayload> TYPE = new Type<>(McMod.id("config_update"));
        public static final StreamCodec<RegistryFriendlyByteBuf, ConfigUpdatePayload> CODEC = StreamCodec.of(
                (buf, payload) -> payload.config.toBuf(buf),
                buf -> new ConfigUpdatePayload(new McModConfigBuilder().fromBuf(buf).build())
        );

        @Override
        public Type<? extends CustomPacketPayload> type() {
            return TYPE;
        }
    }

    /**
     * Registers payload types; must run on both client and server.
     */
    public static void registerPayloads() {
        PayloadTypeRegistry.clientboundPlay().register(ConfigSyncPayload.TYPE, ConfigSyncPayload.CODEC);
        PayloadTypeRegistry.serverboundPlay().register(ConfigUpdatePayload.TYPE, ConfigUpdatePayload.CODEC);
    }

    public static void registerServerHandlers() {
        ServerPlayConnectionEvents.JOIN.register(
                (handler, sender, server) ->
                        sendConfig(server, handler.getPlayer(), McMod.getConfig())
        );

        ServerPlayNetworking.registerGlobalReceiver(ConfigUpdatePayload.TYPE, (payload, context) -> {
            ServerPlayer player = context.player();
            MinecraftServer server = context.server();
            String playerName = player.getGameProfile().name();
            if (!canEdit(server, player)) {
                McMod.LOGGER.warn("Player {} tried to change the config without permission", playerName);
                sendConfig(server, player, McMod.getConfig());
                return;
            }
            LOGGER.info("Config received from player {}", playerName);
            McMod.updateConfig(payload.config());
            for (ServerPlayer other : server.getPlayerList().getPlayers()) {
                sendConfig(server, other, payload.config());
            }
        });
    }

    private static boolean canEdit(MinecraftServer server, ServerPlayer player) {
        return server.isSingleplayerOwner(player.nameAndId()) || player.permissions().hasPermission(Permissions.COMMANDS_GAMEMASTER);
    }

    private static void sendConfig(MinecraftServer server, ServerPlayer player, McModConfig config) {
        if (!ServerPlayNetworking.canSend(player, ConfigSyncPayload.TYPE)) {
            return;
        }
        ServerPlayNetworking.send(player, new ConfigSyncPayload(config, canEdit(server, player)));
    }

}
