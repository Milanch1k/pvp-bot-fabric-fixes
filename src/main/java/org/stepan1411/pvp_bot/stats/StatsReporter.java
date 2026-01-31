package org.stepan1411.pvp_bot.stats;

import com.google.gson.JsonObject;
import net.fabricmc.loader.api.FabricLoader;
import org.stepan1411.pvp_bot.bot.BotManager;
import org.stepan1411.pvp_bot.bot.BotSettings;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Отправляет анонимную статистику на сервер
 */
public class StatsReporter {
    
    private static final String STATS_ENDPOINT = "https://pvpbot-stats.up.railway.app/api/stats";
    private static final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    private static String serverId = null;
    private static boolean enabled = true;
    
    /**
     * Запускает периодическую отправку статистики
     */
    public static void start() {
        // Проверяем настройки
        BotSettings settings = BotSettings.get();
        if (!settings.isSendStats()) {
            System.out.println("[PVP_BOT] Statistics reporting disabled in settings");
            enabled = false;
            return;
        }
        
        // Загружаем или создаём ID сервера
        serverId = loadOrCreateServerId();
        
        // Отправляем статистику сразу при старте
        sendStats();
        
        // Отправляем каждый час
        scheduler.scheduleAtFixedRate(() -> {
            try {
                sendStats();
            } catch (Exception e) {
                // Тихо игнорируем ошибки чтобы не спамить логи
            }
        }, 1, 1, TimeUnit.HOURS);
        
        System.out.println("[PVP_BOT] Statistics reporter started (Server ID: " + serverId.substring(0, 8) + "...)");
    }
    
    /**
     * Останавливает отправку статистики
     */
    public static void stop() {
        scheduler.shutdown();
        // Отправляем финальную статистику с bots_count = 0
        if (enabled) {
            sendStats();
        }
    }
    
    /**
     * Отправляет статистику на сервер
     */
    private static void sendStats() {
        if (!enabled || serverId == null) {
            return;
        }
        
        try {
            JsonObject stats = new JsonObject();
            stats.addProperty("server_id", serverId);
            stats.addProperty("bots_count", BotManager.getAllBots().size());
            stats.addProperty("mod_version", getModVersion());
            stats.addProperty("minecraft_version", "1.21.11");
            stats.addProperty("timestamp", System.currentTimeMillis());
            
            // Отправляем POST запрос
            HttpClient client = HttpClient.newBuilder()
                    .connectTimeout(Duration.ofSeconds(10))
                    .build();
            
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(STATS_ENDPOINT))
                    .header("Content-Type", "application/json")
                    .header("User-Agent", "PVPBOT-Stats/1.0")
                    .timeout(Duration.ofSeconds(10))
                    .POST(HttpRequest.BodyPublishers.ofString(stats.toString()))
                    .build();
            
            client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                    .thenAccept(response -> {
                        if (response.statusCode() == 200 || response.statusCode() == 201) {
                            // Успешно отправлено
                        }
                    })
                    .exceptionally(e -> {
                        // Тихо игнорируем ошибки
                        return null;
                    });
            
        } catch (Exception e) {
            // Тихо игнорируем ошибки
        }
    }
    
    /**
     * Загружает или создаёт уникальный ID сервера
     */
    private static String loadOrCreateServerId() {
        try {
            Path configDir = FabricLoader.getInstance().getConfigDir().resolve("pvpbot");
            Path serverIdFile = configDir.resolve("server_id.txt");
            
            if (Files.exists(serverIdFile)) {
                return Files.readString(serverIdFile).trim();
            } else {
                String newId = UUID.randomUUID().toString();
                Files.createDirectories(configDir);
                Files.writeString(serverIdFile, newId);
                return newId;
            }
        } catch (IOException e) {
            // Если не можем сохранить - генерируем временный ID
            return UUID.randomUUID().toString();
        }
    }
    
    /**
     * Получает версию мода
     */
    private static String getModVersion() {
        return FabricLoader.getInstance()
                .getModContainer("pvp_bot")
                .map(mod -> mod.getMetadata().getVersion().getFriendlyString())
                .orElse("unknown");
    }
}
