package com.kingpixel.ultrashop.infrastructure.config;

import com.kingpixel.cobbleutils.Model.DataBaseConfig;
import com.kingpixel.cobbleutils.Model.ItemModel;
import com.kingpixel.cobbleutils.Model.PanelsConfig;
import com.kingpixel.ultrashop.UltraShop;
import lombok.Data;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * POJO for config.json — no I/O logic, no GUI logic.
 */
@Data
public class ShopConfig {
  private String lang;
  private boolean debug;
  private boolean saveTransactions;
  private List<String> commands;
  private List<String> sellCommands;

  private DataBaseConfig dataBase;

  private String title;
  private int rows;
  private String soundOpen;
  private String soundClose;
  private ItemModel itemClose;
  private List<PanelsConfig> panels;

  private int maxBuyAmount;
  private int transactionPageSize;
  private Map<String, Float> discounts;
  private Map<String, BigDecimal> dailySellLimits;
  private String dailySellResetCooldown;

  private boolean webDashboardEnabled;
  private int webDashboardPort;
  private String webDashboardPassword;

  private WebhooksConfig webhooks;

  public ShopConfig() {
    this.lang = "en";
    this.debug = false;
    this.saveTransactions = true;
    this.commands = new ArrayList<>();
    this.commands.add("shop");
    this.commands.add(UltraShop.MOD_ID);
    this.sellCommands = new ArrayList<>();
    this.sellCommands.add("sell");

    this.dataBase = new DataBaseConfig();
    this.dataBase.setDatabase("ultrashop");

    this.title = "Shop";
    this.rows = 6;
    this.soundOpen = "";
    this.soundClose = "";
    this.itemClose = new ItemModel(49, "minecraft:barrier", "&cClose", List.of(), 0);
    this.panels = List.of(
      new PanelsConfig(new ItemModel("minecraft:gray_stained_glass_pane"), rows)
    );

    this.maxBuyAmount = 2304;
    this.transactionPageSize = 10;
    this.discounts = new HashMap<>();
    this.discounts.put("group.vip", 2.0f);
    this.dailySellLimits = new HashMap<>();
    this.dailySellResetCooldown = "24h";

    this.webDashboardEnabled = false;
    this.webDashboardPort = 8095;
    this.webDashboardPassword = "";

    this.webhooks = new WebhooksConfig();
  }

  public void check() {
    if (lang == null || lang.isBlank()) {
      lang = "en";
    }
    if (commands == null || commands.isEmpty()) {
      commands = new ArrayList<>();
      commands.add("shop");
      commands.add(UltraShop.MOD_ID);
    }
    if (sellCommands == null || sellCommands.isEmpty()) {
      sellCommands = new ArrayList<>();
      sellCommands.add("sell");
    }

    if (dataBase == null) {
      dataBase = new DataBaseConfig();
      dataBase.setDatabase("ultrashop");
    }

    if (title == null || title.isBlank()) {
      title = "Shop";
    }
    if (soundOpen == null) {
      soundOpen = "";
    }
    if (soundClose == null) {
      soundClose = "";
    }
    if (itemClose == null) {
      itemClose = new ItemModel(49, "minecraft:barrier", "&cClose", new ArrayList<>(), 0);
    }
    if (panels == null || panels.isEmpty()) {
      panels = List.of(
        new PanelsConfig(new ItemModel("minecraft:gray_stained_glass_pane"), rows <= 0 ? 6 : rows)
      );
    }

    if (maxBuyAmount <= 0) {
      maxBuyAmount = 2304;
    }
    if (transactionPageSize <= 0) {
      transactionPageSize = 10;
    }
    if (discounts == null) {
      discounts = new HashMap<>();
    }
    if (dailySellLimits == null) {
      dailySellLimits = new HashMap<>();
    }
    if (dailySellResetCooldown == null || dailySellResetCooldown.isBlank()) {
      dailySellResetCooldown = "24h";
    }

    if (webDashboardPort <= 0 || webDashboardPort > 65535) {
      webDashboardPort = 8095;
    }
    webDashboardPassword = webDashboardPassword == null ? "" : webDashboardPassword.trim();

    if (webhooks == null) {
      webhooks = new WebhooksConfig();
    }
    if (webhooks.getRotationWebhookUrl() == null) {
      webhooks.setRotationWebhookUrl("");
    }
    if (webhooks.getMaintenanceWebhookUrl() == null) {
      webhooks.setMaintenanceWebhookUrl("");
    }
  }
}
