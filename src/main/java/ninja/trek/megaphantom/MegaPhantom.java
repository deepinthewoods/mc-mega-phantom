package ninja.trek.megaphantom;

import net.fabricmc.api.ModInitializer;
import ninja.trek.megaphantom.config.MegaPhantomConfig;
import ninja.trek.megaphantom.data.ModAttachments;
import ninja.trek.megaphantom.entity.ModEntities;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MegaPhantom implements ModInitializer {
	public static final String MOD_ID = "megaphantom";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitialize() {
		MegaPhantomConfig.load();
		ModAttachments.register();
		ModEntities.register();
		LOGGER.info("MegaPhantom initialized");
	}
}