package fr.kolala.dragonadventure;

import com.mojang.logging.LogUtils;
import fr.kolala.dragonadventure.commands.RandomTpCommand;
import fr.kolala.dragonadventure.commands.SpawnProtectionCommand;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.slf4j.Logger;

@Mod(fr.kolala.dragonadventure.DragonAdventure.MOD_ID)
public class DragonAdventure
{
    public static final String MOD_ID = "dragonadventure";

    private static final Logger LOGGER = LogUtils.getLogger();

    public DragonAdventure()
    {
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::setup);

        MinecraftForge.EVENT_BUS.register(this);
        MinecraftForge.EVENT_BUS.addListener(this::registerCommands);
    }

    private void setup(final FMLCommonSetupEvent event)
    {
        LOGGER.info("Initializing Dragon Adventure.");
    }

    private void registerCommands(final RegisterCommandsEvent event) {
        LOGGER.info("Initializing commands.");
        RandomTpCommand.register(event.getDispatcher());
        SpawnProtectionCommand.register(event.getDispatcher());
    }
}
