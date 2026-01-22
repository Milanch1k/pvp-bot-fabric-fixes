package org.stepan1411.pvp_bot.gui;

import eu.pb4.sgui.api.elements.GuiElementBuilder;
import eu.pb4.sgui.api.gui.SimpleGui;
import net.minecraft.item.Items;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.stepan1411.pvp_bot.bot.BotFaction;
import org.stepan1411.pvp_bot.bot.BotKits;
import org.stepan1411.pvp_bot.bot.BotManager;

/**
 * Главное меню бота с навигацией
 */
public class BotMenuGui {
    
    /**
     * Главное меню
     */
    public static void openMainMenu(ServerPlayerEntity player) {
        SimpleGui gui = new SimpleGui(ScreenHandlerType.GENERIC_3X3, player, false);
        gui.setTitle(Text.literal("PVP Bot Menu"));
        
        MinecraftServer server = player.getCommandSource().getServer();
        
        // Слот 0: Настройки
        gui.setSlot(0, new GuiElementBuilder()
            .setItem(Items.WRITABLE_BOOK)
            .setName(Text.literal("Settings").formatted(Formatting.GREEN))
            .addLoreLine(Text.literal("Global bot settings").formatted(Formatting.GRAY))
            .setCallback((index, type, action) -> {
                gui.close();
                SettingsGui settingsGui = new SettingsGui(player);
                settingsGui.open();
            })
        );
        
        // Слот 1: Список ботов
        gui.setSlot(1, new GuiElementBuilder()
            .setItem(Items.PLAYER_HEAD)
            .setName(Text.literal("Bot List").formatted(Formatting.AQUA))
            .addLoreLine(Text.literal("Manage bots").formatted(Formatting.GRAY))
            .addLoreLine(Text.literal("Bots: " + BotManager.getBotCount()).formatted(Formatting.YELLOW))
            .setCallback((index, type, action) -> {
                gui.close();
                openBotListMenu(player, server);
            })
        );
        
        // Слот 2: Фракции
        gui.setSlot(2, new GuiElementBuilder()
            .setItem(Items.WHITE_BANNER)
            .setName(Text.literal("Factions").formatted(Formatting.LIGHT_PURPLE))
            .addLoreLine(Text.literal("Manage factions").formatted(Formatting.GRAY))
            .addLoreLine(Text.literal("Factions: " + BotFaction.getAllFactions().size()).formatted(Formatting.YELLOW))
            .setCallback((index, type, action) -> {
                gui.close();
                openFactionsMenu(player, server);
            })
        );
        
        // Слот 3: Киты
        gui.setSlot(3, new GuiElementBuilder()
            .setItem(Items.CHEST)
            .setName(Text.literal("Kits").formatted(Formatting.GOLD))
            .addLoreLine(Text.literal("Manage kits").formatted(Formatting.GRAY))
            .addLoreLine(Text.literal("Kits: " + BotKits.getKitNames().size()).formatted(Formatting.YELLOW))
            .setCallback((index, type, action) -> {
                player.sendMessage(Text.literal("Kits: " + String.join(", ", BotKits.getKitNames())), false);
            })
        );
        
        // Слот 8: Закрыть
        gui.setSlot(8, new GuiElementBuilder()
            .setItem(Items.BARRIER)
            .setName(Text.literal("Close").formatted(Formatting.RED))
            .setCallback((index, type, action) -> gui.close())
        );
        
        gui.open();
    }
    
    public static void openBotListMenu(ServerPlayerEntity player, MinecraftServer server) {
        var bots = BotManager.getAllBots();
        
        if (bots.isEmpty()) {
            player.sendMessage(Text.literal("No bots spawned!").formatted(Formatting.RED), false);
            openMainMenu(player);
            return;
        }
        
        SimpleGui gui = new SimpleGui(ScreenHandlerType.GENERIC_9X6, player, false);
        gui.setTitle(Text.literal("Bot List (" + bots.size() + ")"));
        
        int slot = 0;
        for (String botName : bots) {
            if (slot >= 45) break;
            
            String faction = BotFaction.getFaction(botName);
            
            gui.setSlot(slot++, new GuiElementBuilder()
                .setItem(Items.PLAYER_HEAD)
                .setName(Text.literal(botName).formatted(Formatting.YELLOW))
                .addLoreLine(Text.literal("Faction: " + (faction != null ? faction : "None")).formatted(Formatting.GRAY))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("Click to manage").formatted(Formatting.GREEN))
                .setCallback((index, type, action) -> {
                    gui.close();
                    openBotManageMenu(player, server, botName);
                })
            );
        }
        
        gui.setSlot(45, new GuiElementBuilder()
            .setItem(Items.ARROW)
            .setName(Text.literal("Back").formatted(Formatting.YELLOW))
            .setCallback((index, type, action) -> {
                gui.close();
                openMainMenu(player);
            })
        );
        
        gui.open();
    }
    
    public static void openBotManageMenu(ServerPlayerEntity player, MinecraftServer server, String botName) {
        var bot = BotManager.getBot(server, botName);
        if (bot == null) {
            player.sendMessage(Text.literal("Bot not found!").formatted(Formatting.RED), false);
            openBotListMenu(player, server);
            return;
        }
        
        SimpleGui gui = new SimpleGui(ScreenHandlerType.GENERIC_9X3, player, false);
        gui.setTitle(Text.literal("Manage: " + botName));
        
        String faction = BotFaction.getFaction(botName);
        
        gui.setSlot(0, new GuiElementBuilder()
            .setItem(Items.WHITE_BANNER)
            .setName(Text.literal("Faction").formatted(Formatting.LIGHT_PURPLE))
            .addLoreLine(Text.literal("Current: " + (faction != null ? faction : "None")).formatted(Formatting.GRAY))
            .addLoreLine(Text.literal(""))
            .addLoreLine(Text.literal("Click to change").formatted(Formatting.YELLOW))
            .setCallback((index, type, action) -> {
                gui.close();
                openBotFactionSelectMenu(player, server, botName);
            })
        );
        
        gui.setSlot(1, new GuiElementBuilder()
            .setItem(Items.GOLDEN_APPLE)
            .setName(Text.literal("Heal").formatted(Formatting.GREEN))
            .addLoreLine(Text.literal("HP: " + String.format("%.1f", bot.getHealth()) + "/" + String.format("%.1f", bot.getMaxHealth())).formatted(Formatting.GRAY))
            .addLoreLine(Text.literal(""))
            .addLoreLine(Text.literal("Click to heal").formatted(Formatting.YELLOW))
            .setCallback((index, type, action) -> {
                bot.setHealth(bot.getMaxHealth());
                player.sendMessage(Text.literal("Healed " + botName + " to full HP!").formatted(Formatting.GREEN), false);
                gui.close();
                openBotManageMenu(player, server, botName);
            })
        );
        
        gui.setSlot(2, new GuiElementBuilder()
            .setItem(Items.CHEST)
            .setName(Text.literal("Inventory").formatted(Formatting.GOLD))
            .addLoreLine(Text.literal("View bot inventory").formatted(Formatting.GRAY))
            .setCallback((index, type, action) -> {
                gui.close();
                try {
                    openInvViewGui(player, bot);
                } catch (Exception e) {
                    player.sendMessage(Text.literal("InvView not available!").formatted(Formatting.RED), false);
                    openBotManageMenu(player, server, botName);
                }
            })
        );
        
        gui.setSlot(3, new GuiElementBuilder()
            .setItem(Items.ENDER_PEARL)
            .setName(Text.literal("Teleport").formatted(Formatting.AQUA))
            .addLoreLine(Text.literal("Teleport to bot").formatted(Formatting.GRAY))
            .setCallback((index, type, action) -> {
                player.requestTeleport(bot.getX(), bot.getY(), bot.getZ());
                player.setYaw(bot.getYaw());
                player.setPitch(bot.getPitch());
                player.sendMessage(Text.literal("Teleported to " + botName).formatted(Formatting.GREEN), false);
                gui.close();
            })
        );
        
        gui.setSlot(4, new GuiElementBuilder()
            .setItem(Items.BARRIER)
            .setName(Text.literal("Remove Bot").formatted(Formatting.RED))
            .addLoreLine(Text.literal("Delete this bot").formatted(Formatting.GRAY))
            .setCallback((index, type, action) -> {
                BotManager.removeBot(server, botName, player.getCommandSource());
                player.sendMessage(Text.literal("Removed bot " + botName).formatted(Formatting.GREEN), false);
                gui.close();
                openBotListMenu(player, server);
            })
        );
        
        gui.setSlot(18, new GuiElementBuilder()
            .setItem(Items.ARROW)
            .setName(Text.literal("Back").formatted(Formatting.YELLOW))
            .setCallback((index, type, action) -> {
                gui.close();
                openBotListMenu(player, server);
            })
        );
        
        gui.open();
    }
    
    public static void openBotFactionSelectMenu(ServerPlayerEntity player, MinecraftServer server, String botName) {
        var factions = BotFaction.getAllFactions();
        
        SimpleGui gui = new SimpleGui(ScreenHandlerType.GENERIC_9X3, player, false);
        gui.setTitle(Text.literal("Select Faction for " + botName));
        
        int slot = 0;
        
        gui.setSlot(slot++, new GuiElementBuilder()
            .setItem(Items.GRAY_BANNER)
            .setName(Text.literal("No Faction").formatted(Formatting.GRAY))
            .addLoreLine(Text.literal("Remove from faction").formatted(Formatting.GRAY))
            .setCallback((index, type, action) -> {
                String currentFaction = BotFaction.getFaction(botName);
                if (currentFaction != null) {
                    BotFaction.removeMember(currentFaction, botName);
                    player.sendMessage(Text.literal("Removed " + botName + " from faction").formatted(Formatting.GREEN), false);
                }
                gui.close();
                openBotManageMenu(player, server, botName);
            })
        );
        
        for (String factionName : factions) {
            if (slot >= 18) break;
            
            var members = BotFaction.getMembers(factionName);
            boolean isMember = members != null && members.contains(botName);
            
            gui.setSlot(slot++, new GuiElementBuilder()
                .setItem(isMember ? Items.LIME_BANNER : Items.WHITE_BANNER)
                .setName(Text.literal(factionName).formatted(isMember ? Formatting.GREEN : Formatting.WHITE))
                .addLoreLine(Text.literal("Members: " + (members != null ? members.size() : 0)).formatted(Formatting.GRAY))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal(isMember ? "Current faction" : "Click to join").formatted(isMember ? Formatting.GREEN : Formatting.YELLOW))
                .setCallback((index, type, action) -> {
                    String currentFaction = BotFaction.getFaction(botName);
                    if (currentFaction != null) {
                        BotFaction.removeMember(currentFaction, botName);
                    }
                    BotFaction.addMember(factionName, botName);
                    player.sendMessage(Text.literal("Added " + botName + " to faction " + factionName).formatted(Formatting.GREEN), false);
                    gui.close();
                    openBotManageMenu(player, server, botName);
                })
            );
        }
        
        gui.setSlot(18, new GuiElementBuilder()
            .setItem(Items.ARROW)
            .setName(Text.literal("Back").formatted(Formatting.YELLOW))
            .setCallback((index, type, action) -> {
                gui.close();
                openBotManageMenu(player, server, botName);
            })
        );
        
        gui.open();
    }
    
    public static void openFactionsMenu(ServerPlayerEntity player, MinecraftServer server) {
        var factions = BotFaction.getAllFactions();
        
        SimpleGui gui = new SimpleGui(ScreenHandlerType.GENERIC_9X6, player, false);
        gui.setTitle(Text.literal("Factions (" + factions.size() + ")"));
        
        int slot = 0;
        for (String factionName : factions) {
            if (slot >= 45) break;
            
            var members = BotFaction.getMembers(factionName);
            var enemies = BotFaction.getHostileFactions(factionName);
            
            gui.setSlot(slot++, new GuiElementBuilder()
                .setItem(Items.WHITE_BANNER)
                .setName(Text.literal(factionName).formatted(Formatting.LIGHT_PURPLE))
                .addLoreLine(Text.literal("Members: " + (members != null ? members.size() : 0)).formatted(Formatting.GRAY))
                .addLoreLine(Text.literal("Enemies: " + (enemies != null ? enemies.size() : 0)).formatted(Formatting.RED))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("Click to manage").formatted(Formatting.YELLOW))
                .setCallback((index, type, action) -> {
                    gui.close();
                    openFactionManageMenu(player, server, factionName);
                })
            );
        }
        
        gui.setSlot(45, new GuiElementBuilder()
            .setItem(Items.WRITABLE_BOOK)
            .setName(Text.literal("Create Faction").formatted(Formatting.GREEN))
            .addLoreLine(Text.literal("Use /pvpbot faction create <name>").formatted(Formatting.GRAY))
            .setCallback((index, type, action) -> {
                player.sendMessage(Text.literal("Use: /pvpbot faction create <name>").formatted(Formatting.YELLOW), false);
            })
        );
        
        gui.setSlot(53, new GuiElementBuilder()
            .setItem(Items.ARROW)
            .setName(Text.literal("Back").formatted(Formatting.YELLOW))
            .setCallback((index, type, action) -> {
                gui.close();
                openMainMenu(player);
            })
        );
        
        gui.open();
    }
    
    public static void openFactionManageMenu(ServerPlayerEntity player, MinecraftServer server, String factionName) {
        if (!BotFaction.getAllFactions().contains(factionName)) {
            player.sendMessage(Text.literal("Faction not found!").formatted(Formatting.RED), false);
            openFactionsMenu(player, server);
            return;
        }
        
        SimpleGui gui = new SimpleGui(ScreenHandlerType.GENERIC_9X3, player, false);
        gui.setTitle(Text.literal("Faction: " + factionName));
        
        var members = BotFaction.getMembers(factionName);
        var enemies = BotFaction.getHostileFactions(factionName);
        
        gui.setSlot(0, new GuiElementBuilder()
            .setItem(Items.PLAYER_HEAD)
            .setName(Text.literal("Members").formatted(Formatting.AQUA))
            .addLoreLine(Text.literal("Count: " + (members != null ? members.size() : 0)).formatted(Formatting.GRAY))
            .addLoreLine(Text.literal(""))
            .addLoreLine(Text.literal("Click to view").formatted(Formatting.YELLOW))
            .setCallback((index, type, action) -> {
                if (members != null && !members.isEmpty()) {
                    player.sendMessage(Text.literal("Members: " + String.join(", ", members)).formatted(Formatting.AQUA), false);
                } else {
                    player.sendMessage(Text.literal("No members").formatted(Formatting.GRAY), false);
                }
            })
        );
        
        gui.setSlot(1, new GuiElementBuilder()
            .setItem(Items.IRON_SWORD)
            .setName(Text.literal("Enemies").formatted(Formatting.RED))
            .addLoreLine(Text.literal("Count: " + (enemies != null ? enemies.size() : 0)).formatted(Formatting.GRAY))
            .addLoreLine(Text.literal(""))
            .addLoreLine(Text.literal("Click to view").formatted(Formatting.YELLOW))
            .setCallback((index, type, action) -> {
                if (enemies != null && !enemies.isEmpty()) {
                    player.sendMessage(Text.literal("Enemies: " + String.join(", ", enemies)).formatted(Formatting.RED), false);
                } else {
                    player.sendMessage(Text.literal("No enemies").formatted(Formatting.GRAY), false);
                }
            })
        );
        
        gui.setSlot(2, new GuiElementBuilder()
            .setItem(Items.GOLDEN_APPLE)
            .setName(Text.literal("Heal All").formatted(Formatting.GREEN))
            .addLoreLine(Text.literal("Heal all faction members").formatted(Formatting.GRAY))
            .setCallback((index, type, action) -> {
                if (members != null) {
                    int healed = 0;
                    for (String memberName : members) {
                        if (BotManager.getAllBots().contains(memberName)) {
                            var bot = BotManager.getBot(server, memberName);
                            if (bot != null) {
                                bot.setHealth(bot.getMaxHealth());
                                healed++;
                            }
                        }
                    }
                    player.sendMessage(Text.literal("Healed " + healed + " bots in faction " + factionName).formatted(Formatting.GREEN), false);
                }
            })
        );
        
        gui.setSlot(3, new GuiElementBuilder()
            .setItem(Items.BARRIER)
            .setName(Text.literal("Delete Faction").formatted(Formatting.RED))
            .addLoreLine(Text.literal("Permanently delete").formatted(Formatting.GRAY))
            .setCallback((index, type, action) -> {
                BotFaction.deleteFaction(factionName);
                player.sendMessage(Text.literal("Deleted faction " + factionName).formatted(Formatting.GREEN), false);
                gui.close();
                openFactionsMenu(player, server);
            })
        );
        
        gui.setSlot(18, new GuiElementBuilder()
            .setItem(Items.ARROW)
            .setName(Text.literal("Back").formatted(Formatting.YELLOW))
            .setCallback((index, type, action) -> {
                gui.close();
                openFactionsMenu(player, server);
            })
        );
        
        gui.open();
    }
    
    private static void openInvViewGui(ServerPlayerEntity viewer, ServerPlayerEntity target) throws Exception {
        Class<?> guiClass = Class.forName("us.potatoboy.invview.gui.SavingPlayerDataGui");
        var constructor = guiClass.getConstructor(ServerPlayerEntity.class, ServerPlayerEntity.class);
        var gui = constructor.newInstance(viewer, target);
        var openMethod = guiClass.getMethod("open");
        openMethod.invoke(gui);
    }
}
