package ninja.trek.megaphantom;

import net.fabricmc.api.ClientModInitializer;
import net.minecraft.client.renderer.entity.EntityRenderers;
import ninja.trek.megaphantom.entity.ModEntities;

public class MegaPhantomClient implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
		EntityRenderers.register(ModEntities.MEGA_PHANTOM, MegaPhantomRenderer::new);
	}
}