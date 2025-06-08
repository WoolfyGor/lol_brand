package leaguemod.brand.network;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.util.Identifier;
public class ModPackets {
    public static final Identifier THROW_Q_SKILL = new Identifier("leaguemod", "throw_sear");
    public static final Identifier THROW_W_SKILL = new Identifier("leaguemod", "throw_pillar");
    public static final Identifier THROW_E_SKILL = new Identifier("leaguemod", "throw_flame");
    public static final Identifier THROW_R_SKILL = new Identifier("leaguemod", "throw_skull");
    public static void register() {
        ServerPlayNetworking.registerGlobalReceiver(THROW_Q_SKILL, QSkillPacket::receive);
        ServerPlayNetworking.registerGlobalReceiver(THROW_W_SKILL, WSkillPacket::receive);
        ServerPlayNetworking.registerGlobalReceiver(THROW_E_SKILL, ESkillPacket::receive);
        ServerPlayNetworking.registerGlobalReceiver(THROW_R_SKILL, RSkillPacket::receive);
    }

    public static void registerClient() {

    }
}
