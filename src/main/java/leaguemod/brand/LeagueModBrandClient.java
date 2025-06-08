package leaguemod.brand;

import io.netty.buffer.Unpooled;
import leaguemod.brand.network.ModPackets;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.entity.AreaEffectCloudEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.decoration.ItemFrameEntity;
import net.minecraft.entity.projectile.ProjectileUtil;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.text.Text;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.RaycastContext;
import net.minecraft.world.World;
import org.lwjgl.glfw.GLFW;

import static net.minecraft.world.BlockView.raycast;

public class LeagueModBrandClient implements ClientModInitializer {

    private static KeyBinding qKeyBinding;
    private static KeyBinding wKeyBinding;
    private static KeyBinding eKeyBinding;
    private static KeyBinding rKeyBinding;




    @Override
    public void onInitializeClient() {
        registerSkills();
        registerKeys();
        ModPackets.registerClient();
    }

    private  void registerSkills(){
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (client.player == null) return;
            while (qKeyBinding.wasPressed()) {
                throwSearSpell(client);
            }
            while (wKeyBinding.wasPressed()) {
                spawnFlameCloud(client);
            }
            while (eKeyBinding.wasPressed()) {
                spreadFlame(client);
            }
            while (rKeyBinding.wasPressed()) {
                shootBounce(client);
            }
        });
    }

    private void registerKeys(){
        qKeyBinding = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.leaguemod_brand.h",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_H,
                "category.leaguemod_brand.keys"
        ));
        wKeyBinding = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.leaguemod_brand.j",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_J,
                "category.leaguemod_brand.keys"
        ));
        eKeyBinding = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.leaguemod_brand.k",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_K,
                "category.leaguemod_brand.keys"
        ));
        rKeyBinding = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.leaguemod_brand.l",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_L,
                "category.leaguemod_brand.keys"
        ));
    }

    private void throwSearSpell(MinecraftClient client) {
        if (client.world == null || client.player == null) return;
        if (client.getNetworkHandler() != null) {
            ClientPlayNetworking.send(ModPackets.THROW_Q_SKILL, new PacketByteBuf(Unpooled.buffer()));
    }
    }

    private void spawnFlameCloud(MinecraftClient client) {
        if (client.world == null || client.player == null) return;

        BlockHitResult hitResult = client.world.raycast(new RaycastContext(
                client.player.getEyePos(),
                client.player.getEyePos().add(client.player.getRotationVector().multiply(LeagueModBrand.W_SHOOT_DISTANCE)),
                RaycastContext.ShapeType.OUTLINE,
                RaycastContext.FluidHandling.NONE,
                client.player
        ));

        if (hitResult.getType() != HitResult.Type.BLOCK) {
            client.player.playSound(SoundEvents.ENTITY_VILLAGER_NO, 1f, 1f);
            return;
        }
        BlockPos targetPos = hitResult.getBlockPos();

        PacketByteBuf buf = PacketByteBufs.create();
        buf.writeBlockPos(targetPos);
        ClientPlayNetworking.send(ModPackets.THROW_W_SKILL, buf);
    }

    private void spreadFlame(MinecraftClient client){
        if (client.world == null || client.player == null) return;

        HitResult target = raycastInDirection(client, LeagueModBrand.E_SHOOT_DISTANCE, client.player.getRotationVector());
        if(!(target instanceof EntityHitResult entity)) {
            client.player.playSound(SoundEvents.ENTITY_VILLAGER_NO, 1f, 1f);
            return;
        }

        PacketByteBuf buf = PacketByteBufs.create();
        var ent = entity.getEntity();
        if(!(ent instanceof LivingEntity livingEntity))
            return;

        buf.writeInt(ent.getId());
        ClientPlayNetworking.send(ModPackets.THROW_E_SKILL, buf);


        var player = client.player;
        float range = LeagueModBrand.E_BASE_RANGE;

        if(livingEntity.hasStatusEffect(LeagueModBrand.BLAZE_EFFECT))
            range *= 2;

        Box area = new Box(
                player.getX() - range, player.getY() - 2, player.getZ() - range,
                player.getX() + range, player.getY() + 2, player.getZ() + range
        );

        for (LivingEntity nearbyEntity : client.world.getEntitiesByClass(
                LivingEntity.class,
                area,
                p -> p != client.player
        ))
        {


            spawnParticleLineBetweenEntities(client.world,livingEntity,nearbyEntity,ParticleTypes.SMOKE,10,2f);
        }
    }


    private void shootBounce(MinecraftClient client){
        if (client.world == null || client.player == null) return;

        HitResult target = raycastInDirection(client, LeagueModBrand.E_SHOOT_DISTANCE, client.player.getRotationVector());
        if(!(target instanceof EntityHitResult entity)) {
            client.player.playSound(SoundEvents.ENTITY_VILLAGER_NO, 1f, 1f);
            return;
        }
        if(!(((EntityHitResult) target).getEntity() instanceof LivingEntity livingEntity))
            return;
        PacketByteBuf buf = PacketByteBufs.create();
        buf.writeInt(livingEntity.getId());

        ClientPlayNetworking.send(ModPackets.THROW_R_SKILL, buf);

    }
    public static void spawnParticleLineBetweenEntities(World world, Entity entity1, Entity entity2, ParticleEffect particleType, int particleCount, double particleSpeed) {
        if (world == null || entity1 == null || entity2 == null) return;

        // Получаем позиции обеих сущностей
        Vec3d startPos = entity1.getPos();
        Vec3d endPos = entity2.getPos();

        // Вектор направления от entity1 к entity2
        Vec3d direction = endPos.subtract(startPos);

        // Длина между сущностями
        double distance = direction.length();

        // Нормализуем вектор направления
        Vec3d step = direction.normalize();

        // Шаг между частицами
        double stepSize = distance / particleCount;

        // Спавним частицы вдоль линии
        for (int i = 0; i <= particleCount; i++) {
            // Вычисляем позицию для текущей частицы
            Vec3d particlePos = startPos.add(step.multiply(i * stepSize));

            // Добавляем небольшой случайный разброс для более естественного вида
            double offsetX = (world.random.nextDouble() - 0.5) * 0.2;
            double offsetY = (world.random.nextDouble() - 0.5) * 0.2;
            double offsetZ = (world.random.nextDouble() - 0.5) * 0.2;

            // Спавним частицу (только на клиенте)
            if (world.isClient) {
                world.addParticle(
                        particleType,
                        particlePos.x + offsetX,
                        particlePos.y + entity1.getHeight() / 2 + offsetY, // Центрируем по высоте
                        particlePos.z + offsetZ,
                        0,
                        0,
                        0
                );
            }}
    }

    private static HitResult raycastInDirection(MinecraftClient client, float tickDelta, Vec3d direction) {
        Entity entity = client.getCameraEntity();
        if (entity == null || client.world == null) {
            return null;
        }

        double reachDistance = client.interactionManager.getReachDistance();//Change this to extend the reach
        HitResult target = raycast(entity, reachDistance, tickDelta, false, direction);
        boolean tooFar = false;
        double extendedReach = reachDistance;
        if (client.interactionManager.hasExtendedReach()) {
            extendedReach = 6.0D;//Change this to extend the reach
            reachDistance = extendedReach;
        } else {
            if (reachDistance > 3.0D) {
                tooFar = true;
            }
        }

        Vec3d cameraPos = entity.getCameraPosVec(tickDelta);

        extendedReach = extendedReach * extendedReach;
        if (target != null) {
            extendedReach = target.getPos().squaredDistanceTo(cameraPos);
        }

        Vec3d vec3d3 = cameraPos.add(direction.multiply(reachDistance));
        Box box = entity
                .getBoundingBox()
                .stretch(entity.getRotationVec(1.0F).multiply(reachDistance))
                .expand(1.0D, 1.0D, 1.0D);
        EntityHitResult entityHitResult = ProjectileUtil.raycast(
                entity,
                cameraPos,
                vec3d3,
                box,
                (entityx) -> !entityx.isSpectator() ,
                extendedReach
        );

        if (entityHitResult == null) {
            return target;
        }

        Entity entity2 = entityHitResult.getEntity();
        Vec3d vec3d4 = entityHitResult.getPos();
        double g = cameraPos.squaredDistanceTo(vec3d4);
        if (tooFar && g > 9.0D) {
            return null;
        } else if (g < extendedReach || target == null) {
            target = entityHitResult;
            if (entity2 instanceof LivingEntity || entity2 instanceof ItemFrameEntity) {
                client.targetedEntity = entity2;
            }
        }

        return target;
    }

    private static HitResult raycast(
            Entity entity,
            double maxDistance,
            float tickDelta,
            boolean includeFluids,
            Vec3d direction
    ) {
        Vec3d end = entity.getCameraPosVec(tickDelta).add(direction.multiply(maxDistance));
        return entity.getWorld().raycast(new RaycastContext(
                entity.getCameraPosVec(tickDelta),
                end,
                RaycastContext.ShapeType.OUTLINE,
                includeFluids ? RaycastContext.FluidHandling.ANY : RaycastContext.FluidHandling.NONE,
                entity
        ));
    }
}




