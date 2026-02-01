package ninja.trek.megaphantom.entity;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerBossEvent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.BossEvent;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Phantom;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import ninja.trek.megaphantom.config.MegaPhantomConfig;
import ninja.trek.megaphantom.item.ModItems;

import java.util.List;

public class MegaPhantomEntity extends Phantom {
    private static final int SPAWN_SOUND_COUNT = 10;
    private static final int SPAWN_SOUND_DURATION_TICKS = 40; // 2 seconds

    private final ServerBossEvent bossEvent = new ServerBossEvent(
            Component.literal("Mega Phantom").withStyle(ChatFormatting.DARK_PURPLE),
            BossEvent.BossBarColor.PURPLE,
            BossEvent.BossBarOverlay.PROGRESS
    );

    private int spawnTickCounter = 0;
    private int[] spawnSoundTicks;

    public MegaPhantomEntity(EntityType<? extends Phantom> entityType, Level level) {
        super(entityType, level);
        this.xpReward = 50;
    }

    /** Call after adding entity to world to schedule spawn sounds. */
    public void initSpawnSounds() {
        spawnSoundTicks = new int[SPAWN_SOUND_COUNT];
        for (int i = 0; i < SPAWN_SOUND_COUNT; i++) {
            spawnSoundTicks[i] = this.random.nextInt(SPAWN_SOUND_DURATION_TICKS);
        }
        java.util.Arrays.sort(spawnSoundTicks);
        spawnTickCounter = 0;
    }

    public static AttributeSupplier.Builder createMegaPhantomAttributes() {
        MegaPhantomConfig config = MegaPhantomConfig.get();
        return Phantom.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 20.0 * config.healthMultiplier)
                .add(Attributes.ATTACK_DAMAGE, 6.0 * config.damageMultiplier)
                .add(Attributes.SCALE, config.scale);
    }

    @Override
    protected void customServerAiStep(ServerLevel level) {
        super.customServerAiStep(level);
        bossEvent.setProgress(this.getHealth() / this.getMaxHealth());

        // Play spawn swoop sounds
        if (spawnSoundTicks != null) {
            for (int tick : spawnSoundTicks) {
                if (tick == spawnTickCounter) {
                    level.playSound(null, this.getX(), this.getY(), this.getZ(),
                            SoundEvents.PHANTOM_SWOOP, SoundSource.HOSTILE,
                            1.0F, 0.8F + this.random.nextFloat() * 0.4F);
                }
            }
            spawnTickCounter++;
            if (spawnTickCounter >= SPAWN_SOUND_DURATION_TICKS) {
                spawnSoundTicks = null; // Done, stop checking
            }
        }
    }

    @Override
    public void startSeenByPlayer(ServerPlayer player) {
        super.startSeenByPlayer(player);
        bossEvent.addPlayer(player);
    }

    @Override
    public void stopSeenByPlayer(ServerPlayer player) {
        super.stopSeenByPlayer(player);
        bossEvent.removePlayer(player);
    }

    @Override
    public boolean hurtServer(ServerLevel level, DamageSource source, float amount) {
        MegaPhantomConfig config = MegaPhantomConfig.get();
        double range = config.losRange;
        double reductionEach = config.reductionPerPhantom;

        // Count nearby non-mega phantoms with line of sight
        AABB searchBox = this.getBoundingBox().inflate(range);
        List<Phantom> nearbyPhantoms = level.getEntitiesOfClass(Phantom.class, searchBox, phantom ->
                !(phantom instanceof MegaPhantomEntity) && phantom.isAlive()
        );

        int shieldCount = 0;
        for (Phantom phantom : nearbyPhantoms) {
            if (hasLineOfSight(level, phantom, this)) {
                shieldCount++;
            }
        }

        float reduction = (float) Math.min(shieldCount * reductionEach, 0.9);
        float finalAmount = amount * (1.0f - reduction);
        // Minimum 10% damage always gets through
        finalAmount = Math.max(finalAmount, amount * 0.1f);

        // Send action bar message to attacker
        if (source.getEntity() instanceof ServerPlayer attacker && shieldCount > 0) {
            int pct = (int) (reduction * 100);
            attacker.displayClientMessage(
                    Component.literal("Phantom Shield: " + shieldCount + " phantoms, " + pct + "% damage reduced")
                            .withStyle(ChatFormatting.LIGHT_PURPLE),
                    true
            );
        }

        return super.hurtServer(level, source, finalAmount);
    }

    private static boolean hasLineOfSight(ServerLevel level, Phantom from, MegaPhantomEntity to) {
        Vec3 start = from.getEyePosition();
        Vec3 end = to.getEyePosition();
        ClipContext ctx = new ClipContext(start, end, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, from);
        HitResult result = level.clip(ctx);
        return result.getType() == HitResult.Type.MISS
                || result.getLocation().distanceToSqr(end) < 1.0;
    }

    @Override
    protected void dropCustomDeathLoot(ServerLevel level, DamageSource source, boolean playerKill) {
        super.dropCustomDeathLoot(level, source, playerKill);
        ItemStack elytra = ModItems.createBrokenElytra(level.registryAccess());
        this.spawnAtLocation(level, elytra);
    }

    @Override
    public boolean removeWhenFarAway(double distanceSq) {
        return false; // Boss should not despawn
    }
}
