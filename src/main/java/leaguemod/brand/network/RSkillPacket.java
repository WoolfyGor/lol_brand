package leaguemod.brand.network;

import leaguemod.brand.entities.BouncingSkullEntity;
import leaguemod.brand.entities.SearEntity;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.minecraft.entity.LivingEntity;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.Vec3d;

public class RSkillPacket {
    public static void receive(MinecraftServer server, ServerPlayerEntity player, ServerPlayNetworkHandler handler, PacketByteBuf buf, PacketSender responseSender) {
        // Выполняем на серверном потоке
        int id = buf.readInt();
        server.execute(() -> {

            LivingEntity target = (LivingEntity) player.getWorld().getEntityById(id);
            BouncingSkullEntity skull = new BouncingSkullEntity(player.getWorld(), player, target);
            player.getWorld().spawnEntity(skull);
        });
    }
}
