package org.stepan1411.pvp_bot.bot;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;

public class BotTicker {

    private static int tickCounter = 0;

    public static void register() {
        ServerTickEvents.END_SERVER_TICK.register(BotTicker::onServerTick);
    }

    private static void onServerTick(MinecraftServer server) {
        tickCounter++;
        
        int interval = BotSettings.get().getCheckInterval();
        
        // Очищаем мёртвых ботов каждые 100 тиков (5 секунд)
        if (tickCounter % 100 == 0) {
            BotManager.cleanupDeadBots(server);
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
