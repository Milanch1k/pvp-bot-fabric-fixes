package org.stepan1411.pvp_bot.bot;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;

public class BotSettings {

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static BotSettings INSTANCE;
    private static Path configPath;
    
    // Настройки экипировки
    private boolean autoEquipArmor = true;
    private boolean autoEquipWeapon = true;
    
    // Настройки выбрасывания
    private boolean dropWorseArmor = false;
    private boolean dropWorseWeapons = false;
    private double dropDistance = 3.0;
    private int dropDelay = 20;
    
    // Интервал проверки
    private int checkInterval = 20;
    
    // Минимальный уровень брони
    private int minArmorLevel = 0;
    
    // ============ Настройки боя ============
    private boolean combatEnabled = true;
    private boolean revengeEnabled = true;        // Атаковать того кто атаковал бота
    private boolean autoTargetEnabled = false;    // Автоматически искать врагов
    private boolean targetPlayers = true;         // Атаковать игроков
    private boolean targetHostileMobs = false;    // Атаковать враждебных мобов
    private boolean targetOtherBots = false;      // Атаковать других ботов
    
    // Дистанции
    private double maxTargetDistance = 64.0;      // Максимальная дистанция поиска цели (увеличено)
    private double meleeRange = 3.5;              // Дистанция ближнего боя
    private double rangedMinRange = 8.0;          // Минимальная дистанция для лука
    private double rangedOptimalRange = 20.0;     // Оптимальная дистанция для лука
    private double maceRange = 6.0;               // Дистанция для булавы
    
    // Параметры боя
    private int attackCooldown = 10;              // Кулдаун атаки в тиках
    private double moveSpeed = 1.0;               // Скорость движения
    private boolean criticalsEnabled = true;      // Критические удары
    private int bowMinDrawTime = 15;              // Минимальное время натяжения лука
    
    // Включение типов оружия
    private boolean rangedEnabled = true;         // Использовать лук/арбалет
    private boolean maceEnabled = true;           // Использовать булаву
    
    // ============ Утилиты ============
    private boolean autoTotemEnabled = true;      // Авто-тотем в offhand
    private boolean autoEatEnabled = true;        // Авто-еда
    private boolean autoShieldEnabled = true;     // Авто-щит
    private boolean shieldBreakEnabled = true;    // Сбивать щит топором
    private int minHungerToEat = 14;              // Минимальный голод для еды
    
    // ============ Фракции и ошибки ============
    private boolean factionsEnabled = true;       // Использовать систему фракций
    private int missChance = 10;                  // Шанс промаха (0-100%)
    private int mistakeChance = 5;                // Шанс ошибки (0-100%)
    private int reactionDelay = 0;                // Задержка реакции в тиках (0-20)
    
    private BotSettings() {}
    
    public static BotSettings get() {
        if (INSTANCE == null) {
            load();
        }
        return INSTANCE;
    }
    
    public static void load() {
        configPath = FabricLoader.getInstance().getConfigDir().resolve("pvp_bot.json");
        
        if (Files.exists(configPath)) {
            try (Reader reader = Files.newBufferedReader(configPath)) {
                INSTANCE = GSON.fromJson(reader, BotSettings.class);
                if (INSTANCE == null) {
                    INSTANCE = new BotSettings();
                }
            } catch (Exception e) {
                INSTANCE = new BotSettings();
            }
        } else {
            INSTANCE = new BotSettings();
            save();
        }
    }

    
    public static void save() {
        if (INSTANCE == null || configPath == null) return;
        
        try (Writer writer = Files.newBufferedWriter(configPath)) {
            GSON.toJson(INSTANCE, writer);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    // Getters - Equipment
    public boolean isAutoEquipArmor() { return autoEquipArmor; }
    public boolean isAutoEquipWeapon() { return autoEquipWeapon; }
    public boolean isDropWorseArmor() { return dropWorseArmor; }
    public boolean isDropWorseWeapons() { return dropWorseWeapons; }
    public double getDropDistance() { return dropDistance; }
    public int getDropDelay() { return dropDelay; }
    public int getCheckInterval() { return checkInterval; }
    public int getMinArmorLevel() { return minArmorLevel; }
    
    // Getters - Combat
    public boolean isCombatEnabled() { return combatEnabled; }
    public boolean isRevengeEnabled() { return revengeEnabled; }
    public boolean isAutoTargetEnabled() { return autoTargetEnabled; }
    public boolean isTargetPlayers() { return targetPlayers; }
    public boolean isTargetHostileMobs() { return targetHostileMobs; }
    public boolean isTargetOtherBots() { return targetOtherBots; }
    public double getMaxTargetDistance() { return maxTargetDistance; }
    public double getMeleeRange() { return meleeRange; }
    public double getRangedMinRange() { return rangedMinRange; }
    public double getRangedOptimalRange() { return rangedOptimalRange; }
    public double getMaceRange() { return maceRange; }
    public int getAttackCooldown() { return attackCooldown; }
    public double getMoveSpeed() { return moveSpeed; }
    public boolean isCriticalsEnabled() { return criticalsEnabled; }
    public int getBowMinDrawTime() { return bowMinDrawTime; }
    public boolean isRangedEnabled() { return rangedEnabled; }
    public boolean isMaceEnabled() { return maceEnabled; }
    
    // Getters - Utils
    public boolean isAutoTotemEnabled() { return autoTotemEnabled; }
    public boolean isAutoEatEnabled() { return autoEatEnabled; }
    public boolean isAutoShieldEnabled() { return autoShieldEnabled; }
    public boolean isShieldBreakEnabled() { return shieldBreakEnabled; }
    public int getMinHungerToEat() { return minHungerToEat; }
    
    // Getters - Factions & Mistakes
    public boolean isFactionsEnabled() { return factionsEnabled; }
    public int getMissChance() { return missChance; }
    public int getMistakeChance() { return mistakeChance; }
    public int getReactionDelay() { return reactionDelay; }
    
    // Setters (с автосохранением)
    public void setAutoEquipArmor(boolean value) { 
        this.autoEquipArmor = value; 
        save();
    }
    public void setAutoEquipWeapon(boolean value) { 
        this.autoEquipWeapon = value; 
        save();
    }
    public void setDropWorseArmor(boolean value) { 
        this.dropWorseArmor = value; 
        save();
    }
    public void setDropWorseWeapons(boolean value) { 
        this.dropWorseWeapons = value; 
        save();
    }
    public void setDropDistance(double value) { 
        this.dropDistance = Math.max(1.0, Math.min(10.0, value)); 
        save();
    }
    public void setDropDelay(int value) { 
        this.dropDelay = Math.max(1, Math.min(200, value)); 
        save();
    }
    public void setCheckInterval(int value) { 
        this.checkInterval = Math.max(1, Math.min(100, value)); 
        save();
    }
    public void setMinArmorLevel(int value) { 
        this.minArmorLevel = Math.max(0, Math.min(100, value)); 
        save();
    }
    
    // Setters - Combat
    public void setCombatEnabled(boolean value) { this.combatEnabled = value; save(); }
    public void setRevengeEnabled(boolean value) { this.revengeEnabled = value; save(); }
    public void setAutoTargetEnabled(boolean value) { this.autoTargetEnabled = value; save(); }
    public void setTargetPlayers(boolean value) { this.targetPlayers = value; save(); }
    public void setTargetHostileMobs(boolean value) { this.targetHostileMobs = value; save(); }
    public void setTargetOtherBots(boolean value) { this.targetOtherBots = value; save(); }
    
    public void setMaxTargetDistance(double value) { 
        this.maxTargetDistance = Math.max(5.0, Math.min(128.0, value)); 
        save(); 
    }
    public void setMeleeRange(double value) { 
        this.meleeRange = Math.max(2.0, Math.min(6.0, value)); 
        save(); 
    }
    public void setRangedMinRange(double value) { 
        this.rangedMinRange = Math.max(3.0, Math.min(20.0, value)); 
        save(); 
    }
    public void setRangedOptimalRange(double value) { 
        this.rangedOptimalRange = Math.max(10.0, Math.min(50.0, value)); 
        save(); 
    }
    public void setMaceRange(double value) { 
        this.maceRange = Math.max(3.0, Math.min(10.0, value)); 
        save(); 
    }
    public void setAttackCooldown(int value) { 
        this.attackCooldown = Math.max(1, Math.min(40, value)); 
        save(); 
    }
    public void setMoveSpeed(double value) { 
        this.moveSpeed = Math.max(0.1, Math.min(2.0, value)); 
        save(); 
    }
    public void setCriticalsEnabled(boolean value) { this.criticalsEnabled = value; save(); }
    public void setBowMinDrawTime(int value) { 
        this.bowMinDrawTime = Math.max(5, Math.min(30, value)); 
        save(); 
    }
    public void setRangedEnabled(boolean value) { this.rangedEnabled = value; save(); }
    public void setMaceEnabled(boolean value) { this.maceEnabled = value; save(); }
    
    // Setters - Utils
    public void setAutoTotemEnabled(boolean value) { this.autoTotemEnabled = value; save(); }
    public void setAutoEatEnabled(boolean value) { this.autoEatEnabled = value; save(); }
    public void setAutoShieldEnabled(boolean value) { this.autoShieldEnabled = value; save(); }
    public void setShieldBreakEnabled(boolean value) { this.shieldBreakEnabled = value; save(); }
    public void setMinHungerToEat(int value) { 
        this.minHungerToEat = Math.max(1, Math.min(20, value)); 
        save(); 
    }
    
    // Setters - Factions & Mistakes
    public void setFactionsEnabled(boolean value) { this.factionsEnabled = value; save(); }
    public void setMissChance(int value) { 
        this.missChance = Math.max(0, Math.min(100, value)); 
        save(); 
    }
    public void setMistakeChance(int value) { 
        this.mistakeChance = Math.max(0, Math.min(100, value)); 
        save(); 
    }
    public void setReactionDelay(int value) { 
        this.reactionDelay = Math.max(0, Math.min(20, value)); 
        save(); 
    }
}
