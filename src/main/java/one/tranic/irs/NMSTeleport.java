package one.tranic.irs;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import one.tranic.irs.nms.impl.TeleportImpl;
import one.tranic.irs.platform.Platform;
import org.bukkit.Location;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;

public class NMSTeleport {
    private final static boolean folia = Platform.isMultithreading();

    private static TeleportImpl impl;

    static {
        try {
            Class<?> entityClass = Class.forName("net.minecraft.world.entity.Entity");
            boolean hasTeleportMethod = entityClass.getDeclaredMethod(
                    "teleportTo",
                    Class.forName("net.minecraft.server.level.ServerLevel"),
                    double.class,
                    double.class,
                    double.class,
                    Class.forName("java.util.Set"),
                    float.class,
                    float.class,
                    boolean.class
            ) != null;

            if (hasTeleportMethod) {
                impl = new one.tranic.irs.nms.v1214.NMSTeleport();
            } else {
                try {
                    Class.forName("org.bukkit.craftbukkit.v1_20_R1.CraftWorld");
                    impl = new one.tranic.irs.nms.v1201.NMSTeleport();
                } catch (ClassNotFoundException ignored) {
                    try {
                        Class.forName("org.bukkit.craftbukkit.CraftWorld");
                        impl = new one.tranic.irs.nms.v1206.NMSTeleport();
                    } catch (ClassNotFoundException ignored2) {
                        throw new UnsupportedOperationException("This server version does not support NMSTeleport.");
                    }
                }
            }
        } catch (Exception ignored) {
        }
    }

    public static void IMPL() {
        if (impl == null) throw new UnsupportedOperationException("This server version does not support NMSTeleport.");
    }

    public static void teleportAsync(org.bukkit.entity.Entity entity,
                                     Location location, boolean resetCamara) {
        IMPL();

        Vec3 pos = new Vec3(location.getX(), location.getY(), location.getZ());
        teleportAsync(impl.getNMSEntity(entity), pos, location.getYaw(), location.getPitch(), resetCamara, null);
    }

    public static void teleportAsync(org.bukkit.entity.Entity entity,
                                     double x, double y, double z, boolean resetCamara) {
        IMPL();

        Vec3 pos = new Vec3(x, y, z);
        teleportAsync(impl.getNMSEntity(entity), pos, resetCamara, null);
    }

    public static void teleportAsync(Entity entity, ServerLevel destination,
                                     @NotNull Vec3 pos,
                                     @Nullable org.bukkit.event.player.PlayerTeleportEvent.TeleportCause cause,
                                     float yaw,
                                     float pitch,
                                     long teleportFlags,
                                     boolean resetCamara,
                                     final Consumer<Entity> onComplete) {
        IMPL();

        if (folia) {
            impl.teleportAsync(entity, destination, pos, yaw, pitch, null, cause, teleportFlags, resetCamara, onComplete);
        } else {
            impl.teleportTo(entity, destination, pos.x, pos.y, pos.z, yaw, pitch, resetCamara);
            if (onComplete != null) onComplete.accept(entity);
        }
    }

    public static void teleportAsync(Entity entity, @NotNull net.minecraft.world.phys.Vec3 pos,
                                     float yaw,
                                     float pitch,
                                     boolean resetCamara,
                                     final Consumer<Entity> onComplete) {
        IMPL();
        teleportAsync(entity, impl.getServerLevel(entity), pos, null, yaw, pitch, 0, resetCamara, onComplete);
    }

    public static void teleportAsync(Entity entity, @NotNull net.minecraft.world.phys.Vec3 pos, boolean resetCamara,
                                     final Consumer<Entity> onComplete) {
        teleportAsync(entity, pos, 0, 0, resetCamara, onComplete);
    }

    public static void teleportAsync(Entity entity, float x, float y, float z, boolean resetCamara, final Consumer<Entity> onComplete) {
        teleportAsync(entity, new Vec3(x, y, z), resetCamara, onComplete);
    }

    public static void teleportAsync(Entity entity, float x, float y, float z, float yaw, float pitch, boolean resetCamara, final Consumer<Entity> onComplete) {
        teleportAsync(entity, new Vec3(x, y, z), yaw, pitch, resetCamara, onComplete);
    }
}
