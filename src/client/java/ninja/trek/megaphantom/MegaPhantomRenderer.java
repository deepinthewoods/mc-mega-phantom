package ninja.trek.megaphantom;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.state.CameraRenderState;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.PhantomRenderer;
import net.minecraft.client.renderer.entity.state.PhantomRenderState;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.monster.Phantom;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import ninja.trek.megaphantom.config.MegaPhantomConfig;
import ninja.trek.megaphantom.entity.MegaPhantomEntity;

import java.util.List;

public class MegaPhantomRenderer extends PhantomRenderer {

    private static final Identifier BEAM_TEXTURE =
            Identifier.fromNamespaceAndPath("minecraft", "textures/entity/end_crystal/end_crystal_beam.png");

    private static final RenderType BEAM_RENDER_TYPE = RenderTypes.entityTranslucent(BEAM_TEXTURE);

    // Thinner than dragon crystal beams (0.75) or guardian beams (0.2)
    private static final float BEAM_RADIUS = 0.08f;

    public MegaPhantomRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public MegaPhantomRenderState createRenderState() {
        return new MegaPhantomRenderState();
    }

    @Override
    public void extractRenderState(Phantom phantom, PhantomRenderState state, float partialTick) {
        super.extractRenderState(phantom, state, partialTick);

        if (!(state instanceof MegaPhantomRenderState megaState)) return;
        if (!(phantom instanceof MegaPhantomEntity mega)) return;

        megaState.beamVectors.clear();
        megaState.bodyCenter = mega.getBbHeight() / 2.0f;

        Vec3 megaPos = mega.getPosition(partialTick);
        Vec3 megaCenter = megaPos.add(0, megaState.bodyCenter, 0);

        double range = MegaPhantomConfig.get().losRange;
        AABB searchBox = mega.getBoundingBox().inflate(range);
        List<Phantom> nearbyPhantoms = mega.level().getEntitiesOfClass(
                Phantom.class, searchBox,
                p -> !(p instanceof MegaPhantomEntity) && p.isAlive()
        );

        for (Phantom target : nearbyPhantoms) {
            // Client-side line-of-sight check matching server logic
            Vec3 startEye = mega.getEyePosition(partialTick);
            Vec3 endEye = target.getEyePosition(partialTick);
            ClipContext ctx = new ClipContext(
                    startEye, endEye,
                    ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, mega);
            HitResult result = mega.level().clip(ctx);
            if (result.getType() != HitResult.Type.MISS
                    && result.getLocation().distanceToSqr(endEye) >= 1.0) {
                continue;
            }

            Vec3 targetPos = target.getPosition(partialTick);
            Vec3 targetCenter = targetPos.add(0, target.getBbHeight() / 2.0, 0);
            megaState.beamVectors.add(targetCenter.subtract(megaCenter));
        }
    }

    @Override
    public void submit(PhantomRenderState state, PoseStack poseStack,
                       SubmitNodeCollector collector, CameraRenderState cameraState) {
        super.submit(state, poseStack, collector, cameraState);

        if (!(state instanceof MegaPhantomRenderState megaState)) return;
        if (megaState.beamVectors.isEmpty()) return;

        poseStack.pushPose();
        poseStack.translate(0, megaState.bodyCenter, 0);

        for (Vec3 beam : megaState.beamVectors) {
            renderBeam(poseStack, collector, beam, megaState.ageInTicks);
        }

        poseStack.popPose();
    }

    private static void renderBeam(PoseStack poseStack, SubmitNodeCollector collector,
                                   Vec3 beamVec, float ageInTicks) {
        float distance = (float) beamVec.length();
        if (distance < 0.1f) return;

        Vec3 dir = beamVec.normalize();

        // Rotation to align +Y axis with beam direction (same approach as GuardianRenderer)
        float yAngle = (float) Math.acos(dir.y);
        float xAngle = (float) (Math.PI / 2) - (float) Math.atan2(dir.z, dir.x);

        poseStack.pushPose();
        poseStack.mulPose(Axis.YP.rotationDegrees(xAngle * (180.0f / (float) Math.PI)));
        poseStack.mulPose(Axis.XP.rotationDegrees(yAngle * (180.0f / (float) Math.PI)));

        // Slow spin around beam axis
        float spin = ageInTicks * 0.04f * -1.5f;

        // Two perpendicular pairs of quad corners, rotating
        float r = BEAM_RADIUS;
        float c1x = Mth.cos(spin) * r;
        float c1z = Mth.sin(spin) * r;
        float c2x = Mth.cos(spin + (float) (Math.PI / 2)) * r;
        float c2z = Mth.sin(spin + (float) (Math.PI / 2)) * r;
        float c3x = Mth.cos(spin + (float) Math.PI) * r;
        float c3z = Mth.sin(spin + (float) Math.PI) * r;
        float c4x = Mth.cos(spin + (float) (Math.PI * 3 / 2)) * r;
        float c4z = Mth.sin(spin + (float) (Math.PI * 3 / 2)) * r;

        // Scrolling UV for animation
        float uvStart = -ageInTicks * 0.012f;
        float uvEnd = uvStart + distance * 2.0f;

        collector.submitCustomGeometry(poseStack, BEAM_RENDER_TYPE, (pose, consumer) -> {
            // Quad 1 (one axis of the cross)
            beamVertex(consumer, pose, c1x, distance, c1z, 0.5f, uvEnd);
            beamVertex(consumer, pose, c1x, 0, c1z, 0.5f, uvStart);
            beamVertex(consumer, pose, c3x, 0, c3z, 0, uvStart);
            beamVertex(consumer, pose, c3x, distance, c3z, 0, uvEnd);

            // Quad 2 (perpendicular axis of the cross)
            beamVertex(consumer, pose, c2x, distance, c2z, 0.5f, uvEnd);
            beamVertex(consumer, pose, c2x, 0, c2z, 0.5f, uvStart);
            beamVertex(consumer, pose, c4x, 0, c4z, 0, uvStart);
            beamVertex(consumer, pose, c4x, distance, c4z, 0, uvEnd);
        });

        poseStack.popPose();
    }

    private static void beamVertex(VertexConsumer consumer, PoseStack.Pose pose,
                                   float x, float y, float z, float u, float v) {
        consumer.addVertex(pose, x, y, z)
                .setColor(180, 100, 240, 80)
                .setUv(u, v)
                .setOverlay(OverlayTexture.NO_OVERLAY)
                .setLight(15728880)
                .setNormal(pose, 0, 1, 0);
    }
}
