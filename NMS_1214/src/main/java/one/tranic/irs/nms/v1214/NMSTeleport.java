package one.tranic.irs.nms.v1214;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Set;
import java.util.function.Consumer;

public class NMSTeleport extends one.tranic.irs.nms.v1206.NMSTeleport {
    @Override
    public void teleportAsync(Entity entity,
                              ServerLevel destination,
                              @NotNull Vec3 pos,
                              Float yaw,
                              Float pitch,
                              Vec3 velocity,
                              @Nullable PlayerTeleportEvent.TeleportCause cause,
                              long teleportFlags,
                              boolean resetCamara,
                              Consumer<Entity> teleportComplete) {
        super.teleportAsync(entity, destination, pos, yaw, pitch, velocity, cause, teleportFlags, teleportComplete(resetCamara, teleportComplete));
    }

    private Consumer<Entity> teleportComplete(boolean resetCamara, Consumer<Entity> teleportComplete) {
        return entity -> {
            if (resetCamara && entity instanceof ServerPlayer player) player.setCamera(player);
            if (teleportComplete != null) teleportComplete.accept(entity);
        };
    }

    @Override
    public void teleportTo(Entity entity, ServerLevel destination, double x, double y, double z, float yaw, float pitch, boolean resetCamara) {
        entity.teleportTo(destination, x, y, z, Set.of(), yaw, pitch, resetCamara);
    }
}
