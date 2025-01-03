package one.tranic.irs.nms.v1206;

import io.papermc.paper.threadedregions.EntityScheduler;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import one.tranic.irs.nms.impl.TeleportImpl;
import org.bukkit.craftbukkit.CraftWorld;
import org.bukkit.craftbukkit.entity.CraftEntity;

import java.util.Set;

public class NMSTeleport implements TeleportImpl {
    @Override
    public ServerLevel getServerLevel(Entity entity) {
        return ((CraftWorld) getLocation(entity).getWorld()).getHandle();
    }

    @Override
    public void teleportTo(Entity entity, ServerLevel destination, double x, double y, double z, float yaw, float pitch, boolean resetCamara) {
        entity.teleportTo(destination, x, y, z, Set.of(), yaw, pitch);
    }

    @Override
    public Entity getNMSEntity(org.bukkit.entity.Entity entity) {
        return ((CraftEntity) entity).getHandle();
    }

    @Override
    public EntityScheduler taskScheduler(org.bukkit.entity.Entity entity) {
        return ((CraftEntity) entity).taskScheduler;
    }

    @Override
    public EntityScheduler taskScheduler(Entity entity) {
        return entity.getBukkitEntity().taskScheduler;
    }
}
