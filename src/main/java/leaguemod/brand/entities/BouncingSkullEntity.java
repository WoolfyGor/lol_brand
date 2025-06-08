package leaguemod.brand.entities;

import leaguemod.brand.effects.BlazeEffect;
import leaguemod.brand.LeagueModBrand;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.projectile.WitherSkullEntity;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import java.util.List;
import java.util.stream.Collectors;

public class BouncingSkullEntity extends WitherSkullEntity {
    private LivingEntity owner;
    private LivingEntity target;
    private int bouncesLeft = 5;

    private int stuckTicks = 0;
    private final int MAX_STUCK_TICKS = 5; // Макс. время "застревания" перед поиском новой цели
    private int noTargetTicks = 0;
    private final int MAX_NO_TARGET_TICKS = 5; // Макс время без цели перед уничтожением
    public BouncingSkullEntity(EntityType<? extends WitherSkullEntity> entityType, World world) {
        super(entityType, world);
    }

    public BouncingSkullEntity(World world, LivingEntity owner, LivingEntity target) {
        super(world, owner, 0, 0, 0);
        this.owner = owner;
        this.target = target;
        this.setPosition(owner.getX(), owner.getEyeY(), owner.getZ());
        this.bouncesLeft = LeagueModBrand.R_BOUNCES;
    }


    @Override
    public void tick() {
        super.tick();
        if (target == null || !target.isAlive()) {
            noTargetTicks++;
            if (noTargetTicks >= MAX_NO_TARGET_TICKS) {
                this.discard();
                return;
            }
            findNewTarget(null);
            return;
        }
        if (this.target != null && this.target.isAlive() && this.bouncesLeft > 0) {
            Vec3d targetPos = this.target.getPos().add(0, this.target.getHeight() / 2, 0);
            Vec3d currentPos = this.getPos();
            Vec3d direction = targetPos.subtract(currentPos).normalize();

            // Проверяем, не застряли ли мы
            if (this.squaredDistanceTo(targetPos) < 1.0) {
                stuckTicks++;
                if (stuckTicks >= MAX_STUCK_TICKS) {
                    findNewTarget(this.target);
                    stuckTicks = 0;
                    return;
                }
            } else {
                stuckTicks = 0;
            }

            // Устанавливаем скорость с учетом коэффициента
            this.setVelocity(direction.multiply(LeagueModBrand.R_TRAVEL_SPEED));
            this.velocityModified = true;
        }
    }

    @Override
    protected void onCollision(HitResult hitResult) {
        if (this.isRemoved()) return;

        if (hitResult.getType() == HitResult.Type.ENTITY) {
            EntityHitResult entityHitResult = (EntityHitResult) hitResult;

            if (entityHitResult.getEntity() instanceof LivingEntity) {
                LivingEntity hitEntity = (LivingEntity) entityHitResult.getEntity();

                if (hitEntity == this.owner) {
                    // Отскок от хозяина
                    findNewTarget(hitEntity);
                    return;
                } else if(hitEntity == target) {
                    // Наносим урон и эффект
                    hitEntity.damage(hitEntity.getDamageSources().magic(), LeagueModBrand.R_DAMAGE);
                    BlazeEffect.applyEffect(hitEntity);

                    // Ищем новую цель
                    findNewTarget(hitEntity);
                    return;
                }
                else if (this.bouncesLeft <= 0) {
                    this.discard();
                }
                else
                    return;
            }
        }

        this.discard();
    }

    private void findNewTarget(LivingEntity currentHit) {
        this.bouncesLeft--;
        if (this.bouncesLeft <= 0) {
            this.discard();
            return;
        }

        // Ищем цели в радиусе
        Box box = new Box(
                currentHit != null ? currentHit.getX() - LeagueModBrand.R_BASE_RANGE : this.getX() - LeagueModBrand.R_BASE_RANGE,
                currentHit != null ? currentHit.getY() - LeagueModBrand.R_BASE_RANGE : this.getY() - LeagueModBrand.R_BASE_RANGE,
                currentHit != null ? currentHit.getZ() - LeagueModBrand.R_BASE_RANGE : this.getZ() - LeagueModBrand.R_BASE_RANGE,
                currentHit != null ? currentHit.getX() + LeagueModBrand.R_BASE_RANGE : this.getX() + LeagueModBrand.R_BASE_RANGE,
                currentHit != null ? currentHit.getY() + LeagueModBrand.R_BASE_RANGE : this.getY() + LeagueModBrand.R_BASE_RANGE,
                currentHit != null ? currentHit.getZ() + LeagueModBrand.R_BASE_RANGE : this.getZ() + LeagueModBrand.R_BASE_RANGE
        );

        List<LivingEntity> potentialTargets = this.getWorld().getEntitiesByClass(
                LivingEntity.class,
                box,
                e -> e != currentHit && e.isAlive() && (e != this.owner || this.bouncesLeft > 0)
        );

        // Сначала ищем цели с эффектом BLAZE_EFFECT
        List<LivingEntity> prioritizedTargets = potentialTargets.stream()
                .filter(e -> e.hasStatusEffect(LeagueModBrand.BLAZE_EFFECT))
                .collect(Collectors.toList());

        if (!prioritizedTargets.isEmpty()) {
            this.target = prioritizedTargets.get(this.random.nextInt(prioritizedTargets.size()));
        } else if (!potentialTargets.isEmpty()) {
            this.target = potentialTargets.get(this.random.nextInt(potentialTargets.size()));
        } else if (this.owner != null && this.owner.isAlive()) {
            this.target = this.owner;
        } else {
            this.discard();
        }

        // Сбрасываем счетчик застревания при смене цели
        this.stuckTicks = 0;
    }
}