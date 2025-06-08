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
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;

public class WSkillPacket {
    public static void receive(MinecraftServer server, ServerPlayerEntity player, ServerPlayNetworkHandler handler, PacketByteBuf buf, PacketSender responseSender) {
            BlockPos targetPos = buf.readBlockPos();

            server.execute(() -> {
                ServerWorld world = (ServerWorld) player.getWorld();

                AreaEffectCloudEntity cloud = new AreaEffectCloudEntity(
                        world,
                        targetPos.getX() + 0.5,
                        targetPos.getY() + 1.5,
                        targetPos.getZ() + 0.5
                );

                cloud.setParticleType(ParticleTypes.FLAME);
                cloud.setRadius(LeagueModBrand.W_AREA_RADIUS);
                cloud.setDuration(10);
                cloud.setWaitTime(0);
                cloud.setRadiusOnUse(-0.5F);
                cloud.setWaitTime(0);
                cloud.setRadiusGrowth(-cloud.getRadius() / (float)cloud.getDuration());
                world.spawnEntity(cloud);

                Box area = new Box(
                        targetPos.getX() - LeagueModBrand.W_AREA_RADIUS, targetPos.getY() - 2, targetPos.getZ() - LeagueModBrand.W_AREA_RADIUS,
                        targetPos.getX() + LeagueModBrand.W_AREA_RADIUS, targetPos.getY() + 2, targetPos.getZ() + LeagueModBrand.W_AREA_RADIUS
                );

                for (LivingEntity nearbyEntity : world.getEntitiesByClass(
                        LivingEntity.class,
                        area,
                        p -> p != player
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
                    nearbyEntity.damage(nearbyEntity.getDamageSources().onFire(), LeagueModBrand.W_AREA_DAMAGE * multiplier);
                }


            });
        }

    }