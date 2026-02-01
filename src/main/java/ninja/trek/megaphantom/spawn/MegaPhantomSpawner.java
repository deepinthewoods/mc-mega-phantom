package ninja.trek.megaphantom.spawn;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EntitySpawnReason;
import ninja.trek.megaphantom.MegaPhantom;
import ninja.trek.megaphantom.entity.MegaPhantomEntity;
import ninja.trek.megaphantom.entity.ModEntities;

public class MegaPhantomSpawner {
    public static void spawnMegaPhantom(ServerPlayer player) {
        ServerLevel level = player.level();
        MegaPhantomEntity megaPhantom = ModEntities.MEGA_PHANTOM.create(level, EntitySpawnReason.EVENT);
        if (megaPhantom == null) {
            MegaPhantom.LOGGER.error("Failed to create Mega Phantom entity");
            return;
        }

        double x = player.getX() + (player.getRandom().nextDouble() - 0.5) * 20;
        double z = player.getZ() + (player.getRandom().nextDouble() - 0.5) * 20;
        double y = player.getY() + 20 + player.getRandom().nextDouble() * 10;

        megaPhantom.snapTo(x, y, z, player.getRandom().nextFloat() * 360.0F, 0.0F);
        megaPhantom.setTarget(player);
        megaPhantom.setPersistenceRequired();
        megaPhantom.initSpawnSounds();

        level.addFreshEntity(megaPhantom);

        player.sendSystemMessage(
                Component.literal("A Mega Phantom has appeared!")
                        .withStyle(style -> style
                                .withColor(ChatFormatting.DARK_PURPLE)
                                .withBold(true))
        );

        MegaPhantom.LOGGER.info("Spawned Mega Phantom for player {}", player.getName().getString());
    }
}
