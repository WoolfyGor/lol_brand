package leaguemod.brand.entities;

import leaguemod.brand.entities.BouncingSkullEntity;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricEntityTypeBuilder;
import net.minecraft.entity.EntityDimensions;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

public class ModEntities {
    public static final EntityType<BouncingSkullEntity> BOUNCING_SKULL = Registry.register(
            Registries.ENTITY_TYPE,
            new Identifier("league-mod-brand", "bouncing_skull"),
            FabricEntityTypeBuilder.<BouncingSkullEntity>create(SpawnGroup.MISC, BouncingSkullEntity::new)
                    .dimensions(EntityDimensions.fixed(0.5F, 0.5F))
                    .trackRangeBlocks(4).trackedUpdateRate(10)
                    .build()
    );

    public static void initialize() {
        // Инициализация сущностей
    }
}