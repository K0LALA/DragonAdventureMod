package fr.kolala.dragonadventure.events;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.storage.LevelResource;
import net.minecraftforge.event.entity.living.LivingHurtEvent;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Objects;

public class ModEvents {

    /**
     * Called when an entity takes a damage
     * No need to add event annotation, since a listener is added into the mod's main class
     * @param event The {@link LivingHurtEvent}
     */
    public static void livingDamageEventHandler(LivingHurtEvent event) {
        Entity entity = event.getEntity();

        if (entity == null || entity.getType() != EntityType.PLAYER) return;

        // Read spawnprot coordinates
        int x1, z1, x2, z2;
        Path spawnProtFile = Objects.requireNonNull(entity.getServer()).getWorldPath(LevelResource.ROOT).resolve("spawnprot.txt");
        if (!Files.exists(spawnProtFile)
                || !Files.isReadable(spawnProtFile)
                || !Files.isWritable(spawnProtFile)
                || !Files.isRegularFile(spawnProtFile)) return;

        List<String> lines;
        try {
            lines = Files.readAllLines(spawnProtFile);
        } catch (IOException e) {
            return;
        }
        if (lines.isEmpty())
            return;
        x1 = Integer.parseInt(lines.get(0));
        z1 = Integer.parseInt(lines.get(1));
        x2 = Integer.parseInt(lines.get(2));
        z2 = Integer.parseInt(lines.get(3));

        if (inBounds(entity.getOnPos(), x1, z1, x2, z2)) {
            // Cancel the taken damages
            event.setCanceled(true);
        }
    }

    /**
     * Get if a position is in bounds
     * @param pos The position to check for
     * @param x1 First X coordinate
     * @param z1 First Z coordinate
     * @param x2 Second X coordinate
     * @param z2 Second Z coordinate
     * @return Whether the position is in the bounds
     */
    private static boolean inBounds(BlockPos pos, int x1, int z1, int x2, int z2) {
        int xPos = pos.getX();
        int zPos = pos.getZ();
        return between(xPos, x1, x2) && between(zPos, z1, z2);
    }

    /**
     * Check if a value is between two other (unsorted)
     * @param value The value to check
     * @param a First bound
     * @param b Second bound
     * @return Whether the value is between the two other
     */
    private static boolean between(int value, int a, int b) {
        if (a == b)
            return value == a;
        else if (a < b)
            return value >= a && value <= b;
        else
            return value <= a && value >= b;
    }

}
