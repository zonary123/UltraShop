package com.kingpixel.ultrashop.infrastructure.config;

import com.kingpixel.cobbleutils.CobbleUtils;
import com.kingpixel.cobbleutils.Model.EconomyUse;
import com.kingpixel.cobbleutils.Model.ItemModel;
import com.kingpixel.cobbleutils.Model.conditions.Condition;
import com.kingpixel.cobbleutils.Model.conditions.PermissionCondition;
import com.kingpixel.cobbleutils.util.UtilsFile;
import com.kingpixel.cobbleutils.util.economys.providers.ImpactorEconomy;
import com.kingpixel.ultrashop.ShopContext;
import com.kingpixel.ultrashop.UltraShop;
import com.kingpixel.ultrashop.api.ShopOptionsApi;
import com.kingpixel.ultrashop.domain.model.PriceEntry;
import com.kingpixel.ultrashop.domain.model.Product;
import com.kingpixel.ultrashop.domain.model.RotationScope;
import com.kingpixel.ultrashop.domain.model.SubShop;
import com.kingpixel.ultrashop.domain.model.shop.AbstractShop;
import com.kingpixel.ultrashop.domain.model.shop.CategoryShop;
import com.kingpixel.ultrashop.domain.model.shop.NormalShop;
import com.kingpixel.ultrashop.domain.model.shop.RotationShop;
import com.kingpixel.ultrashop.domain.model.shop.Shop;
import com.kingpixel.ultrashop.domain.model.shop.ShopBridge;
import com.kingpixel.ultrashop.domain.model.shop.config.ConditionsConfig;
import com.kingpixel.ultrashop.domain.model.shop.config.DisplayConfig;
import com.kingpixel.ultrashop.domain.model.shop.config.EconomyConfig;
import com.kingpixel.ultrashop.domain.scheduler.SchedulerFactory;
import com.kingpixel.ultrashop.infrastructure.persistence.RepositoryFactory;
import com.kingpixel.ultrashop.infrastructure.persistence.json.JsonShopRepository;
import com.kingpixel.ultrashop.infrastructure.persistence.mongodb.MongoShopRepository;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;

/**
 * Loads and saves all configuration and shop files using UtilsFile.
 * No GUI logic — pure infrastructure.
 */
public final class ConfigLoader {


  private ConfigLoader() {
  }

  /**
   * Full load: config → lang → shops → repositories → sell index → README.
   */
  public static void load(ShopOptionsApi options) {
    ShopContext ctx = ShopContext.get();

    ShopConfig config = loadConfig(options);
    ctx.getConfigs().put(options.getModId(), config);
    options.setCommands(config.getCommands());

    if (options.getModId().equals(UltraShop.MOD_ID)) {
      loadLang(config);
    }

    ctx.setRepositories(new RepositoryFactory(config.getDataBase()));
    loadShops(options);
    ctx.getDataShop().init();
    ctx.getSellIndex().rebuild(ctx.getTypedShops());
    ShopReadmeGenerator.generateReadme(CobbleUtils.getPath().resolve(options.getPath()));
  }

  /**
   * Loads config.json using UtilsFile.readOrCreate.
   */
  public static ShopConfig loadConfig(ShopOptionsApi options) {
    Path configPath = CobbleUtils.getPath().resolve(options.getPath()).resolve("config.json");
    try {
      ShopConfig config = UtilsFile.readOrCreate(configPath, ShopConfig.class, ShopConfig::new);
      config.check();
      UtilsFile.write(configPath, config);
      return config;
    } catch (IOException e) {
      UltraShop.LOGGER.error("Error loading config: " + configPath, e);
      ShopConfig fallback = new ShopConfig();
      fallback.check();
      return fallback;
    }
  }

  /**
   * Loads language file using UtilsFile.readOrCreate.
   */
  public static void loadLang(ShopConfig config) {
    ShopContext ctx = ShopContext.get();
    Path langPath = CobbleUtils.getPath().resolve(UltraShop.MOD_ID).resolve("lang").resolve(config.getLang() + ".json");
    try {
      LangConfig lang = UtilsFile.readOrCreate(langPath, LangConfig.class, LangConfig::new);
      lang.check();
      UtilsFile.write(langPath, lang);
      ctx.setLang(lang);
    } catch (IOException e) {
      UltraShop.LOGGER.error("Error loading lang config: " + langPath, e);
      ctx.setLang(new LangConfig());
    }
  }

  /**
   * Loads all shop files recursively from the shop directory.
   */
  public static void loadShops(ShopOptionsApi options) {
    ShopContext ctx = ShopContext.get();

    if (ctx.getRepositories() != null && ctx.getRepositories().getShopRepository() instanceof JsonShopRepository) {
      Path shopDir = CobbleUtils.getPath().resolve(options.getPath()).resolve("shop");
      try {
        boolean createDefaults = false;
        if (!Files.exists(shopDir)) {
          Files.createDirectories(shopDir);
          createDefaults = true;
        } else {
          try (var stream = Files.list(shopDir)) {
            if (!stream.findAny().isPresent()) {
              createDefaults = true;
            }
          }
        }
        if (createDefaults) {
          createDefaultShops(shopDir);
        }
      } catch (IOException e) {
        UltraShop.LOGGER.error("Error ensuring JSON default shops", e);
      }
    }

    try {
      List<Shop> loaded = ctx.getRepositories() != null
        ? ctx.getRepositories().getShopRepository().loadAllShops(options)
        : new ArrayList<>();

      if (ctx.getRepositories() != null && ctx.getRepositories().getShopRepository() instanceof MongoShopRepository && loaded.isEmpty()) {
        List<Shop> defaults = new ArrayList<>();
        defaults.add(buildStarterBlocks());
        defaults.add(buildFarmMarket());
        defaults.add(buildMainMenu());
        for (Shop defaultShop : defaults) {
          defaultShop.setFilePath("mongodb:" + defaultShop.getId());
          defaultShop.check();
          ctx.getRepositories().getShopRepository().save(defaultShop);
          loaded.add(defaultShop);
        }
      }

      List<Shop> typedShops = new ArrayList<>();
      List<com.kingpixel.ultrashop.domain.model.Shop> legacyShops = new ArrayList<>();

      for (Shop shopLoaded : loaded) {
        try {
          com.kingpixel.ultrashop.domain.model.Shop legacy = ShopBridge.toLegacy(shopLoaded);
          legacy.setFilePath(shopLoaded.getFilePath());
          legacy.check();

          typedShops.add(shopLoaded);
          legacyShops.add(legacy);
        } catch (Exception e) {
          UltraShop.LOGGER.error("Error loading shop " + shopLoaded.getId(), e);
        }
      }

      ctx.getShops().put(options.getModId(), legacyShops);
      ctx.getTypedShops().put(options.getModId(), typedShops);
    } catch (Exception e) {
      UltraShop.LOGGER.error("Error loading shops for mod: " + options.getModId(), e);
      ctx.getShops().put(options.getModId(), new ArrayList<>());
      ctx.getTypedShops().put(options.getModId(), new ArrayList<>());
    }
  }

  private static void backupIncompatibleShop(Path shopDir, Path file) {
    try {
      Path backupDir = shopDir.resolve("_backup").resolve("incompatible");
      Files.createDirectories(backupDir);
      String timestamp = String.valueOf(System.currentTimeMillis());
      String name = file.getFileName().toString();
      Path target = backupDir.resolve(timestamp + "_" + name);
      Files.move(file, target, StandardCopyOption.REPLACE_EXISTING);
      UltraShop.LOGGER.warn(
        "Moved incompatible shop file to backup: " + file + " -> " + target);
    } catch (Exception backupError) {
      UltraShop.LOGGER.error("Failed to backup incompatible shop file: " + file, backupError);
    }
  }

  /**
   * Saves a single shop to disk in the canonical shopLoaded JSON format.
   */
  public static void saveShop(Shop shop) {
    if (ShopContext.get().getRepositories() != null) {
      ShopContext.get().getRepositories().getShopRepository().save(shop);
    }
  }

  /**
   * Creates a shop and adds it to the registry.
   */
  public static void createShop(ShopOptionsApi options, Shop shop) {
    shop.check();
    if (ShopContext.get().getRepositories() != null) {
      if (ShopContext.get().getRepositories().getShopRepository() instanceof JsonShopRepository) {
        Path shopDir = CobbleUtils.getPath().resolve(options.getPath()).resolve("shop");
        Path filePath = shopDir.resolve(shop.getId() + ".json");
        shop.setFilePath(filePath.toString());
      } else {
        shop.setFilePath("mongodb:" + shop.getId());
      }
      ShopContext.get().getRepositories().getShopRepository().save(shop);
      load(options);
    }
  }

  private static void createDefaultShops(Path shopDir) {
    List<Shop> defaults = new ArrayList<>();
    defaults.add(buildStarterBlocks());
    defaults.add(buildFarmMarket());
    defaults.add(buildToolsWorkshop());
    defaults.add(buildVipLounge());
    defaults.add(buildLimitedDrops());
    defaults.add(buildMulticurrencyBazaar());
    defaults.add(buildHourlyRotation());
    defaults.add(buildLegendaryRotation());
    defaults.add(buildDailySpecials());
    defaults.add(buildMainMenu());

    int slot = 0;
    for (Shop shop : defaults) {
      assignDisplaySlotIfMissing((AbstractShop) shop, slot++);
      shop.check();
      Path file = shopDir.resolve(((AbstractShop) shop).getId() + ".json");
      if (Files.exists(file)) {
        continue;
      }
      try {
        UtilsFile.write(file, shop);
      } catch (IOException e) {
        UltraShop.LOGGER.error("Error creating default shop: " + file, e);
      }
    }
  }

  private static void assignDisplaySlotIfMissing(AbstractShop shop, int fallbackSlot) {
    DisplayConfig dc = shop.getDisplayConfig();
    if (dc == null || dc.getDisplayItem() == null) return;
    ItemModel display = dc.getDisplayItem();
    if (display.getSlot() == null || display.getSlot() == 0) {
      display.setSlot(fallbackSlot);
    }
  }

  private static CategoryShop buildMainMenu() {
    CategoryShop shop = new CategoryShop();
    shop.setId("main_menu");
    shop.setDisplayConfig(DisplayConfig.builder()
      .name("Main Menu")
      .autoPlace(false)
      .rows(6)
      .displayItem(displayIcon("minecraft:compass", "<#2ecc71>« <#feca57><b>Main Menu</b> <#2ecc71>»", List.of(
        "§8─────────────────────────────────",
        " §7Browse all shops by category.",
        " §7Select a department to view products.",
        "§8─────────────────────────────────"
      )))
      .build());
    shop.setSubShops(new ArrayList<>(List.of(
      new SubShop(10, "starter_blocks"),
      new SubShop(11, "farm_market"),
      new SubShop(12, "tools_workshop"),
      new SubShop(13, "limited_drops"),
      new SubShop(14, "multicurrency_bazaar"),
      new SubShop(15, "vip_lounge"),
      new SubShop(16, "hourly_rotation"),
      new SubShop(20, "legendary_rotation"),
      new SubShop(22, "daily_specials")
    )));
    return shop;
  }

  private static NormalShop buildStarterBlocks() {
    NormalShop shop = new NormalShop();
    shop.setId("starter_blocks");
    shop.setDisplayConfig(simpleDisplay("Starter Blocks", "minecraft:grass_block", "<#2ecc71>« <#10ac84>Starter Blocks <#2ecc71>»", List.of(
      "§8─────────────────────────────────",
      " §7Cheap building blocks for new players.",
      " §7Every product here is always in stock.",
      "§8─────────────────────────────────"
    )));
    shop.setProducts(new ArrayList<>(List.of(
      simpleProduct("minecraft:dirt", 5, 1),
      simpleProduct("minecraft:cobblestone", 8, 2),
      simpleProduct("minecraft:oak_planks", 10, 2),
      simpleProduct("minecraft:stone", 12, 3),
      simpleProduct("minecraft:glass", 15, 4),
      simpleProduct("minecraft:torch", 5, 0)
    )));
    return shop;
  }

  private static NormalShop buildFarmMarket() {
    NormalShop shop = new NormalShop();
    shop.setId("farm_market");
    shop.setDisplayConfig(simpleDisplay("Farm Market", "minecraft:wheat", "<#2ecc71>« <#feca57>Farm Market <#2ecc71>»", List.of(
      "§8─────────────────────────────────",
      " §7Sell your harvest here for a fair price.",
      " §7This shop is sell-only.",
      "§8─────────────────────────────────"
    )));
    shop.setProducts(new ArrayList<>(List.of(
      simpleProduct("minecraft:wheat", 0, 4),
      simpleProduct("minecraft:carrot", 0, 5),
      simpleProduct("minecraft:potato", 0, 5),
      simpleProduct("minecraft:beetroot", 0, 6),
      simpleProduct("minecraft:pumpkin", 0, 10),
      simpleProduct("minecraft:melon", 0, 8),
      simpleProduct("minecraft:apple", 0, 12),
      simpleProduct("minecraft:sweet_berries", 0, 7)
    )));
    return shop;
  }

  private static NormalShop buildToolsWorkshop() {
    NormalShop shop = new NormalShop();
    shop.setId("tools_workshop");
    shop.setDisplayConfig(DisplayConfig.builder()
      .name("Tools Workshop")
      .autoPlace(false)
      .rows(6)
      .displayItem(displayIcon("minecraft:iron_pickaxe", "<#2ecc71>« <#ff9f43>Tools Workshop <#2ecc71>»", List.of(
        "§8─────────────────────────────────",
        " §7Buy tools at fixed positions in the GUI.",
        " §7Custom arrangements pin each product.",
        "§8─────────────────────────────────"
      )))
      .build());
    shop.setProducts(new ArrayList<>(List.of(
      slottedProduct("minecraft:wooden_pickaxe", 50, 0, 10),
      slottedProduct("minecraft:stone_pickaxe", 150, 0, 11),
      slottedProduct("minecraft:iron_pickaxe", 500, 0, 12),
      slottedProduct("minecraft:diamond_pickaxe", 2500, 0, 13),
      slottedProduct("minecraft:netherite_pickaxe", 10000, 0, 14),
      slottedProduct("minecraft:wooden_axe", 50, 0, 19),
      slottedProduct("minecraft:stone_axe", 150, 0, 20),
      slottedProduct("minecraft:iron_axe", 500, 0, 21),
      slottedProduct("minecraft:diamond_axe", 2500, 0, 22),
      slottedProduct("minecraft:netherite_axe", 10000, 0, 23)
    )));
    return shop;
  }

  private static NormalShop buildVipLounge() {
    NormalShop shop = new NormalShop();
    shop.setId("vip_lounge");
    shop.setDisplayConfig(simpleDisplay("VIP Lounge", "minecraft:diamond", "<#2ecc71>« <#ff6b81>VIP Lounge <#2ecc71>»", List.of(
      "§8─────────────────────────────────",
      " §7Exclusive high-tier items for VIPs.",
      " §7Requires VIP permission to enter.",
      "§8─────────────────────────────────"
    )));
    List<Condition> openConditions = new ArrayList<>();
    openConditions.add(PermissionCondition.builder().permission("ultrashop.vip").build());
    shop.setConditionsConfig(ConditionsConfig.builder()
      .openConditions(openConditions)
      .build());
    shop.setEconomyConfig(EconomyConfig.builder()
      .globalDiscount(15.0f)
      .build());
    shop.setProducts(new ArrayList<>(List.of(
      simpleProduct("minecraft:netherite_ingot", 8000, 4000),
      simpleProduct("minecraft:elytra", 50000, 25000),
      simpleProduct("minecraft:totem_of_undying", 12000, 6000),
      simpleProduct("minecraft:enchanted_golden_apple", 5000, 2500),
      simpleProduct("minecraft:beacon", 30000, 15000)
    )));
    return shop;
  }

  private static NormalShop buildLimitedDrops() {
    NormalShop shop = new NormalShop();
    shop.setId("limited_drops");
    shop.setDisplayConfig(simpleDisplay("Limited Drops", "minecraft:totem_of_undying", "<#2ecc71>« <#ee5253>Limited Drops <#2ecc71>»", List.of(
      "§8─────────────────────────────────",
      " §7Buy rare items with purchase limits.",
      " §7Limits reset automatically after cooldown.",
      "§8─────────────────────────────────"
    )));

    Product dailyDiamond = simpleProduct("minecraft:diamond", 100, 50);
    dailyDiamond.setMax(8);
    dailyDiamond.setCooldown("1440m");

    Product hourlyEnderPearl = simpleProduct("minecraft:ender_pearl", 200, 100);
    hourlyEnderPearl.setMax(4);
    hourlyEnderPearl.setCooldown("60m");

    Product weeklyTotem = simpleProduct("minecraft:totem_of_undying", 5000, 2500);
    weeklyTotem.setMax(1);
    weeklyTotem.setCooldown("10080m");

    shop.setProducts(new ArrayList<>(List.of(dailyDiamond, hourlyEnderPearl, weeklyTotem)));
    return shop;
  }

  private static NormalShop buildMulticurrencyBazaar() {
    NormalShop shop = new NormalShop();
    shop.setId("multicurrency_bazaar");
    shop.setDisplayConfig(simpleDisplay("Bazaar", "minecraft:emerald", "<#2ecc71>« <#54a0ff>Multi-Currency Bazaar <#2ecc71>»", List.of(
      "§8─────────────────────────────────",
      " §7Special items priced in multiple currencies.",
      " §7Accepts virtual dollars and physical items.",
      "§8─────────────────────────────────"
    )));

    EconomyUse dollars = new EconomyUse(ImpactorEconomy.IDENTIFY, "impactor:dollars");
    EconomyUse diamonds = new EconomyUse("item", "minecraft:diamond");
    EconomyUse emeralds = new EconomyUse("item", "minecraft:emerald");

    shop.setEconomyConfig(EconomyConfig.builder()
      .economies(new LinkedHashSet<>(List.of(dollars)))
      .build());

    Product netheriteSword = simpleProduct("minecraft:netherite_sword", 0, 0);
    netheriteSword.setPrices(new ArrayList<>(List.of(
      new PriceEntry(dollars, BigDecimal.valueOf(2000), BigDecimal.valueOf(1000)),
      new PriceEntry(diamonds, BigDecimal.valueOf(8), BigDecimal.valueOf(4))
    )));

    Product enchantedBook = simpleProduct("minecraft:enchanted_book", 0, 0);
    enchantedBook.setPrices(new ArrayList<>(List.of(
      new PriceEntry(emeralds, BigDecimal.valueOf(16), BigDecimal.valueOf(8))
    )));

    Product shulkerBox = simpleProduct("minecraft:shulker_box", 0, 0);
    shulkerBox.setPrices(new ArrayList<>(List.of(
      new PriceEntry(dollars, BigDecimal.valueOf(5000), BigDecimal.ZERO),
      new PriceEntry(diamonds, BigDecimal.valueOf(20), BigDecimal.ZERO),
      new PriceEntry(emeralds, BigDecimal.valueOf(64), BigDecimal.ZERO)
    )));

    shop.setProducts(new ArrayList<>(List.of(netheriteSword, enchantedBook, shulkerBox)));
    return shop;
  }

  private static RotationShop buildHourlyRotation() {
    RotationShop shop = new RotationShop();
    shop.setId("hourly_rotation");
    shop.setDisplayConfig(simpleDisplay("Hourly Rotation", "minecraft:clock", "<#2ecc71>« <#00d2d3>Hourly Rotation <#2ecc71>»", List.of(
      "§8─────────────────────────────────",
      " §7A pool of rotating items refreshed hourly.",
      " §7New deals appear dynamically.",
      "§8─────────────────────────────────"
    )).toBuilder().autoPlace(false).build());
    shop.setConditionsConfig(ConditionsConfig.builder().announceRotation(true).build());
    shop.setScheduler(SchedulerFactory.fromInterval("1h"));
    shop.setRotationAmount(4);
    shop.setRotationSlots(new ArrayList<>(List.of(10, 11, 12, 13)));
    shop.setProducts(new ArrayList<>(List.of(
      weightedProduct("minecraft:redstone", 30, 15, 100),
      weightedProduct("minecraft:lapis_lazuli", 25, 12, 100),
      weightedProduct("minecraft:quartz", 35, 17, 80),
      weightedProduct("minecraft:glowstone_dust", 40, 20, 80),
      weightedProduct("minecraft:blaze_rod", 150, 75, 50),
      weightedProduct("minecraft:ghast_tear", 500, 250, 20),
      weightedProduct("minecraft:nether_star", 5000, 2500, 5)
    )));
    return shop;
  }

  private static RotationShop buildLegendaryRotation() {
    RotationShop shop = new RotationShop();
    shop.setId("legendary_rotation");
    shop.setDisplayConfig(simpleDisplay("Legendary Rotation", "minecraft:dragon_egg", "<#2ecc71>« <#ff9f43>Legendary Rotation <#2ecc71>»", List.of(
      "§8─────────────────────────────────",
      " §7Rotating high-tier legendary drops.",
      " §7Rotates every Friday at 18:00.",
      "§8─────────────────────────────────"
    )));
    shop.setConditionsConfig(ConditionsConfig.builder().announceRotation(true).build());
    shop.setScheduler(SchedulerFactory.fromCron("0 18 * * 5"));
    shop.setRotationAmount(2);
    shop.setProducts(new ArrayList<>(List.of(
      weightedProduct("minecraft:elytra", 25000, 10000, 30),
      weightedProduct("minecraft:netherite_block", 15000, 7500, 50),
      weightedProduct("minecraft:beacon", 20000, 10000, 40),
      weightedProduct("minecraft:dragon_egg", 100000, 50000, 5),
      weightedProduct("minecraft:trident", 8000, 4000, 60),
      weightedProduct("minecraft:enchanted_golden_apple", 3000, 1500, 80)
    )));
    return shop;
  }

  private static RotationShop buildDailySpecials() {
    RotationShop shop = new RotationShop();
    shop.setId("daily_specials");
    shop.setDisplayConfig(simpleDisplay("Daily Specials", "minecraft:sunflower", "<#2ecc71>« <#feca57>Daily Specials <#2ecc71>»", List.of(
      "§8─────────────────────────────────",
      " §7Special deals rotated daily at midnight.",
      " §7Grab them before they disappear!",
      "§8─────────────────────────────────"
    )));
    shop.setScheduler(SchedulerFactory.fromCron("0 0 * * *"));
    shop.setRotationAmount(6);
    shop.setRotationScope(RotationScope.PLAYER);
    shop.setProducts(new ArrayList<>(List.of(
      weightedProduct("minecraft:iron_ingot", 50, 25, 100),
      weightedProduct("minecraft:gold_ingot", 80, 40, 100),
      weightedProduct("minecraft:copper_ingot", 30, 15, 100),
      weightedProduct("minecraft:emerald", 200, 100, 80),
      weightedProduct("minecraft:diamond", 300, 150, 60),
      weightedProduct("minecraft:experience_bottle", 100, 0, 70),
      weightedProduct("minecraft:saddle", 800, 400, 40),
      weightedProduct("minecraft:name_tag", 600, 300, 50)
    )));
    return shop;
  }

  private static DisplayConfig simpleDisplay(String name, String item, String displayname, List<String> lore) {
    return DisplayConfig.builder()
      .name(name)
      .autoPlace(true)
      .rows(6)
      .displayItem(displayIcon(item, displayname, lore))
      .build();
  }

  private static ItemModel displayIcon(String item, String displayname, List<String> lore) {
    return new ItemModel(0, item, displayname, lore, 0);
  }

  private static Product simpleProduct(String id, double buy, double sell) {
    Product p = new Product();
    p.setProduct(id);
    p.setBuy(BigDecimal.valueOf(buy));
    p.setSell(BigDecimal.valueOf(sell));
    return p;
  }

  private static Product slottedProduct(String id, double buy, double sell, int slot) {
    Product p = simpleProduct(id, buy, sell);
    p.setSlot(slot);
    return p;
  }

  private static Product weightedProduct(String id, double buy, double sell, int chance) {
    Product p = simpleProduct(id, buy, sell);
    p.setChance(chance);
    return p;
  }

}