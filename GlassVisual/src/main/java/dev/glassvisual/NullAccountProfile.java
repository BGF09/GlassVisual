package dev.glassvisual;

import net.minecraft.client.MinecraftClient;
import java.nio.file.Files;
import java.nio.file.Path;

public final class NullAccountProfile {
    private static String login = "Guest";
    private NullAccountProfile() {}
    public static void load() {
        try {
            Path file = MinecraftClient.getInstance().runDirectory.toPath().resolve("NULL").resolve("active-account.txt");
            if (Files.isRegularFile(file)) {
                String value = Files.readString(file).trim();
                if (!value.isBlank()) login = value;
            }
        } catch (Exception ignored) { login = "Guest"; }
    }
    public static String login() { return login; }
}
