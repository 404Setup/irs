package one.tranic.irs.nms.impl;

import io.papermc.paper.threadedregions.EntityScheduler;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import org.bukkit.Location;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;

public interface TeleportImpl {
    default void teleportAsync(Entity entity,
                               ServerLevel destination,
                               @NotNull Vec3 pos,
                               Float yaw,
                               Float pitch,
                               Vec3 velocity,
                               @Nullable PlayerTeleportEvent.TeleportCause cause,
                               long teleportFlags,
                               Consumer<Entity> teleportComplete) {
        taskScheduler(entity).schedule(
                (final Entity realFrom) -> realFrom.teleportAsync(
                        destination, pos, yaw, pitch, velocity,
                        cause, teleportFlags, teleportComplete
                ),
                (final Entity retired) -> {
                    if (teleportComplete != null) teleportComplete.accept(null);
                },
                1L);
    }

    default void teleportAsync(Entity entity,
                               ServerLevel destination,
                               @NotNull Vec3 pos,
                               Float yaw,
                               Float pitch,
                               Vec3 velocity,
                               @Nullable PlayerTeleportEvent.TeleportCause cause,
                               long teleportFlags,
                               boolean resetCamara,
                               Consumer<Entity> teleportComplete) {
        teleportAsync(entity, destination, pos, yaw, pitch, velocity, cause, teleportFlags, teleportComplete);
    }

    default void teleportTo(Entity entity, ServerLevel destination, double x, double y, double z, float yaw, float pitch) {
        teleportTo(entity, destination, x, y, z, yaw, pitch, false);
    }

    void teleportTo(Entity entity, ServerLevel destination, double x, double y, double z, float yaw, float pitch, boolean resetCamara);

    default Location getLocation(Entity entity) {
        return entity.getBukkitEntity().getLocation();
    }

    Entity getNMSEntity(org.bukkit.entity.Entity entity);

    ServerLevel getServerLevel(Entity entity);

    EntityScheduler taskScheduler(org.bukkit.entity.Entity entity);

    EntityScheduler taskScheduler(Entity entity);
}
