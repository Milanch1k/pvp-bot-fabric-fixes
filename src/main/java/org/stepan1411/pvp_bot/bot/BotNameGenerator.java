package org.stepan1411.pvp_bot.bot;

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
     */
    public static String generateUniqueName() {
        Set<String> existingBots = BotManager.getAllBots();
        
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
