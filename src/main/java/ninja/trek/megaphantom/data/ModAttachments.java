package ninja.trek.megaphantom.data;

import com.mojang.serialization.Codec;
import net.fabricmc.fabric.api.attachment.v1.AttachmentRegistry;
import net.fabricmc.fabric.api.attachment.v1.AttachmentType;
import net.minecraft.resources.ResourceLocation;
import ninja.trek.megaphantom.MegaPhantom;

public class ModAttachments {
    public static final AttachmentType<Integer> SWOOP_COUNT = AttachmentRegistry.create(
            ResourceLocation.fromNamespaceAndPath(MegaPhantom.MOD_ID, "swoop_count"),
            builder -> builder
                    .persistent(Codec.INT)
                    .initializer(() -> 0)
                    .copyOnDeath()
    );

    public static void register() {
        MegaPhantom.LOGGER.info("Registered MegaPhantom attachments");
    }
}
