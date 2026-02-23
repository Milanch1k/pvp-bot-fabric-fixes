package org.stepan1411.pvp_bot.gui;

import eu.pb4.sgui.api.elements.GuiElementBuilder;
import eu.pb4.sgui.api.gui.SimpleGui;
import net.minecraft.item.Items;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.stepan1411.pvp_bot.bot.BotSettings;

import java.util.ArrayList;
import java.util.List;

public class SettingsGui extends SimpleGui {
    
    private int currentPage = 0;
    private final List<SettingEntry> allSettings = new ArrayList<>();
    private static final int SETTINGS_PER_PAGE = 45; // 5 рядов по 9 слотов
    
    public SettingsGui(ServerPlayerEntity player) {
        super(ScreenHandlerType.GENERIC_9X6, player, false);
        this.setTitle(Text.literal("PVP Bot Settings"));
        
        initializeSettings();
        updatePage();
    }
    
    private void initializeSettings() {
        BotSettings s = BotSettings.get();
        
        // === Equipment Settings ===
        allSettings.add(new SettingEntry(
            "Auto Armor",
            "Automatically equip best armor",
            s::isAutoEquipArmor,
            s::setAutoEquipArmor
        ));
        
        allSettings.add(new SettingEntry(
            "Auto Weapon",
            "Automatically equip best weapon",
            s::isAutoEquipWeapon,
            s::setAutoEquipWeapon
        ));
        
        allSettings.add(new SettingEntry(
            "Auto Totem",
            "Auto-equip totem in offhand",
            s::isAutoTotemEnabled,
            s::setAutoTotemEnabled
        ));
        
        allSettings.add(new SettingEntry(
            "Auto Shield",
            "Auto-use shield when blocking",
            s::isAutoShieldEnabled,
            s::setAutoShieldEnabled
        ));
        
        allSettings.add(new SettingEntry(
            "Shield Break",
            "Switch to axe to break enemy shield",
            s::isShieldBreakEnabled,
            s::setShieldBreakEnabled
        ));
        
        allSettings.add(new SettingEntry(
            "Prefer Sword",
            "Prefer sword over axe",
            s::isPreferSword,
            s::setPreferSword
        ));
        
        allSettings.add(new SettingEntry(
            "Drop Worse Armor",
            "Drop worse armor pieces",
            s::isDropWorseArmor,
            s::setDropWorseArmor
        ));
        
        allSettings.add(new SettingEntry(
            "Drop Worse Weapons",
            "Drop worse weapons",
            s::isDropWorseWeapons,
            s::setDropWorseWeapons
        ));
        
        // === Combat Settings ===
        allSettings.add(new SettingEntry(
            "Combat",
            "Enable/disable combat system",
            s::isCombatEnabled,
            s::setCombatEnabled
        ));
        
        allSettings.add(new SettingEntry(
            "Revenge",
            "Attack entities that damage the bot",
            s::isRevengeEnabled,
            s::setRevengeEnabled
        ));
        
        allSettings.add(new SettingEntry(
            "Auto Target",
            "Automatically search for enemies",
            s::isAutoTargetEnabled,
            s::setAutoTargetEnabled
        ));
        
        allSettings.add(new SettingEntry(
            "Target Players",
            "Can target players",
            s::isTargetPlayers,
            s::setTargetPlayers
        ));
        
        allSettings.add(new SettingEntry(
            "Target Mobs",
            "Can target hostile mobs",
            s::isTargetHostileMobs,
            s::setTargetHostileMobs
        ));
        
        allSettings.add(new SettingEntry(
            "Target Bots",
            "Can target other bots",
            s::isTargetOtherBots,
            s::setTargetOtherBots
        ));
        
        allSettings.add(new SettingEntry(
            "Criticals",
            "Perform critical hits",
            s::isCriticalsEnabled,
            s::setCriticalsEnabled
        ));
        
        allSettings.add(new SettingEntry(
            "Ranged",
            "Use bows/crossbows",
            s::isRangedEnabled,
            s::setRangedEnabled
        ));
        
        allSettings.add(new SettingEntry(
            "Mace",
            "Use mace with wind charges",
            s::isMaceEnabled,
            s::setMaceEnabled
        ));
        
        // === Utilities ===
        allSettings.add(new SettingEntry(
            "Auto Potion",
            "Auto-use healing/buff potions",
            s::isAutoPotionEnabled,
            s::setAutoPotionEnabled
        ));
        
        allSettings.add(new SettingEntry(
            "Auto Eat",
            "Auto-eat when hungry",
            s::isAutoEatEnabled,
            s::setAutoEatEnabled
        ));
        
        allSettings.add(new SettingEntry(
            "Cobweb",
            "Use cobwebs to slow enemies",
            s::isCobwebEnabled,
            s::setCobwebEnabled
        ));

        allSettings.add(new SettingEntry(
                "Cobweb escape",
                "Escape from cobweb",
                s::isEscapeFromCobweb,
                s::setEscapeFromCobweb
        ));
        
        // === Navigation ===
        allSettings.add(new SettingEntry(
            "Retreat",
            "Retreat when low HP",
            s::isRetreatEnabled,
            s::setRetreatEnabled
        ));
        
        allSettings.add(new SettingEntry(
            "Bunny Hop",
            "Jump while running",
            s::isBhopEnabled,
            s::setBhopEnabled
        ));
        
        allSettings.add(new SettingEntry(
            "Idle Wander",
            "Wander when no target",
            s::isIdleWanderEnabled,
            s::setIdleWanderEnabled
        ));
        
        // === Factions ===
        allSettings.add(new SettingEntry(
            "Factions",
            "Enable faction system",
            s::isFactionsEnabled,
            s::setFactionsEnabled
        ));
        
        allSettings.add(new SettingEntry(
            "Friendly Fire",
            "Allow damage to allies",
            s::isFriendlyFireEnabled,
            s::setFriendlyFireEnabled
        ));
    }
    
    private void updatePage() {
        // Очищаем GUI
        for (int i = 0; i < this.getSize(); i++) {
            this.clearSlot(i);
        }
        
        // Вычисляем диапазон настроек для текущей страницы
        int startIndex = currentPage * SETTINGS_PER_PAGE;
        int endIndex = Math.min(startIndex + SETTINGS_PER_PAGE, allSettings.size());
        
        // Добавляем настройки
        for (int i = startIndex; i < endIndex; i++) {
            SettingEntry setting = allSettings.get(i);
            int slot = i - startIndex;
            
            boolean currentValue = setting.getter.get();
            
            GuiElementBuilder element = new GuiElementBuilder()
                .setItem(currentValue ? Items.GREEN_CONCRETE : Items.RED_CONCRETE)
                .setName(Text.literal(setting.name).formatted(currentValue ? Formatting.GREEN : Formatting.RED))
                .addLoreLine(Text.literal(setting.description).formatted(Formatting.GRAY))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("Status: " + (currentValue ? "Enabled" : "Disabled"))
                    .formatted(currentValue ? Formatting.GREEN : Formatting.RED))
                .addLoreLine(Text.literal("Click to toggle").formatted(Formatting.YELLOW))
                .setCallback((index, type, action) -> {
                    setting.setter.accept(!currentValue);
                    updatePage();
                });
            
            this.setSlot(slot, element);
        }
        
        // Добавляем навигацию внизу (последний ряд)
        int totalPages = (int) Math.ceil((double) allSettings.size() / SETTINGS_PER_PAGE);
        
        // Предыдущая страница (слот 45)
        if (currentPage > 0) {
            this.setSlot(45, new GuiElementBuilder()
                .setItem(Items.SPECTRAL_ARROW)
                .setName(Text.literal("Previous Page").formatted(Formatting.YELLOW))
                .addLoreLine(Text.literal("Page " + currentPage + "/" + totalPages).formatted(Formatting.GRAY))
                .setCallback((index, type, action) -> {
                    currentPage--;
                    updatePage();
                })
            );
        }
        
        // Информация о странице (слот 49 - центр)
        this.setSlot(49, new GuiElementBuilder()
            .setItem(Items.BOOK)
            .setName(Text.literal("Page " + (currentPage + 1) + "/" + totalPages).formatted(Formatting.GOLD))
            .addLoreLine(Text.literal("Settings: " + allSettings.size()).formatted(Formatting.GRAY))
        );
        
        // Следующая страница (слот 53)
        if (currentPage < totalPages - 1) {
            this.setSlot(53, new GuiElementBuilder()
                .setItem(Items.SPECTRAL_ARROW)
                .setName(Text.literal("Next Page").formatted(Formatting.YELLOW))
                .addLoreLine(Text.literal("Page " + (currentPage + 2) + "/" + totalPages).formatted(Formatting.GRAY))
                .setCallback((index, type, action) -> {
                    currentPage++;
                    updatePage();
                })
            );
        }
        
        // Кнопка назад в главное меню (слот 46)
        this.setSlot(46, new GuiElementBuilder()
            .setItem(Items.ARROW)
            .setName(Text.literal("Back to Menu").formatted(Formatting.YELLOW))
            .setCallback((index, type, action) -> {
                this.close();
                BotMenuGui.openMainMenu(this.player);
            })
        );
    }
    
    private static class SettingEntry {
        final String name;
        final String description;
        final BooleanGetter getter;
        final BooleanSetter setter;
        
        SettingEntry(String name, String description, BooleanGetter getter, BooleanSetter setter) {
            this.name = name;
            this.description = description;
            this.getter = getter;
            this.setter = setter;
        }
    }
    
    @FunctionalInterface
    interface BooleanGetter {
        boolean get();
    }
    
    @FunctionalInterface
    interface BooleanSetter {
        void accept(boolean value);
    }
}
