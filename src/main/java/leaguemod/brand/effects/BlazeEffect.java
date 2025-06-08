package leaguemod.brand.effects;

import leaguemod.brand.LeagueModBrand;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectCategory;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import java.util.List;

public class BlazeEffect  extends StatusEffect {
    public BlazeEffect() {
        super(StatusEffectCategory.HARMFUL,0xe9b8b3 );
    }

    @Override
    public boolean canApplyUpdateEffect(int duration, int amplifier) {
        // In our case, we just make it return true so that it applies the effect every tick
        return true;
    }

    @Override
    public void applyUpdateEffect(LivingEntity entity, int amplifier) {
        entity.damage(entity.getDamageSources().inFire(), LeagueModBrand.BLAZE_TICK_DAMAGE * amplifier);

        if (amplifier == 2) {
            entity.removeStatusEffect(LeagueModBrand.BLAZE_EFFECT);

            World world = entity.getWorld();
            Vec3d pos = entity.getPos();

            List<LivingEntity> entities = world.getEntitiesByClass(
                    LivingEntity.class,
                    new Box(
                            pos.x - 3, pos.y - 3, pos.z - 3,
                            pos.x + 3, pos.y + 3, pos.z + 3
                    ),
                    e -> e instanceof LivingEntity
            );

            for (LivingEntity target : entities) {
                target.damage(world.getDamageSources().explosion(null, null), LeagueModBrand.BLAZE_BLOW_DAMAGE);
            }

            if (world instanceof ServerWorld serverWorld) {
                serverWorld.spawnParticles(
                        ParticleTypes.EXPLOSION,
                        pos.x, pos.y + 1, pos.z,
                        20,
                        1, 1, 1,
                        0.5
                );
                serverWorld.playSound(
                        null,
                        pos.x, pos.y, pos.z,
                        SoundEvents.ENTITY_GENERIC_EXPLODE,
                        SoundCategory.HOSTILE,
                        1f, 1f
                );
            }
        }
    }

    public static void applyEffect(LivingEntity livingEntity){
        var effect = livingEntity.getStatusEffect(LeagueModBrand.BLAZE_EFFECT);
        if (effect == null) {
            livingEntity.addStatusEffect(new StatusEffectInstance(LeagueModBrand.BLAZE_EFFECT, 90));
            return;
        }
        int level = effect.getAmplifier();
        effect = new StatusEffectInstance(LeagueModBrand.BLAZE_EFFECT, 90, clamp(level+1,0,2));
        livingEntity.addStatusEffect(effect);
    }
    public static int clamp(int val, int min, int max) {
        return Math.max(min, Math.min(max, val));
    }
}
