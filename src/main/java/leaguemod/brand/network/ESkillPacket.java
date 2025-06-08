package leaguemod.brand.network;

import leaguemod.brand.effects.BlazeEffect;
import leaguemod.brand.LeagueModBrand;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.minecraft.entity.AreaEffectCloudEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.Box;

public class ESkillPacket {
    public static void receive(MinecraftServer server, ServerPlayerEntity player, ServerPlayNetworkHandler handler, PacketByteBuf buf, PacketSender responseSender) {
            int targetPos = buf.readInt();

            server.execute(() -> {
                ServerWorld world = (ServerWorld) player.getWorld();
                var entity = world.getEntityById(targetPos);
                if (entity == null || !(entity instanceof LivingEntity livingEntity))
                    return;

                float range = LeagueModBrand.E_BASE_RANGE;
                if(livingEntity.hasStatusEffect(LeagueModBrand.BLAZE_EFFECT))
                    range *= 2;
                BlazeEffect.applyEffect(livingEntity);

                Box area = new Box(
                        entity.getX() - range, entity.getY() - 2, entity.getZ() - range,
                        entity.getX() + range, entity.getY() + 2, entity.getZ() + range
                );

                for (LivingEntity nearbyEntity : world.getEntitiesByClass(
                        LivingEntity.class,
                        area,
                        p -> p != player && p!= entity
                ))
                {

                    AreaEffectCloudEntity cloudsmall = new AreaEffectCloudEntity(
                            world,
                            nearbyEntity.getX() ,
                            nearbyEntity.getY(),
                            nearbyEntity.getZ()
                    );

                    cloudsmall.setParticleType(ParticleTypes.SOUL_FIRE_FLAME);
                    cloudsmall.setRadius(1f);
                    cloudsmall.setDuration(15);
                    cloudsmall.setWaitTime(0);
                    cloudsmall.setRadiusOnUse(-0.5F);
                    cloudsmall.setWaitTime(0);
                    cloudsmall.setRadiusGrowth(-cloudsmall.getRadius() / (float)cloudsmall.getDuration());
                    world.spawnEntity(cloudsmall);
                    BlazeEffect.applyEffect(nearbyEntity);
                    float multiplier = 1;
                    if(nearbyEntity.hasStatusEffect(LeagueModBrand.BLAZE_EFFECT))
                        multiplier = 1.25f;
                    nearbyEntity.damage(nearbyEntity.getDamageSources().onFire(), LeagueModBrand.E_DAMAGE * multiplier);

                    //spawnParticleLineBetweenEntities(world,livingEntity,nearbyEntity,ParticleTypes.FLAME,10,2f);
                }
            });


    }

}