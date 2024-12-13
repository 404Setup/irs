package one.tranic.irs;

import one.tranic.irs.platform.Platform;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.event.player.PlayerTeleportEvent;

import java.util.concurrent.CompletableFuture;

public class Teleport {
    private final static boolean folia = Platform.isMultithreading();

    public static boolean teleport(Entity entity, Location location) {
        if (folia) {
            entity.teleportAsync(location);
            return true;
        }
        return entity.teleport(location);
    }

    public static boolean teleport(Entity entity, Entity destination) {
        if (folia) {
            entity.teleportAsync(destination.getLocation());
            return true;
        }
        return entity.teleport(destination);
    }

    public static boolean teleport(Entity entity, Location location, PlayerTeleportEvent.TeleportCause cause) {
        if (folia) {
            entity.teleportAsync(location, cause);
            return true;
        }
        return entity.teleport(location, cause);
    }

    public static CompletableFuture<Boolean> teleportAsync(Entity entity, Location location) {
        return folia ? entity.teleportAsync(location)
                : CompletableFuture.supplyAsync(() -> entity.teleport(location));
    }

    public static CompletableFuture<Boolean> teleportAsync(Entity entity, Entity destination) {
        return folia ? entity.teleportAsync(destination.getLocation())
                : CompletableFuture.supplyAsync(() -> entity.teleport(destination));
    }

    public static CompletableFuture<Boolean> teleportAsync(Entity entity, Location location, PlayerTeleportEvent.TeleportCause cause) {
        return folia ? entity.teleportAsync(location, cause)
                : CompletableFuture.supplyAsync(() -> entity.teleport(location, cause));
    }
}
