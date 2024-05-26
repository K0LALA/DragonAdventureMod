package fr.kolala.dragonadventure.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.storage.LevelResource;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public class SpawnProtectionCommand {
    private static final int MAX_AREA = 65536;

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("spawnprot").requires(source -> source.hasPermission(4))
                .then(Commands.argument("x1", IntegerArgumentType.integer()).then(Commands.argument("y1", IntegerArgumentType.integer())
                        .then(Commands.argument("x2", IntegerArgumentType.integer()).then(Commands.argument("y2", IntegerArgumentType.integer())))))
                .executes(context -> executeSpawnProtectionGlobal(context.getSource(), IntegerArgumentType.getInteger(context, "x1"), IntegerArgumentType.getInteger(context, "y1"),
                        IntegerArgumentType.getInteger(context, "x2"), IntegerArgumentType.getInteger(context, "y2")))

                .then(Commands.literal("first").executes(context -> executeSpawnProtectionLocal(context.getSource(), (int) context.getSource().getPosition().x, (int) context.getSource().getPosition().z, true))
                        .then(Commands.argument("x", IntegerArgumentType.integer()).then(Commands.argument("y", IntegerArgumentType.integer())
                                .executes(context -> executeSpawnProtectionLocal(context.getSource(), IntegerArgumentType.getInteger(context, "x"), IntegerArgumentType.getInteger(context, "y"), true)))))
                .then(Commands.literal("second").executes(context -> executeSpawnProtectionLocal(context.getSource(), (int) context.getSource().getPosition().x, (int) context.getSource().getPosition().z, false))
                        .then(Commands.argument("x", IntegerArgumentType.integer()).then(Commands.argument("y", IntegerArgumentType.integer())
                                .executes(context -> executeSpawnProtectionLocal(context.getSource(), IntegerArgumentType.getInteger(context, "x"), IntegerArgumentType.getInteger(context, "y"), false))))));
    }

    private static int executeSpawnProtectionGlobal(CommandSourceStack source, int x1, int z1, int x2, int z2) {
        // Calculate the area covered
        int area = Math.abs(x1 - x2) * Math.abs(z1 - z2);
        if (area > MAX_AREA)
        {
            source.sendFailure(new TextComponent("The selected area is too big"));
        }

        int correctExecution;
        correctExecution = executeSpawnProtectionLocal(source, x1, z1, true);
        correctExecution = executeSpawnProtectionLocal(source, x2, z2, false) == 1 ? correctExecution : 0;
        return correctExecution;
    }

    private static int executeSpawnProtectionLocal(CommandSourceStack source, int x, int z, boolean first) {
        ServerLevel level = source.getLevel();

        // Display a warning if the player is not in the overworld
        if (level.dimension() != ServerLevel.OVERWORLD) {
            source.sendFailure(new TextComponent("You can't set a spawn protection outside the Overworld!"));
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
        }

        // Change the coordinates in the file to the new coordinates
        // Check if the file is usable
        if (!Files.isReadable(spawnProtFile))
            source.sendFailure(new TextComponent("The file is not readable"));
        if (!Files.isWritable(spawnProtFile))
            source.sendFailure(new TextComponent("The file is not writable"));
        if (!Files.isRegularFile(spawnProtFile))
            source.sendFailure(new TextComponent("The file is not a file :/"));

        // Write new coordinates
        try {
            List<String> lines = Files.readAllLines(spawnProtFile);
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
            source.sendFailure(new TextComponent("An exception occurred!"));
            return 0;
        }

        return 1;
    }

}
