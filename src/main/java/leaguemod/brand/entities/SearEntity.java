package leaguemod.brand.entities;

import leaguemod.brand.effects.BlazeEffect;
import leaguemod.brand.LeagueModBrand;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.projectile.thrown.SnowballEntity;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.world.World;

public class SearEntity extends SnowballEntity {
    private  int liveTicks = 0;
    public SearEntity(EntityType<? extends SnowballEntity> entityType, World world) {
        super(entityType, world);
    }

    public SearEntity(World world, LivingEntity owner) {
        super(world, owner);
    }

    public SearEntity(World world, double x, double y, double z) {
        super(world, x, y, z);
        this.setNoGravity(true);

    }

    @Override
    public void tick() {
        super.tick();
        liveTicks++;
        if(liveTicks > LeagueModBrand.Q_LIFE_TIME)
            this.discard();
    }

    @Override
    protected void onEntityHit(EntityHitResult entityHitResult) {
        super.onEntityHit(entityHitResult);
        Entity entity = entityHitResult.getEntity();
        entity.damage(this.getDamageSources().thrown(this, this.getOwner()), LeagueModBrand.Q_DAMAGE);
        if (entity instanceof LivingEntity livingEntity && !(entity instanceof BouncingSkullEntity)) {
            if(livingEntity.hasStatusEffect(LeagueModBrand.BLAZE_EFFECT))
                livingEntity.addStatusEffect(new StatusEffectInstance(StatusEffects.SLOWNESS,(Integer)LeagueModBrand.Q_STUN,10000));
            BlazeEffect.applyEffect(livingEntity);
        }
    }


}
