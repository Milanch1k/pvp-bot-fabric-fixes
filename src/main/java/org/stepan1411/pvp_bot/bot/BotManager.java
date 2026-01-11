package org.stepan1411.pvp_bot.bot;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.world.GameMode;

import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class BotManager {

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Set<String> bots = new HashSet<>();
    private static final Map<String, BotData> botDataMap = new HashMap<>();
    private static Path savePath;
    private static boolean initialized = false;
    
    /**
     * Данные бота для сохранения
     */
    public static class BotData {
        public String name;
        public double x, y, z;
        public float yaw, pitch;
        public String dimension; // minecraft:overworld, minecraft:the_nether, minecraft:the_end
        public String gamemode; // survival, creative, adventure, spectator
        
        public BotData() {}
        
        public BotData(ServerPlayerEntity bot) {
            this.name = bot.getName().getString();
            this.x = bot.getX();
            this.y = bot.getY();
            this.z = bot.getZ();
            this.yaw = bot.getYaw();
            this.pitch = bot.getPitch();
            this.dimension = bot.getEntityWorld().getRegistryKey().getValue().toString();
            this.gamemode = bot.interactionManager.getGameMode().asString();
        }
    }

    /**
     * Инициализация - загрузка сохранённых ботов
     */
    public static void init(MinecraftServer server) {
        if (initialized) return;
        
        // Создаём папку config/pvpbot если не существует
        Path configDir = FabricLoader.getInstance().getConfigDir().resolve("pvpbot");
        try {
            Files.createDirectories(configDir);
        } catch (Exception e) {
            System.out.println("[PVP_BOT] Failed to create config directory: " + e.getMessage());
        }
        
        savePath = configDir.resolve("bots.json");
        loadBots();
        
        // Респавним сохранённых ботов с задержкой
        if (!botDataMap.isEmpty()) {
            System.out.println("[PVP_BOT] Restoring " + botDataMap.size() + " bots...");
            Map<String, BotData> botsToRestore = new HashMap<>(botDataMap);
            bots.clear();
            botDataMap.clear();
            
            // Запускаем респавн с задержкой
            server.execute(() -> restoreBotsDelayed(server, botsToRestore, 0));
        }
        
        initialized = true;
    }
    
    private static void restoreBotsDelayed(MinecraftServer server, Map<String, BotData> botsToRestore, int index) {
        if (index >= botsToRestore.size()) {
            System.out.println("[PVP_BOT] Restored " + bots.size() + " bots");
            return;
        }
        
        String[] names = botsToRestore.keySet().toArray(new String[0]);
        if (index < names.length) {
            String name = names[index];
            BotData data = botsToRestore.get(name);
            
            // Спавним бота с позицией и измерением
            var dispatcher = server.getCommandManager().getDispatcher();
            try {
                // Формат: player NAME spawn at X Y Z facing YAW PITCH in DIMENSION in GAMEMODE
                String command = String.format(
                    "player %s spawn at %.2f %.2f %.2f facing %.2f %.2f in %s in %s",
                    name, data.x, data.y, data.z, data.yaw, data.pitch, data.dimension, data.gamemode
                );
                dispatcher.execute(command, server.getCommandSource());
                bots.add(name);
                botDataMap.put(name, data);
            } catch (Exception e) {
                // Пробуем упрощённую команду
                try {
                    String simpleCommand = String.format(
                        "player %s spawn at %.2f %.2f %.2f",
                        name, data.x, data.y, data.z
                    );
                    dispatcher.execute(simpleCommand, server.getCommandSource());
                    bots.add(name);
                    botDataMap.put(name, data);
                } catch (Exception e2) {
                    System.out.println("[PVP_BOT] Failed to restore bot: " + name);
                }
            }
            
            // Следующий бот через 10 тиков
            final int nextIndex = index + 1;
            server.execute(() -> {
                final int[] delay = {0};
                server.execute(new Runnable() {
                    @Override
                    public void run() {
                        delay[0]++;
                        if (delay[0] < 10) {
                            server.execute(this);
                        } else {
                            restoreBotsDelayed(server, botsToRestore, nextIndex);
                        }
                    }
                });
            });
        }
    }
    
    /**
     * Обновление данных всех ботов перед сохранением
     */
    public static void updateBotData(MinecraftServer server) {
        for (String name : bots) {
            ServerPlayerEntity bot = server.getPlayerManager().getPlayer(name);
            if (bot != null && bot.isAlive()) {
                botDataMap.put(name, new BotData(bot));
            }
        }
    }
    
    /**
     * Сохранение списка ботов
     */
    public static void saveBots() {
        if (savePath == null) return;
        
        try (Writer writer = Files.newBufferedWriter(savePath)) {
            GSON.toJson(botDataMap, writer);
        } catch (Exception e) {
            System.out.println("[PVP_BOT] Failed to save bots: " + e.getMessage());
        }
    }
    
    /**
     * Загрузка списка ботов
     */
    private static void loadBots() {
        if (savePath == null || !Files.exists(savePath)) return;
        
        try (Reader reader = Files.newBufferedReader(savePath)) {
            Map<String, BotData> loaded = GSON.fromJson(reader, new TypeToken<Map<String, BotData>>(){}.getType());
            if (loaded != null) {
                botDataMap.putAll(loaded);
                bots.addAll(loaded.keySet());
            }
        } catch (Exception e) {
            System.out.println("[PVP_BOT] Failed to load bots: " + e.getMessage());
        }
    }
    
    /**
     * Сброс при выходе из мира
     */
    public static void reset(MinecraftServer server) {
        updateBotData(server);
        saveBots();
        initialized = false;
    }

    public static boolean spawnBot(MinecraftServer server, String name, ServerCommandSource source) {
        // Проверяем, существует ли уже игрок с таким именем на сервере
        ServerPlayerEntity existingPlayer = server.getPlayerManager().getPlayer(name);
        if (existingPlayer != null && existingPlayer.isAlive()) {
            // Бот уже существует и жив
            if (!bots.contains(name)) {
                bots.add(name); // Добавляем в список если не было
            }
            return false;
        }

        // Execute Carpet's /player command - spawn in survival mode
        var dispatcher = server.getCommandManager().getDispatcher();
        try {
            // Spawn bot in survival mode using Carpet syntax
            dispatcher.execute("player " + name + " spawn in survival", source);
        } catch (Exception e) {
            // Try alternative method if first fails
            try {
                dispatcher.execute("player " + name + " spawn", source);
                // Force gamemode change after spawn
                dispatcher.execute("gamemode survival " + name, server.getCommandSource());
            } catch (Exception e2) {
                return false;
            }
        }
        
        bots.add(name);
        saveBots();
        return true;
    }

    public static boolean removeBot(MinecraftServer server, String name, ServerCommandSource source) {
        // Удаляем из списка в любом случае
        boolean wasInList = bots.remove(name);
        saveBots();
        
        // Очищаем все состояния бота
        BotCombat.removeState(name);
        BotUtils.removeState(name);
        BotNavigation.resetIdle(name);

        String command = "player " + name + " kill";
        var dispatcher = server.getCommandManager().getDispatcher();
        try {
            dispatcher.execute(command, source);
        } catch (Exception e) {
            // Ignore
        }
        
        return wasInList;
    }

    public static ServerPlayerEntity getBot(MinecraftServer server, String name) {
        return server.getPlayerManager().getPlayer(name);
    }

    public static void removeAllBots(MinecraftServer server, ServerCommandSource source) {
        var dispatcher = server.getCommandManager().getDispatcher();
        for (String name : new HashSet<>(bots)) {
            // Очищаем все состояния бота
            BotCombat.removeState(name);
            BotUtils.removeState(name);
            BotNavigation.resetIdle(name);
            
            String command = "player " + name + " kill";
            try {
                dispatcher.execute(command, source);
            } catch (Exception e) {
                // Ignore
            }
        }
        bots.clear();
        saveBots();
    }

    public static int getBotCount() {
        return bots.size();
    }

    public static Set<String> getAllBots() {
        return new HashSet<>(bots);
    }
    
    /**
     * Проверяет жив ли бот - НЕ удаляет из списка при смерти
     * Мёртвые боты будут респавнены при рестарте
     */
    public static void cleanupDeadBots(MinecraftServer server) {
        // Только очищаем состояния для мёртвых ботов, но НЕ удаляем из списка
        for (String name : new HashSet<>(bots)) {
            ServerPlayerEntity bot = server.getPlayerManager().getPlayer(name);
            if (bot == null || !bot.isAlive()) {
                // Очищаем состояния но оставляем в списке
                BotCombat.removeState(name);
                BotUtils.removeState(name);
                BotNavigation.resetIdle(name);
            }
        }
    }
}
