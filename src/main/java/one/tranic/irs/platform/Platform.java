package one.tranic.irs.platform;

import org.jetbrains.annotations.NotNull;

/**
 * Enum representing various Minecraft server platforms.
 * Supported platforms include:
 * <ul>
 *     <li>{@link #Velocity}</li>
 *     <li>{@link #BungeeCord}</li>
 *     <li>{@link #Spigot}</li>
 *     <li>{@link #Paper}</li>
 *     <li>{@link #ShreddedPaper}</li>
 *     <li>{@link #Folia}</li>
 * </ul>
 */
public enum Platform {
    /**
     * Represents the Velocity platform.
     * Velocity is a modern Minecraft proxy server designed for high performance and flexibility.
     */
    Velocity,

    /**
     * Represents the BungeeCord platform.
     * BungeeCord is a classic Minecraft proxy server.
     */
    BungeeCord,

    /**
     * Represents the Spigot platform.
     * Spigot is a highly optimized Minecraft server software, built from the vanilla Minecraft server,
     * and is widely used for plugin support and performance improvements.
     */
    Spigot,

    /**
     * Represents the Paper platform.
     * Paper is a fork of Spigot that further optimizes server performance and adds additional features for plugins.
     */
    Paper,

    /**
     * Represents the ShreddedPaper platform.
     * ShreddedPaper is a highly specialized fork of Paper, designed for improved threading in Minecraft servers.
     */
    ShreddedPaper,

    /**
     * Represents the Folia platform.
     * Folia is a specialized fork of Paper with region-based multi-threading support for high-concurrency environments.
     */
    Folia;

    /**
     * A cached value of the detected platform. The platform is only determined once using reflection
     * and the result is cached for future calls to {@link #get()}.
     */
    private static Platform platform;

    /**
     * Detects and returns the current platform.
     * <p>
     * This method uses reflection to check for specific platform classes and methods in the runtime
     * environment, caching the result after the first check to avoid unnecessary overhead in future calls.
     * </p>
     *
     * @return the detected {@link Platform}, or {@link Platform#Spigot} as the default if no other platform is found.
     */
    public static @NotNull Platform get() {
        if (platform != null) {
            return platform;
        }

        // Test for Velocity platform
        try {
            Class.forName("com.velocitypowered.api.proxy.Player");
            platform = Velocity;
            return platform;
        } catch (ClassNotFoundException e) {
            // Not Velocity
        }

        // Test for BungeeCord platform
        try {
            Class.forName("net.md_5.bungee.api.CommandSender");
            platform = BungeeCord;
            return platform;
        } catch (ClassNotFoundException e) {
            // Not BungeeCord or its forks
        }

        // Test for Folia platform
        try {
            Class.forName("io.papermc.paper.threadedregions.commands.CommandServerHealth");
            platform = Folia;
            return platform;
        } catch (ClassNotFoundException e) {
            // Not Folia or its forks
        }

        // Test for ShreddedPaper platform
        try {
            Class.forName("io.multipaper.shreddedpaper.threading.ShreddedPaperTickThread");
            platform = ShreddedPaper;
            return platform;
        } catch (ClassNotFoundException e) {
            // Not ShreddedPaper or its forks
        }

        // Test for Paper platform
        try {
            Class.forName("io.papermc.paper.util.MCUtil");
            platform = Paper;
            return platform;
        } catch (ClassNotFoundException e) {
            // Default to Spigot if none of the above
        }

        platform = Spigot;
        return Spigot;
    }

    /**
     * Returns the {@link Platform} corresponding to the provided platform name.
     * <p>
     * This method compares the provided platform name (case-insensitive) to known platforms
     * and returns the appropriate enum constant.
     * </p>
     *
     * @param name the name of the platform (e.g., "velocity", "spigot")
     * @return the corresponding {@link Platform} enum constant
     * @throws IllegalArgumentException if the platform name is unknown
     */
    public static @NotNull Platform of(@NotNull String name) {
        return switch (name.toLowerCase()) {
            case "velocity" -> Velocity;
            case "bungeecord" -> BungeeCord;
            case "spigot" -> Spigot;
            case "paper" -> Paper;
            case "shreddedpaper" -> ShreddedPaper;
            case "folia" -> Folia;
            default -> throw new IllegalArgumentException("Unknown platform: " + name);
        };
    }

    public static boolean isMultithreading() {
        return get() == Folia || get() == ShreddedPaper;
    }

    /**
     * Returns the lowercase string representation of the platform.
     *
     * @return the platform name in lowercase (e.g., "velocity", "spigot").
     */
    @Override
    public @NotNull String toString() {
        return switch (this) {
            case Velocity -> "velocity";
            case BungeeCord -> "bungeecord";
            case Spigot -> "spigot";
            case Paper -> "paper";
            case ShreddedPaper -> "shreddedpaper";
            case Folia -> "folia";
        };
    }

    public @NotNull String toRawString() {
        return switch (this) {
            case Velocity -> "Velocity";
            case BungeeCord -> "BungeeCord";
            case Spigot -> "Spigot";
            case Paper -> "Paper";
            case ShreddedPaper -> "ShreddedPaper";
            case Folia -> "Folia";
        };
    }
}
