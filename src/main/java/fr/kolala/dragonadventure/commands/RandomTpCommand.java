package fr.kolala.dragonadventure.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.SpawnPlacements;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.event.world.BlockEvent;

public class RandomTpCommand {


    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("rtp").executes(context -> executeRandomTp(context.getSource(), 11))
                .then(Commands.argument("multiplier", IntegerArgumentType.integer(9, 15)).executes(context -> executeRandomTp(context.getSource(), IntegerArgumentType.getInteger(context, "multiplier")))));
    }

    private static int executeRandomTp(CommandSourceStack source, int multiplier) throws CommandSyntaxException {
        ServerPlayer player = source.getPlayerOrException();
        Level level = player.getLevel();
        if (level.dimension() != Level.OVERWORLD) {
            source.sendFailure(new TextComponent("You can't teleport randomly outside the Overworld!"));
            return 0;
        }
        for (int i = 0; i < 5; i++){
            source.sendSuccess(new TextComponent("Try #" + (i+1)), false);
            int x = (int) ((Math.random() + 1) * Math.pow(2, multiplier) + player.getX());
            int z = (int) ((Math.random() + 1) * Math.pow(2, multiplier) + player.getZ());
            BlockPos blockPos = new BlockPos(x, 319, z);
            source.sendSuccess(new TextComponent("Highest point found: " + level.getBlockFloorHeight(blockPos)), true);
            boolean found = true;
            while (!canTeleport(level, blockPos)) {
                blockPos = blockPos.below();
                if (blockPos.getY() <= 63) {
                    found = false;
                    break;
                }
            }
            if (found) {
                player.teleportTo(blockPos.getX(), blockPos.getY() + 1, blockPos.getZ());
                source.sendSuccess(new TranslatableComponent("command.dragonadventure.rtp.success", blockPos.getX(), blockPos.getY(), blockPos.getZ()), true);
                return 0;
            }
        }

        source.sendFailure(new TranslatableComponent("command.dragonadventure.rtp.not_found"));
        return 1;
    }

    private static boolean canTeleport(Level level, BlockPos blockPos) {
        BlockState state = level.getBlockState(blockPos);
        LevelReader levelReader = new BlockEvent(level, blockPos, state).getWorld();
        return SpawnPlacements.Type.ON_GROUND.canSpawnAt(levelReader, blockPos, EntityType.PLAYER)
                && !SpawnPlacements.Type.IN_WATER.canSpawnAt(levelReader, blockPos, EntityType.PLAYER)
                && !SpawnPlacements.Type.IN_LAVA.canSpawnAt(levelReader, blockPos, EntityType.PLAYER);
    }

}
