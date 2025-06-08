package leaguemod.brand;
import leaguemod.brand.effects.BlazeEffect;
import leaguemod.brand.network.ModPackets;
import net.fabricmc.api.ModInitializer;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LeagueModBrand implements ModInitializer {
	public static final String MOD_ID = "leaguemod_brand";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
	public static final StatusEffect BLAZE_EFFECT = new BlazeEffect();

	public static final Float Q_DAMAGE = 3f; // урон от Q
	public static final int Q_STUN = 30; // Время стана В ТИКАХ (20 тиков = 1 секунда)
	public static final int Q_LIFE_TIME = 10; // Время жизни снаряда В ТИКАХ (20 тиков = 1 секунда)

	public static final Float W_SHOOT_DISTANCE = 7f; // Дальность использования
	public static final Float W_AREA_RADIUS = 5f; // радиус поражения
	public static final Float W_AREA_DAMAGE = 5f; // урон от W

	public static final Float E_SHOOT_DISTANCE = 7f; // дальность использования
	public static final Float E_DAMAGE = 1f; // урон E
	public static final Float E_BASE_RANGE = 3f; // радиус распространения огня

	public static final Float R_BASE_RANGE = 5f; // дальность использования R
	public static final Float R_DAMAGE = 2f; // урон
	public static final Integer R_BOUNCES = 5; // кол-во отскоков
	public  static final Float R_TRAVEL_SPEED = 0.2f; //скорость снаряда

	public static final Float BLAZE_TICK_DAMAGE = 1f; //урон от стака горения
	public static final Float BLAZE_BLOW_DAMAGE = 7.5f; //урон взрыва (3 стака)



	@Override
	public void onInitialize() {
		LOGGER.info("Start brand mod");
		ModPackets.register();
		Registry.register(Registries.STATUS_EFFECT, new Identifier(MOD_ID,"blaze"),BLAZE_EFFECT);
	}
}