package org.stepan1411.pvp_bot.bot;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;

public class BotTicker {

    private static int tickCounter = 0;
    private static int autoSaveCounter = 0;
    private static final int AUTO_SAVE_INTERVAL = 1200; // Автосохранение каждые 60 секунд (1200 тиков)

    public static void register() {
        ServerTickEvents.END_SERVER_TICK.register(BotTicker::onServerTick);
    }

    private static void onServerTick(MinecraftServer server) {
        tickCounter++;
        autoSaveCounter++;
        
        int interval = BotSettings.get().getCheckInterval();
        
        // Автосохранение данных ботов каждые 60 секунд
        if (autoSaveCounter >= AUTO_SAVE_INTERVAL) {
            BotManager.updateBotData(server);
            BotManager.saveBots();
            autoSaveCounter = 0;
        }
        
        // Очищаем мёртвых ботов каждые 20 тиков (1 секунда)
        if (tickCounter % 20 == 0) {
            BotManager.cleanupDeadBots(server);
            // Синхронизируем список ботов с сервером
            BotManager.syncBots(server);
        }
        
        for (String botName : BotManager.getAllBots()) {
            ServerPlayerEntity bot = BotManager.getBot(server, botName);
            if (bot != null && bot.isAlive()) {
                // Утилиты (тотем, еда, щит, плавание) - каждый тик
                BotUtils.update(bot, server);
                
                // Боевая система - каждый тик
                BotCombat.update(bot, server);
                
                // Экипировка - по интервалу (не во время еды!)
                if (tickCounter >= interval) {
                    var utilsState = BotUtils.getState(botName);
                    if (!utilsState.isEating) {
                        BotEquipment.autoEquip(bot);
                    }
                }
            }
        }
        
        if (tickCounter >= interval) {
            tickCounter = 0;
        }
    }
}
