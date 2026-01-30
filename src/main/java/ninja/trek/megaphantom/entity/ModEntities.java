package ninja.trek.megaphantom.entity;

import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import ninja.trek.megaphantom.MegaPhantom;

public class ModEntities {
    public static final ResourceKey<EntityType<?>> MEGA_PHANTOM_KEY = ResourceKey.create(
            Registries.ENTITY_TYPE,
            ResourceLocation.fromNamespaceAndPath(MegaPhantom.MOD_ID, "mega_phantom")
    );

    public static final EntityType<MegaPhantomEntity> MEGA_PHANTOM = Registry.register(
            BuiltInRegistries.ENTITY_TYPE,
            MEGA_PHANTOM_KEY,
            EntityType.Builder.of(MegaPhantomEntity::new, MobCategory.MONSTER)
                    .sized(0.9F * 3, 0.5F * 3)
                    .clientTrackingRange(8)
                    .build(MEGA_PHANTOM_KEY)
    );

    public static void register() {
        FabricDefaultAttributeRegistry.register(MEGA_PHANTOM, MegaPhantomEntity.createMegaPhantomAttributes());
        MegaPhantom.LOGGER.info("Registered MegaPhantom entities");
    }
}
