package fr.kolala.dragonadventure.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import fr.kolala.dragonadventure.DragonAdventure;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.storage.LevelResource;
import org.slf4j.Logger;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public class SpawnProtectionCommand {
    private static final int MAX_AREA = 65536;
    private static Logger LOGGER;

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        LOGGER = DragonAdventure.getLogger();

        dispatcher.register(Commands.literal("spawnprot").requires(source -> source.hasPermission(4))
                .then(Commands.argument("x1", IntegerArgumentType.integer()).then(Commands.argument("z1", IntegerArgumentType.integer())
                        .then(Commands.argument("x2", IntegerArgumentType.integer()).then(Commands.argument("z2", IntegerArgumentType.integer())
                .executes(context -> executeSpawnProtectionGlobal(context.getSource(), IntegerArgumentType.getInteger(context, "x1"), IntegerArgumentType.getInteger(context, "z1"),
                        IntegerArgumentType.getInteger(context, "x2"), IntegerArgumentType.getInteger(context, "z2")))))))

                .then(Commands.literal("first").executes(context -> executeSpawnProtectionLocal(context.getSource(), (int) context.getSource().getPosition().x, (int) context.getSource().getPosition().z, true))
                        .then(Commands.argument("x", IntegerArgumentType.integer()).then(Commands.argument("z", IntegerArgumentType.integer())
                                .executes(context -> executeSpawnProtectionLocal(context.getSource(), IntegerArgumentType.getInteger(context, "x"), IntegerArgumentType.getInteger(context, "z"), true)))))
                .then(Commands.literal("second").executes(context -> executeSpawnProtectionLocal(context.getSource(), (int) context.getSource().getPosition().x, (int) context.getSource().getPosition().z, false))
                        .then(Commands.argument("x", IntegerArgumentType.integer()).then(Commands.argument("z", IntegerArgumentType.integer())
                                .executes(context -> executeSpawnProtectionLocal(context.getSource(), IntegerArgumentType.getInteger(context, "x"), IntegerArgumentType.getInteger(context, "z"), false))))));
    }

    /**
     * Executes the global spawn protection command
     * @param source {@link CommandSourceStack} of the command
     * @param x1 First X coordinate
     * @param z1 First Z coordinate
     * @param x2 Second X coordinate
     * @param z2 Second Z coordinate
     * @return The execution status with everything except 1 meaning a problem occurred
     */
    private static int executeSpawnProtectionGlobal(CommandSourceStack source, int x1, int z1, int x2, int z2) {
        // Calculate the area covered
        int area = Math.abs(x1 - x2) * Math.abs(z1 - z2);
        if (area > MAX_AREA)
        {
            source.sendFailure(new TranslatableComponent("command.dragonadventure.spawnprot.too_big", area, MAX_AREA));
        }

        int correctExecution;
        correctExecution = executeSpawnProtectionLocal(source, x1, z1, true);
        correctExecution = executeSpawnProtectionLocal(source, x2, z2, false) == 1 ? correctExecution : 0;
        return correctExecution;
    }

    /**
     * Executes the local spawn protection command, called independently or from {@link SpawnProtectionCommand#executeSpawnProtectionGlobal(CommandSourceStack, int, int, int, int)}
     * @param source {@link CommandSourceStack} of the command
     * @param x X coordinate
     * @param z Z coordinate
     * @param first Indicate if it is the first or second point
     * @return The execution status with everything except 1 meaning a problem occurred
     */
    private static int executeSpawnProtectionLocal(CommandSourceStack source, int x, int z, boolean first) {
        
        ServerLevel level = source.getLevel();

        // Display a warning if the player is not in the overworld
        if (level.dimension() != ServerLevel.OVERWORLD) {
            source.sendFailure(new TranslatableComponent("command.dragonadventure.spawnprot.dimension"));
            return 0;
        }

        // Check if a spawn file exists
        Path spawnProtFile = source.getServer().getWorldPath(LevelResource.ROOT).resolve("spawnprot.txt");
        try {
            // Create one if not
            if (!Files.exists(spawnProtFile)) {
                Files.createFile(spawnProtFile);
            }
        } catch (IOException e) {
            source.sendFailure(new TextComponent("Couldn't create file: " + spawnProtFile));
            LOGGER.trace(e.getMessage());
            return 0;
        }

        // Change the coordinates in the file to the new coordinates
        // Check if the file is usable
        if (!Files.isReadable(spawnProtFile)) {
            source.sendFailure(new TextComponent("The file is not readable"));
            return 0;
        }
        if (!Files.isWritable(spawnProtFile)) {
            source.sendFailure(new TextComponent("The file is not writable"));
            return 0;
        }
        if (!Files.isRegularFile(spawnProtFile)) {
            source.sendFailure(new TextComponent("The file is not a file :/"));
            return 0;
        }

        // Write new coordinates
        try {
            List<String> lines = Files.readAllLines(spawnProtFile);
            for (int i = lines.size(); i < 4; i++) {
                lines.add(i, "");
            }

            if (first) {
                lines.set(0, String.valueOf(x));
                lines.set(1, String.valueOf(z));
            }
            else {
                lines.set(2, String.valueOf(x));
                lines.set(3, String.valueOf(z));
            }
            Files.write(spawnProtFile, lines);
        } catch (IOException e) {
            source.sendFailure(new TranslatableComponent("command.dragonadventure.spawnprot.error"));
            LOGGER.trace(e.getMessage());
            
            return 0;
        }

        source.sendSuccess(new TranslatableComponent("command.dragonadventure.spawnprot.success", first ? 2 : 1, x, z), true);

        return 1;
    }

}
