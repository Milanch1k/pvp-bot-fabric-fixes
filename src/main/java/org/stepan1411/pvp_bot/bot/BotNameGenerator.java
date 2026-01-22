package org.stepan1411.pvp_bot.bot;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Генератор уникальных имён для ботов
 * Разбивает имена типа "AetherClaw" на части и комбинирует их
 */
public class BotNameGenerator {
    
    private static final List<String> PREFIXES = new ArrayList<>();
    private static final List<String> SUFFIXES = new ArrayList<>();
    private static final Random random = new Random();
    
    // URL для загрузки особых ников (GitHub Pages)
    private static final String SPECIAL_NAMES_URL = "https://stepan1411.github.io/pvp-bot-fabric/special_names.json";
    
    // Особые ники (загружаются из JSON)
    private static List<String> specialNames = new ArrayList<>();
    private static int spawnChance = 10; // По умолчанию 10%
    private static boolean specialNamesLoaded = false;
    private static long lastLoadTime = 0; // Время последней загрузки
    private static final long RELOAD_INTERVAL = 60000; // 1 минута в миллисекундах
    
    // Паттерн для разделения CamelCase (AetherClaw -> Aether, Claw)
    private static final Pattern CAMEL_CASE = Pattern.compile("([A-Z][a-z0-9]+)");
    
    // Базовые имена для разбора
    private static final String[] BASE_NAMES = {
        "AetherClaw", "BlazeRunner", "NetherScribe", "VoidCarver", "StoneVigil",
        "HexaStrike", "CinderBlade", "FrostShard", "NightRavager", "WarpedSentinel",
        "CrimsonBolt", "EchoMiner", "ShadowCrafter", "IronSpecter", "BlueTalon",
        "CoreBreaker", "SilentObsidian", "EnderHarvester", "GhastSilencer", "LavaWalker",
        "NullCaster", "RiftHunter", "DeepStrider", "BoneSpark", "PhantomTide",
        "AncientSting", "SolarVortex", "HollowKnight", "PixelNomad", "DustReaver",
        "AmberWarden", "FuryCrafter", "BedrockFang", "ThunderFloe", "DreadCircuit",
        "QuartzStalker", "SkyboundRogue", "FallenCobalt", "ObsidianHornet", "RuneShatter",
        "PrismFire", "VortexHermit", "Ashborne", "Netherling", "SilentCrux",
        "EbonShade", "StormGlider", "IronWanderer", "WarpDiver", "StellarForge",
        "GhostMire", "CobaltBreaker", "BlitzCraze", "ScarletPiercer", "FeralCircuit",
        "DarklingRush", "CoreFrost", "MoltenCode", "SpireWalker", "EtherRune",
        "GlassReaver", "DeltaCrafter", "DeepFang", "MistHowler", "CopperVandal",
        "IronNova", "NightAggregate", "ScaledComet", "VileHarvester", "DustHopper",
        "MarrowRush", "PhantomVerse", "NullShard", "WardenTamer", "PrimalSlicer",
        "Stormborne", "GlintTracer", "FluxReaver", "ShadeWielder", "CliffJumper",
        "AshenSickle", "HauntEdge", "SnareStrike", "CrimsonVoxel", "SlateBreaker",
        "HuskRider", "Echolite", "VoidWalker", "SkyForge", "DarkBlade",
        "ShadowWarden", "IronPhantom", "SkywardDrift", "EmberTalon", "ObsidianReaper",
        "CrystalWisp", "ThunderHawk", "MoltenVeil", "DuskStrider", "AstralForge",
        "GloomWeaver", "StormCaller", "AshenKnight", "FrozenOrb", "EnderHarbinger",
        "QuartzSerpent", "LunarWraith", "InfernoSage", "DeepWarden", "MythicFlame",
        "RuneBreaker", "SilentHowl", "SolarViper", "ChaosMender", "NeonSpectre",
        "BlightFang", "ArcanePulse", "CrimsonDrake", "GalaxyRider", "MossWarden",
        "PhantomEdge", "TwilightSeer", "VolcanicCore", "StarlitNomad", "BoneHarvester",
        "CelestialWolf", "DarkenedSoul", "ElectricMoth", "FrostbiteKing", "GildedRaven",
        "HollowCry", "IcyTempest", "JaggedSpire", "KarmaBlade", "LavaWhisper",
        "MysticHound", "NovaStriker", "OblivionEye", "PlasmaFist", "QuillStorm",
        "RiftWalker", "SableFury", "ToxicVein", "UmbralFlare", "ViperShade",
        "WickedGale", "XenoBlade", "YawningVoid", "ZenithHawk", "AuroraSting",
        "BasiliskGaze", "CursedFlame", "DreadedMaw", "EclipseWing", "FeralEcho",
        "GhastlyGrin", "HollowSpine", "IgnisRook", "JaggedFang", "KrakenTide",
        "LunarFang", "MoltenHeart", "NebulaFist", "OnyxScourge", "PolarClash",
        "QuakeBeast", "RavenousGhost", "SanguineThorn", "TidalWraith", "UmberHowl",
        "VermilionSoul", "WarpTalon", "XyloSpecter", "YmirFrost", "ZephyrBlade",
        "AshbornRogue", "BloodiedCrest", "CipherWing", "DriftSoul", "EmberWing",
        "Frostborne", "GloomRider", "HavocFang", "IroncladEye", "JinxedVeil",
        "KoboldKing", "Lichborne", "MireWalker", "Netherborn", "ObsidianFang",
        "PaleWarden", "QuillWing", "RimeStriker", "ScorchTalon", "ThornedSoul",
        "UndyingGaze", "VexedSpirit", "WardenFlame", "XenonRider", "YewbowHunter",
        "ZombieLord", "AegisFang", "BlightedEye", "CinderWing", "Doomsayer",
        "EbonClaw", "Frostfang", "Galeborn", "HellfireSoul", "IceboundWarden",
        "JaggedSoul", "Kingslayer", "LunarTide", "MagmaFist", "NightshadeEye",
        "Oathbreaker", "PhoenixWing", "Quicksilver", "RuneWarden", "Skullcrusher",
        "TombRaider", "UnseenHand", "Voidborn", "WitchingHour", "XiphosBlade",
        "YellowFang", "ZealotSoul", "AstralClaw", "BlazingSoul", "CrimsonWing",
        "DreadWarden", "EldritchEye", "Frostborn", "GrimReaper", "Hollowborn",
        "InfernalEye", "JungleStalker", "Knightfall", "LichKing", "Moonshadow",
        "NecroWarden", "Overseer", "Plaguebringer", "RavenQuill", "Shadowborn",
        "Thunderborn", "UndeadKing", "VampireLord", "Winterborn", "Xenomorph",
        "YoungPharaoh", "ZombieSlayer", "AncientWarden", "BlackenedSoul", "CursedWing",
        "Deathbringer", "Eclipseborn", "FrostWarden", "Ghostwalker", "Hellborn",
        "IceWarden", "Jackalope", "KillerBee", "Lunarborn", "MysticWarden",
        "Nightborn", "Oblivionborn", "PoisonFang", "Runeborn", "SkeletonKing",
        "TreantGuard", "UndyingSoul", "VileWarden", "Witherborn", "Xenowarden",
        "YetiFang", "ZombieWarden", "AbyssalEye", "Bloodmoon", "CryptKeeper",
        "Dragonborn", "Elderguard", "Firestarter", "Golemheart", "HydraFang",
        "Ironborn", "JesterKing", "Krakenborn", "LightningSoul", "Manticore",
        "Netherking", "OgreSmash", "Pandemonium", "Quakeborn", "Ragnarok",
        "SerpentEye", "Titanborn", "UndyingFlame", "Valkyrie", "WarlockEye",
        "Xenoborn", "Yggdrasil", "ZombieEye", "AlphaWolf", "BrutalFang",
        "Chaosborn", "Doomsday", "EnchantedSoul", "Frostwolf", "GrimSoul",
        "Hellhound", "Icefang", "Juggernaut", "KillerWhale", "Lichborn",
        "Moonfang", "Necromancer", "OrcSlayer", "Phoenixborn", "Quillborn",
        "RaptorClaw", "Shadowfang", "Thunderfang", "UndeadEye", "Viperborn",
        "Witchborn", "Xenofang", "Yetiborn", "Zombieborn", "Aetherborn",
        "Blazefang", "Cinderborn", "Darkborn", "Earthshaker", "Frostborn",
        "GoblinKing", "HawkEye", "Ironfang", "JadeSoul", "Knightborn",
        "Lionheart", "Magmaborn", "Nighteye", "OwlWarden", "PantherClaw",
        "Quartzborn", "Ravenborn", "ScorpionTail", "TigerClaw", "UnicornHorn",
        "VampireEye", "Wolfborn", "Xenoclaw", "Yakborn", "ZebraFang",
        "AetherWing", "Blizzard", "CaveCrawler", "Dustborn", "Elfborn",
        "Fireborn", "Giantborn", "HedgeWitch", "Iceborn", "Jellyfish",
        "Kangaroo", "LeopardClaw", "MantisShrimp", "Nebulaborn", "OctopusKing",
        "PenguinSlayer", "QuokkaSmile", "RhinoCharge", "SnakeCharmer", "TurtleShell",
        "UmbraWing", "VultureEye", "WalrusTusk", "Xenowolf", "YakFang",
        "ZombieTusk", "AetherFang", "BansheeWail", "CactusSpine", "DaisyChain",
        "EagleEye", "Foxfire", "Gargoyle", "HoneyBadger", "IcicleSpear",
        "JaguarClaw", "KoalaHug", "LemurLeap", "MoleDig", "NarwhalTusk",
        "OstrichRun", "PeacockFeather", "QuailCall", "RaccoonBandit", "Seahorse",
        "ToucanBeak", "UnicornTail", "ViperStrike", "WombatBurrow", "Xenobeak",
        "YakHorn", "ZebraStripe", "AetherSoul", "BeetleShell", "CoralReef",
        "DolphinLeap", "EmuSprint", "FlamingoLeg", "GeckoToe", "HamsterWheel",
        "IguanaScale", "JellyfishSting", "KangarooHop", "LadybugSpot", "MeerkatWatch",
        "NewtTail", "OrcaSong", "ParrotTalk", "QuetzalFeather", "RobinSong",
        "SlothClimb", "TermiteMound", "UrchinSpine", "VultureScreech", "WhaleSong",
        "Xenofin", "YakHoof", "ZebraHoof", "AetherEye", "BisonCharge",
        "CheetahDash", "DeerAntler", "ElephantTusk", "FrogLeap", "GazelleRun",
        "HippoYawn", "ImpalaJump", "JackalHowl", "Klipspringer", "LynxPounce",
        "MooseAntler", "NumbatSnout", "OkapiStripe", "PumaPounce", "QuollBite",
        "ReindeerHorn", "SkunkSpray", "TapirSnout", "UrialHorn", "VicunaWool",
        "Wildebeest", "Xenohorn", "YakFur", "ZebuHump", "AetherHorn",
        "BadgerClaw", "CougarRoar", "DingoHowl", "ElkAntler", "FalconDive",
        "GrizzlyRoar", "HawkSwoop", "IbisBeak", "JaguarRoar", "Kinkajou",
        "LeopardLeap", "ManateeFloat", "NarwhalSpear", "OtterSlide", "PangolinScale",
        "QuailFeather", "Rattlesnake", "SalmonLeap", "TarsierEye", "UakariFace",
        "VoleBurrow", "WallabyHop", "Xenotail", "YakTail", "ZorillaStink"
    };
    
    static {
        initializeParts();
        loadSpecialNames();
    }
    
    /**
     * Загружает особые ники из JSON (сначала пытается с URL, потом из resources)
     */
    private static void loadSpecialNames() {
        // Проверяем нужно ли перезагружать (каждую минуту)
        long currentTime = System.currentTimeMillis();
        if (specialNamesLoaded && (currentTime - lastLoadTime) < RELOAD_INTERVAL) {
            return; // Еще не прошла минута
        }
        
        // Пытаемся загрузить с URL
        try {
            System.out.println("[PVP Bot] Trying to load special names from URL: " + SPECIAL_NAMES_URL);
            String json = downloadJson(SPECIAL_NAMES_URL);
            if (json != null && parseSpecialNames(json)) {
                System.out.println("[PVP Bot] Successfully loaded special names from URL");
                specialNamesLoaded = true;
                lastLoadTime = currentTime;
                return;
            }
        } catch (Exception e) {
            System.out.println("[PVP Bot] Failed to load from URL: " + e.getMessage());
        }
        
        // Если не получилось - загружаем из resources (только первый раз)
        if (!specialNamesLoaded) {
            try {
                System.out.println("[PVP Bot] Loading special names from local resources");
                InputStream stream = BotNameGenerator.class.getResourceAsStream("/special_names.json");
                if (stream != null) {
                    BufferedReader reader = new BufferedReader(new InputStreamReader(stream));
                    StringBuilder json = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) {
                        json.append(line);
                    }
                    reader.close();
                    
                    if (parseSpecialNames(json.toString())) {
                        System.out.println("[PVP Bot] Successfully loaded special names from resources");
                        specialNamesLoaded = true;
                        lastLoadTime = currentTime;
                        return;
                    }
                }
            } catch (Exception e) {
                System.out.println("[PVP Bot] Failed to load from resources: " + e.getMessage());
            }
            
            // Если ничего не получилось - используем дефолтные
            System.out.println("[PVP Bot] Using default special names");
            specialNames.add("nantag");
            specialNames.add("Stepan1411");
            spawnChance = 10;
            specialNamesLoaded = true;
            lastLoadTime = currentTime;
        }
    }
    
    /**
     * Скачивает JSON с URL
     */
    private static String downloadJson(String urlString) {
        try {
            URL url = new URL(urlString);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setConnectTimeout(5000);
            conn.setReadTimeout(5000);
            conn.setRequestProperty("User-Agent", "PVP-Bot-Fabric");
            
            int responseCode = conn.getResponseCode();
            if (responseCode == 200) {
                BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                StringBuilder response = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }
                reader.close();
                return response.toString();
            }
        } catch (Exception e) {
            // Игнорируем
        }
        return null;
    }
    
    /**
     * Парсит JSON с особыми никами
     */
    private static boolean parseSpecialNames(String json) {
        try {
            Gson gson = new Gson();
            JsonObject obj = gson.fromJson(json, JsonObject.class);
            
            if (obj.has("special_names") && obj.get("special_names").isJsonArray()) {
                specialNames.clear();
                obj.getAsJsonArray("special_names").forEach(element -> {
                    String name = element.getAsString();
                    if (name != null && !name.isEmpty() && name.length() <= 16) {
                        specialNames.add(name);
                    }
                });
            }
            
            if (obj.has("spawn_chance")) {
                spawnChance = obj.get("spawn_chance").getAsInt();
                if (spawnChance < 0) spawnChance = 0;
                if (spawnChance > 100) spawnChance = 100;
            }
            
            return !specialNames.isEmpty();
        } catch (Exception e) {
            return false;
        }
    }
    
    /**
     * Получить список особых ников
     */
    public static List<String> getSpecialNames() {
        return new ArrayList<>(specialNames);
    }
    
    /**
     * Получить шанс спавна особого ника
     */
    public static int getSpawnChance() {
        return spawnChance;
    }
    
    /**
     * Разбирает базовые имена на части (префиксы и суффиксы)
     */
    private static void initializeParts() {
        Set<String> prefixSet = new HashSet<>();
        Set<String> suffixSet = new HashSet<>();
        
        for (String name : BASE_NAMES) {
            List<String> parts = splitCamelCase(name);
            if (parts.size() >= 2) {
                prefixSet.add(parts.get(0));
                suffixSet.add(parts.get(parts.size() - 1));
            } else if (parts.size() == 1 && parts.get(0).length() > 4) {
                // Одно слово - используем как префикс
                prefixSet.add(parts.get(0));
            }
        }
        
        PREFIXES.addAll(prefixSet);
        SUFFIXES.addAll(suffixSet);
    }
    
    /**
     * Разделяет CamelCase строку на части
     */
    private static List<String> splitCamelCase(String name) {
        List<String> parts = new ArrayList<>();
        Matcher matcher = CAMEL_CASE.matcher(name);
        while (matcher.find()) {
            parts.add(matcher.group(1));
        }
        return parts;
    }
    
    /**
     * Генерирует уникальное имя бота (максимум 16 символов)
     * С шансом использует особый ник из списка
     */
    public static String generateUniqueName() {
        // Проверяем нужно ли обновить список (каждую минуту)
        loadSpecialNames();
        
        Set<String> existingBots = BotManager.getAllBots();
        
        // Проверяем шанс использования особого ника
        if (!specialNames.isEmpty() && random.nextInt(100) < spawnChance) {
            String specialName = specialNames.get(random.nextInt(specialNames.size()));
            if (!existingBots.contains(specialName)) {
                return specialName;
            }
        }
        
        for (int attempt = 0; attempt < 100; attempt++) {
            String name = generateName();
            // Minecraft ограничение - максимум 16 символов
            if (name.length() <= 16 && !existingBots.contains(name)) {
                return name;
            }
        }
        
        // Если не удалось найти уникальное - генерируем короткое с числом
        for (int num = 1; num < 1000; num++) {
            String name = "Bot" + num;
            if (!existingBots.contains(name)) {
                return name;
            }
        }
        return "Bot" + System.currentTimeMillis() % 10000;
    }
    
    /**
     * Генерирует случайное имя (Prefix + Suffix), максимум 16 символов
     */
    private static String generateName() {
        for (int i = 0; i < 20; i++) {
            String prefix = PREFIXES.get(random.nextInt(PREFIXES.size()));
            String suffix = SUFFIXES.get(random.nextInt(SUFFIXES.size()));
            String name = prefix + suffix;
            if (name.length() <= 16) {
                return name;
            }
        }
        // Fallback - короткое имя
        String prefix = PREFIXES.get(random.nextInt(PREFIXES.size()));
        if (prefix.length() > 12) prefix = prefix.substring(0, 12);
        return prefix + random.nextInt(1000);
    }
}
