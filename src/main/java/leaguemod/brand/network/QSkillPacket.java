package leaguemod.brand.network;

import leaguemod.brand.entities.SearEntity;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.minecraft.entity.projectile.thrown.SnowballEntity;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.Vec3d;

public class QSkillPacket {
    public static void receive(MinecraftServer server, ServerPlayerEntity player, ServerPlayNetworkHandler handler, PacketByteBuf buf, PacketSender responseSender) {
        // Выполняем на серверном потоке
        server.execute(() -> {
            Vec3d lookVec = player.getRotationVec(1.0F);
            Vec3d eyePos = player.getEyePos();

            SearEntity snowball = new SearEntity(
                    player.getWorld(),
                    eyePos.x,
                    eyePos.y,
                    eyePos.z
            );

            snowball.setVelocity(lookVec.multiply(1.5));
            player.getWorld().spawnEntity(snowball);
        });
    }
}
