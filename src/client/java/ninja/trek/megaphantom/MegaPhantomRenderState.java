package ninja.trek.megaphantom;

import net.minecraft.client.renderer.entity.state.PhantomRenderState;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.List;

public class MegaPhantomRenderState extends PhantomRenderState {
    public final List<Vec3> beamVectors = new ArrayList<>();
    public float bodyCenter;
}
