package ninja.trek.megaphantom.mixin;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.monster.Phantom;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.Vec3;
import ninja.trek.megaphantom.MegaPhantom;
import ninja.trek.megaphantom.config.MegaPhantomConfig;
import ninja.trek.megaphantom.data.ModAttachments;
import ninja.trek.megaphantom.entity.MegaPhantomEntity;
import ninja.trek.megaphantom.spawn.MegaPhantomSpawner;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ServerPlayer.class)
public class PlayerHurtByPhantomMixin {

    @Inject(method = "hurtServer", at = @At("HEAD"), cancellable = true)
    private void megaphantom$onHurt(ServerLevel level, DamageSource source, float amount, CallbackInfoReturnable<Boolean> cir) {
        if (!(source.getEntity() instanceof Phantom phantom)) {
            return;
        }

        // Mega phantom damage passes through normally
        if (phantom instanceof MegaPhantomEntity) {
            return;
        }

        // Cancel normal phantom damage
        cir.setReturnValue(false);

        ServerPlayer player = (ServerPlayer) (Object) this;

        // Check if there's a solid block 1-2 blocks above player eye position (indoor check)
        Vec3 eyePos = player.getEyePosition();
        BlockPos above1 = BlockPos.containing(eyePos.x, eyePos.y + 1, eyePos.z);
        BlockPos above2 = BlockPos.containing(eyePos.x, eyePos.y + 2, eyePos.z);
        BlockState state1 = level.getBlockState(above1);
        BlockState state2 = level.getBlockState(above2);

        if (state1.isSuffocating(level, above1) || state2.isSuffocating(level, above2)) {
            // Player is indoors, don't count the swoop
            return;
        }

        // Increment swoop counter
        int count = player.getAttachedOrCreate(ModAttachments.SWOOP_COUNT);
        count++;
        player.setAttached(ModAttachments.SWOOP_COUNT, count);

        MegaPhantom.LOGGER.debug("Player {} swoop count: {}", player.getName().getString(), count);

        // Check threshold
        if (count >= MegaPhantomConfig.get().swoopThreshold) {
            player.setAttached(ModAttachments.SWOOP_COUNT, 0);
            MegaPhantomSpawner.spawnMegaPhantom(player);
        }
    }
}
