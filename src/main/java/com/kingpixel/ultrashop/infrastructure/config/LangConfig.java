package com.kingpixel.ultrashop.infrastructure.config;

import com.kingpixel.cobbleutils.Model.ItemModel;
import lombok.Data;

import java.util.List;

/**
 * POJO for language/message configuration.
 * No GUI instances, no I/O — pure data.
 */
@Data
public class LangConfig {
  private String prefix;
  private String messageNotBuyPermission;
  private String messageNotHavePermission;
  private String messageShopNotOpen;
  private String messageNotEnoughMoney;
  private String messageNotSell;
  private String messageSell;
  private String messageSimpleSell;
  private String messageYouCantBuyNow;
  private String messageSimpleBuy;
  private String messageYouCantSellNow;
  private String messageShopDailySellLimitReached;
  private String messageDailySellLimitReached;
  private String messageBuyPriceLessThanSell;
  private String messageNotEnoughSpace;
  private String messageOutOfStock;
  private String messageNotEnoughStock;
  private String formatSell;
  private String notExtraInfo;
  private String messageShopRotated;
  private String messageInvalidNumber;
  private String messageConditionTypesUnavailable;
  private String messageConditionCreateFailed;
  private String messageShopInMaintenance;
  private String messageChatPromptEnterAmount;
  private String messageChatPromptInvalidAmount;
  private String cooldownReady;

  private String commandReloaded;
  private String commandReloadFailed;
  private String commandShopAlreadyExists;
  private String commandShopCreated;
  private String commandDynamicShopRestarted;
  private String commandDynamicShopInvalid;
  private String commandShopNotFound;
  private String commandShopDeleted;
  private String commandShopDeleteError;
  private String commandNoRepository;
  private String commandNoTransactionsFound;
  private String commandTransactionsHeader;
  private String commandTransactionLine;
  private String commandStatsConsoleTitle;
  private String commandStatsConsoleTransactions;
  private String commandStatsConsolePlayers;
  private String commandStatsConsoleRevenue;
  private String commandStatsConsolePayouts;
  private String commandStatsConsoleNet;

  private String transactionMenuTitle;
  private String transactionBuyLabel;
  private String transactionSellLabel;
  private String transactionDateLabel;
  private String transactionShopLabel;
  private String transactionProductLabel;
  private String transactionAmountLabel;
  private String transactionPriceLabel;
  private String statsMenuTitle;
  private String statsServerOverviewTitle;
  private String statsPlayerOverviewTitle;
  private String statsShopBreakdownTitle;
  private String statsTotalTransactionsLabel;
  private String statsUniquePlayersLabel;
  private String statsRevenueLabel;
  private String statsPayoutLabel;
  private String statsNetProfitLabel;
  private String statsItemsBoughtLabel;
  private String statsItemsSoldLabel;
  private String statsTotalSpentLabel;
  private String statsTotalEarnedLabel;
  private String statsNoData;
  private String statsShopBreakdownEntry;
  private String statsTopProductTitle;
  private String statsTopProductShopLabel;
  private String statsTopProductRankLabel;
  private String statsTopProductBoughtLabel;
  private String statsTopProductSoldLabel;
  private String statsTopProductUniquePlayersLabel;

  private String editorTitleShopList;
  private String editorTitleProductList;
  private String editorTitleProductEdit;
  private String editorTitleShopSettings;
  private String editorTitleInventoryPicker;
  private String editorTitleInventoryPickerEdit;
  private String editorTitleConditionList;
  private String editorTitleAddCondition;
  private String editorTitleProductConditionList;
  private String editorButtonClose;
  private String editorButtonBack;
  private String editorButtonStockControl;
  private String editorPromptExactBuyPrice;
  private String editorPromptExactSellPrice;

  private String sellGuiTitle;
  private String messageShopNotFound;
  private String messageErrorParsingArgs;
  private String messagePropertiesCannotBeEmpty;
  private String messageAddedPokemon;
  private String messageShopMaintenanceStatus;
  private String shopStatusOpen;
  private String shopStatusClosed;
  private String titleSearch;

  private String editorTitleSubShopList;
  private String editorTitleSelectShopType;
  private String editorTitleConfirmWarning;
  private String editorTitleAddPokemonOptions;
  private String editorTitleRectangle;
  private String editorTitleEconomies;
  private String editorTitleDiscounts;
  private String editorTitleProductPrices;

  private String editorChatInputPrefix;
  private String editorChatInputPrompt;
  private String editorChatInputCancelled;
  private String editorChatInputError;
  private String messagePokemonAddedFromParty;
  private String messageErrorOpeningPartyPc;
  private String editorInvalidId;
  private String editorHoldItemInHand;
  private String editorInvalidSlotsInput;
  private String editorInvalidFormat;
  private String editorInvalidKeyValueFormat;

  private List<String> infoProduct;

  private ItemModel shopInfoPermanent;
  private ItemModel shopInfoDynamic;

  private ItemModel globalDisplay;
  private ItemModel globalItemInfoShop;
  private ItemModel globalItemBalance;
  private ItemModel globalItemPrevious;
  private ItemModel globalItemClose;
  private ItemModel globalItemNext;

  private ItemModel add1;
  private ItemModel add8;
  private ItemModel add16;
  private ItemModel add64;
  private ItemModel addMax;
  private ItemModel customAmount;
  private ItemModel remove1;
  private ItemModel remove8;
  private ItemModel remove16;
  private ItemModel remove64;

  private BuyAndSellConfig menuBuyAndSell;

  public LangConfig() {
    prefix = "<#2ecc71>« <#feca57><b>UltraShop</b> <#2ecc71>» §r";
    messageShopNotOpen = "%prefix% <#ff6b6b>The shop is not open";
    messageShopRotated = "%prefix% <#2ecc71>The shop <#ffa502>%shop% <#2ecc71>has been rotated";
    messageNotBuyPermission = "%prefix% <#ff6b6b>You can't buy this product";
    messageNotHavePermission = "%prefix% <#ff6b6b>You do not have permission for this shop -> <#ffa502>%permission%";
    messageNotEnoughMoney = "%prefix% <#ff6b6b>You do not have enough money";
    messageNotSell = "%prefix% <#ff6b6b>You don't have anything to sell";
    messageSell = "%prefix% <#2ecc71>You have sold:\n %sell%";
    messageNotEnoughSpace = "%prefix% <#ff6b6b>You don't have enough space in your inventory";
    messageOutOfStock = "%prefix% <#ff6b6b>This product is out of stock";
    messageNotEnoughStock = "%prefix% <#ff6b6b>Not enough stock available. Remaining: <#ffa502>%remaining%";
    messageBuyPriceLessThanSell = "%prefix% <#ff6b6b>The buy price is less than the sell price";
    messageSimpleSell = "%prefix% <#2ecc71>You have sold <#ffa502>%amount%x <#2ecc71>and earned <#feca57>%price%";
    messageYouCantBuyNow = "%prefix% <#ff6b6b>You reached your limit of <#ffa502>%limit%x <#ff6b6b>for this product. Cooldown: <#ffa502>%time%s";
    messageSimpleBuy = "%prefix% <#2ecc71>You have bought <#ffa502>%amount%x %product% <#2ecc71>for <#feca57>%price%";
    messageYouCantSellNow = "%prefix% <#ff6b6b>You reached your sell limit of <#ffa502>%limit%x <#ff6b6b>for this product. Cooldown: <#ffa502>%time%s";
    messageShopDailySellLimitReached = "%prefix% <#ff6b6b>You have reached the shop's daily sell limit of <#ffa502>%limit% %currency%<#ff6b6b>!";
    messageDailySellLimitReached = "%prefix% <#ff6b6b>You have reached your daily sell limit of <#ffa502>%limit% %currency%<#ff6b6b>!";
    formatSell = " <#a0aec0>- <#ffa502>%price%";
    notExtraInfo = "<#a0aec0>No extra information";
    messageInvalidNumber = "%prefix% <#ff6b6b>Invalid number: <#ffa502>%input%";
    messageConditionTypesUnavailable = "%prefix% <#ff6b6b>Could not load condition types from CobbleUtils.";
    messageConditionCreateFailed = "%prefix% <#ff6b6b>Failed to create condition: <#ffa502>%error%";
    messageShopInMaintenance = "%prefix% <#ff6b6b>The shop <#ffa502>%shop% <#ff6b6b>is currently closed for maintenance.";
    messageChatPromptEnterAmount = "Enter amount to buy/sell (or type 'cancel' to abort):";
    messageChatPromptInvalidAmount = "<#ff6b6b>Invalid amount. Please enter a positive integer.";
    cooldownReady = "<#2ecc71>Ready";

    commandReloaded = "%prefix% <#2ecc71>Reloaded <#ffa502>%modId% <#2ecc71>shops";
    commandReloadFailed = "%prefix% <#ff6b6b>Reload failed for <#ffa502>%modId%<#ff6b6b>: <#ffa502>%error%";
    commandShopAlreadyExists = "%prefix% <#ff6b6b>Shop already exists: <#ffa502>%shop%";
    commandShopCreated = "%prefix% <#2ecc71>Created shop: <#ffa502>%shop%";
    commandDynamicShopRestarted = "%prefix% <#2ecc71>Restarted dynamic shop: <#ffa502>%shop%";
    commandDynamicShopInvalid = "%prefix% <#ff6b6b>Shop is not dynamic or not found: <#ffa502>%shop%";
    commandShopNotFound = "%prefix% <#ff6b6b>Shop not found: <#ffa502>%shop%";
    commandShopDeleted = "%prefix% <#2ecc71>Deleted shop: <#ffa502>%shop%";
    commandShopDeleteError = "%prefix% <#ff6b6b>Error deleting shop: <#ffa502>%error%";
    commandNoRepository = "%prefix% <#ff6b6b>No repository available";
    commandNoTransactionsFound = "%prefix% <#a0aec0>No transactions found for <#ffa502>%player%";
    commandTransactionsHeader = " <#feca57>« <#2ecc71>Transactions for %player% <#feca57>»";
    commandTransactionLine = " <#a0aec0>[%date%] %action% <#a0aec0>x%amount% <#ffa502>%product% <#a0aec0>(%price% %currency%)";
    commandStatsConsoleTitle = " <#feca57>« <#2ecc71>UltraShop Stats (30d) <#feca57>»";
    commandStatsConsoleTransactions = " <#a0aec0>• Transactions: <#ffffff>%value%";
    commandStatsConsolePlayers = " <#a0aec0>• Players: <#ffffff>%value%";
    commandStatsConsoleRevenue = " <#a0aec0>• Revenue: <#2ecc71>$%value%";
    commandStatsConsolePayouts = " <#a0aec0>• Payouts: <#ff6b6b>$%value%";
    commandStatsConsoleNet = " <#a0aec0>• Net Profit: <#feca57>$%value%";

    transactionMenuTitle = "<#feca57>Transactions: <#ffffff>%player%";
    transactionBuyLabel = "<#2ecc71>BUY";
    transactionSellLabel = "<#ff6b6b>SELL";
    transactionDateLabel = "<#a0aec0>Date: <#ffffff>%value%";
    transactionShopLabel = "<#a0aec0>Shop: <#ffffff>%value%";
    transactionProductLabel = "<#a0aec0>Product: <#ffffff>%value%";
    transactionAmountLabel = "<#a0aec0>Amount: <#ffffff>%value%";
    transactionPriceLabel = "<#a0aec0>Price: <#feca57>%value%";
    statsMenuTitle = "<#feca57><b>UltraShop Stats</b> <#a0aec0>(30 days)";
    statsServerOverviewTitle = "<#feca57>⚡ <b>Server Overview</b> <#a0aec0>(30d)";
    statsPlayerOverviewTitle = "<#54a0ff>👤 <b>Your Stats</b> <#a0aec0>(30d)";
    statsShopBreakdownTitle = "<#feca57>🏪 <b>Shop Breakdown</b> <#a0aec0>(30d)";
    statsTotalTransactionsLabel = "<#a0aec0>Total Transactions: <#ffffff>%value%";
    statsUniquePlayersLabel = "<#a0aec0>Unique Players: <#ffffff>%value%";
    statsRevenueLabel = "<#a0aec0>Revenue (buys): <#2ecc71>$%value%";
    statsPayoutLabel = "<#a0aec0>Payouts (sells): <#ff6b6b>$%value%";
    statsNetProfitLabel = "<#a0aec0>Net Profit: <#feca57>$%value%";
    statsItemsBoughtLabel = "<#a0aec0>Items Bought: <#ffffff>%value%";
    statsItemsSoldLabel = "<#a0aec0>Items Sold: <#ffffff>%value%";
    statsTotalSpentLabel = "<#a0aec0>Total Spent: <#ff6b6b>$%value%";
    statsTotalEarnedLabel = "<#a0aec0>Total Earned: <#2ecc71>$%value%";
    statsNoData = "<#a0aec0>No data yet";
    statsShopBreakdownEntry = "<#feca57>%shop%<#a0aec0>: <#2ecc71>$%revenue% <#a0aec0>/ <#ff6b6b>$%payout% <#718096>(%transactions% tx)";
    statsTopProductTitle = "<#feca57>#%rank% <#ffffff>%product%";
    statsTopProductShopLabel = "<#a0aec0>Shop: <#ffffff>%value%";
    statsTopProductRankLabel = "<#a0aec0>Rank: <#feca57>#%value%";
    statsTopProductBoughtLabel = "<#a0aec0>Bought: <#54a0ff>%amount%x <#a0aec0>by <#ffffff>%players% <#a0aec0>players";
    statsTopProductSoldLabel = "<#a0aec0>Sold: <#ff4757>%amount%x <#a0aec0>by <#ffffff>%players% <#a0aec0>players";
    statsTopProductUniquePlayersLabel = "<#a0aec0>Unique Players: <#ffffff>%value%";

    editorTitleShopList = "<#feca57><b>Shop Editor</b>";
    editorTitleProductList = "<#feca57><b>Products:</b> <#feca57>%shop%";
    editorTitleProductEdit = "<#feca57><b>Edit:</b> <#feca57>%product%";
    editorTitleShopSettings = "<#feca57><b>Settings:</b> <#feca57>%shop%";
    editorTitleInventoryPicker = "<#feca57><b>Pick Item from Inventory</b>";
    editorTitleInventoryPickerEdit = "<#feca57><b>Pick Item for:</b> <#feca57>%product%";
    editorTitleConditionList = "<#feca57><b>Conditions:</b> <#feca57>%shop%";
    editorTitleAddCondition = "<#feca57><b>Add Condition</b>";
    editorTitleProductConditionList = "<#feca57><b>%label% Conditions:</b> <#feca57>%product%";
    editorButtonClose = "<#ff6b6b>✕ Close";
    editorButtonBack = "<#a0aec0>← Back";
    editorButtonStockControl = "<#54a0ff>📦 Stock Control";
    editorPromptExactBuyPrice = "Enter exact buy price:";
    editorPromptExactSellPrice = "Enter exact sell price:";

    sellGuiTitle = "<#54a0ff>Sell GUI - Place items to sell";
    messageShopNotFound = "<#ff6b6b>Shop not found: <#feca57>%shop%";
    messageErrorParsingArgs = "<#ff6b6b>Error parsing command arguments.";
    messagePropertiesCannotBeEmpty = "<#ff6b6b>Properties cannot be empty.";
    messageAddedPokemon = "<#2ecc71>Added Pokémon: <#54a0ff>%pokemon% <#2ecc71>to shop: <#feca57>%shop%";
    messageShopMaintenanceStatus = "%prefix% <#a0aec0>Shop <#feca57>%shop% <#a0aec0>is now %status%";
    shopStatusOpen = "<#2ecc71>OPEN";
    shopStatusClosed = "<#ff6b6b>CLOSED (Maintenance)";
    titleSearch = "<#feca57><b>Search:</b> <#ffffff>%query%";

    editorTitleSubShopList = "<#feca57><b>Sub-Shops:</b> <#feca57>%shop%";
    editorTitleSelectShopType = "<#feca57><b>Select Shop Type</b>";
    editorTitleConfirmWarning = "<#ff6b6b><b>WARNING</b>";
    editorTitleAddPokemonOptions = "<#54a0ff><b>Add Pokémon Options</b>";
    editorTitleRectangle = "<#feca57><b>Rectangle Settings</b>";
    editorTitleEconomies = "<#feca57><b>Shop Economies</b>";
    editorTitleDiscounts = "<#feca57><b>Permission Discounts</b>";
    editorTitleProductPrices = "<#feca57><b>Prices:</b> <#feca57>%product%";

    editorChatInputPrefix = "<#feca57>[UltraShop Editor] <#f5f6fa>";
    editorChatInputPrompt = "<#a0aec0>Type your value in chat. Type <#ff6b6b>cancel <#a0aec0>to abort.";
    editorChatInputCancelled = "<#ff6b6b>Input cancelled.";
    editorChatInputError = "<#ff6b6b>Error: %error%";
    messagePokemonAddedFromParty = "<#2ecc71>Added Pokémon from Party/PC: <#54a0ff>%pokemon%";
    messageErrorOpeningPartyPc = "<#ff6b6b>Error opening Party/PC menu: %error%";
    editorInvalidId = "<#ff6b6b>Invalid ID.";
    editorHoldItemInHand = "<#ff6b6b>Hold an item in your hand first.";
    editorInvalidSlotsInput = "<#ff6b6b>Invalid slots input. Must be numbers between 0 and %max%";
    editorInvalidFormat = "<#ff6b6b>Invalid format or number: %input%";
    editorInvalidKeyValueFormat = "<#ff6b6b>Invalid format. Use key:value.";


    infoProduct = List.of(
      "%info%",
      "§8─────────────────────────────",
      " <#a0aec0>📦 Quantity: <#54a0ff>%amount%x <#718096>(Pack: %pack%x)",
      " ",
      " <#a0aec0>💰 Buy: <#2ecc71>%buy% %removebuy%",
      " <#a0aec0>🏷️ Discount: <#ffa502>%discount% %removediscount%",
      " <#a0aec0>💵 Sell: <#ff4757>%sell% %removesell%",
      " ",
      " <#a0aec0>📦 Stock: <#74b9ff>%stock_remaining%<#a0aec0>/<#f4d03f>%stock_limit% §7(%stock_mode%) %removestock%",
      " <#a0aec0>⏱ Cooldown: <#ffa502>%cooldown_time% %removelimit%",
      "§8─────────────────────────────",
      " <#2ecc71>▶ Left Click: <#a0aec0>Buy %removebuy%",
      " <#ff4757>▶ Right Click: <#a0aec0>Sell %removesell%",
      " ",
      " <#54a0ff>👤 Your Balance: <#2ecc71>%balance%"
    );

    shopInfoPermanent = new ItemModel(0, "minecraft:book", "§e✦ Permanent Shop", List.of(
      "§8─────────────────────",
      " §7Browse and purchase items",
      " §7from our permanent catalog.",
      "§8─────────────────────"
    ), 0);
    shopInfoDynamic = new ItemModel(1, "minecraft:clock", "§b⏰ Rotating Catalog", List.of(
      "§8─────────────────────",
      " §7Items in this catalog rotate",
      " §7automatically over time.",
      " ",
      " §7Next rotation in: §e%cooldown%",
      " §7Products shown: §a%number% §8/ §f%totalProducts%",
      "§8─────────────────────"
    ), 0);

    globalDisplay = new ItemModel(0, "cobblemon:poke_ball", "§a🏪 Shop: §f%shop%", List.of(""), 0);
    globalItemInfoShop = new ItemModel(0, "minecraft:book", "§e✦ Info", List.of("%info%"), 0);
    globalItemBalance = new ItemModel(0, "minecraft:emerald", "§6⛃ Your Balance", List.of("§7You currently have: §e%amount%"), 0);
    globalItemPrevious = new ItemModel(0, "minecraft:arrow", "§a« Go Back", List.of(""), 0);
    globalItemClose = new ItemModel(0, "minecraft:barrier", "§c✕ Close", List.of(""), 0);
    globalItemNext = new ItemModel(0, "minecraft:arrow", "§aNext page »", List.of(""), 0);

    add1 = new ItemModel(21, "item:1:minecraft:lime_stained_glass_pane", "<#2ecc71>Add 1", List.of(""), 0);
    add8 = new ItemModel(20, "item:8:minecraft:lime_stained_glass_pane", "<#2ecc71>Add 8", List.of(""), 0);
    add16 = new ItemModel(20, "item:16:minecraft:lime_stained_glass_pane", "<#2ecc71>Add 16", List.of(""), 0);
    add64 = new ItemModel(19, "item:64:minecraft:lime_stained_glass_pane", "<#2ecc71>Add 64", List.of(""), 0);
    addMax = new ItemModel(18, "minecraft:gold_block", "<#feca57>Max Amount", List.of("<#eccc68>Click to set to maximum"), 0);
    customAmount = new ItemModel(31, "minecraft:name_tag", "<#54a0ff>Custom Amount", List.of("<#70a1ff>Click to enter amount via chat"), 0);
    remove1 = new ItemModel(23, "item:1:minecraft:red_stained_glass_pane", "<#ff6b6b>Remove 1", List.of(""), 0);
    remove8 = new ItemModel(24, "item:8:minecraft:red_stained_glass_pane", "<#ff6b6b>Remove 8", List.of(""), 0);
    remove16 = new ItemModel(24, "item:16:minecraft:red_stained_glass_pane", "<#ff6b6b>Remove 16", List.of(""), 0);
    remove64 = new ItemModel(25, "item:64:minecraft:red_stained_glass_pane", "<#ff6b6b>Remove 64", List.of(""), 0);

    menuBuyAndSell = new BuyAndSellConfig();
  }

  public void check() {
    if (prefix == null) prefix = "<#2ecc71>« <#feca57><b>UltraShop</b> <#2ecc71>» §r";
    if (messageShopInMaintenance == null || messageShopInMaintenance.isBlank()) {
      messageShopInMaintenance = "%prefix% <#ff6b6b>The shop <#ffa502>%shop% <#ff6b6b>is currently closed for maintenance.";
    }
    if (messageChatPromptEnterAmount == null || messageChatPromptEnterAmount.isBlank()) {
      messageChatPromptEnterAmount = "Enter amount to buy/sell (or type 'cancel' to abort):";
    }
    if (messageChatPromptInvalidAmount == null || messageChatPromptInvalidAmount.isBlank()) {
      messageChatPromptInvalidAmount = "<#ff6b6b>Invalid amount. Please enter a positive integer.";
    }
    if (messageDailySellLimitReached == null || messageDailySellLimitReached.isBlank()) {
      messageDailySellLimitReached = "%prefix% <#ff6b6b>You have reached your daily sell limit of <#ffa502>%limit% %currency%<#ff6b6b>!";
    }
    if (messageSimpleBuy == null || messageSimpleBuy.isBlank()) {
      messageSimpleBuy = "%prefix% <#2ecc71>You have bought <#ffa502>%amount%x %product% <#2ecc71>for <#feca57>%price%";
    }
    if (messageYouCantSellNow == null || messageYouCantSellNow.isBlank()) {
      messageYouCantSellNow = "%prefix% <#ff6b6b>You reached your sell limit of <#ffa502>%limit%x <#ff6b6b>for this product. Cooldown: <#ffa502>%time%s";
    }
    if (messageShopDailySellLimitReached == null || messageShopDailySellLimitReached.isBlank()) {
      messageShopDailySellLimitReached = "%prefix% <#ff6b6b>You have reached the shop's daily sell limit of <#ffa502>%limit% %currency%<#ff6b6b>!";
    }
    if (cooldownReady == null || cooldownReady.isBlank()) {
      cooldownReady = "<#2ecc71>Ready";
    }
    if (messageOutOfStock == null || messageOutOfStock.isBlank()) {
      messageOutOfStock = "%prefix% <#ff6b6b>This product is out of stock";
    }
    if (messageNotEnoughStock == null || messageNotEnoughStock.isBlank()) {
      messageNotEnoughStock = "%prefix% <#ff6b6b>Not enough stock available. Remaining: <#ffa502>%remaining%";
    }
    if (messageInvalidNumber == null || messageInvalidNumber.isBlank()) {
      messageInvalidNumber = "%prefix% <#ff6b6b>Invalid number: <#ffa502>%input%";
    }
    if (messageConditionTypesUnavailable == null || messageConditionTypesUnavailable.isBlank()) {
      messageConditionTypesUnavailable = "%prefix% <#ff6b6b>Could not load condition types from CobbleUtils.";
    }
    if (messageConditionCreateFailed == null || messageConditionCreateFailed.isBlank()) {
      messageConditionCreateFailed = "%prefix% <#ff6b6b>Failed to create condition: <#ffa502>%error%";
    }
    if (commandReloaded == null || commandReloaded.isBlank()) commandReloaded = "%prefix% <#2ecc71>Reloaded <#ffa502>%modId% <#2ecc71>shops";
    if (commandReloadFailed == null || commandReloadFailed.isBlank()) commandReloadFailed = "%prefix% <#ff6b6b>Reload failed for <#ffa502>%modId%<#ff6b6b>: <#ffa502>%error%";
    if (commandShopAlreadyExists == null || commandShopAlreadyExists.isBlank()) commandShopAlreadyExists = "%prefix% <#ff6b6b>Shop already exists: <#ffa502>%shop%";
    if (commandShopCreated == null || commandShopCreated.isBlank()) commandShopCreated = "%prefix% <#2ecc71>Created shop: <#ffa502>%shop%";
    if (commandDynamicShopRestarted == null || commandDynamicShopRestarted.isBlank()) commandDynamicShopRestarted = "%prefix% <#2ecc71>Restarted dynamic shop: <#ffa502>%shop%";
    if (commandDynamicShopInvalid == null || commandDynamicShopInvalid.isBlank()) commandDynamicShopInvalid = "%prefix% <#ff6b6b>Shop is not dynamic or not found: <#ffa502>%shop%";
    if (commandShopNotFound == null || commandShopNotFound.isBlank()) commandShopNotFound = "%prefix% <#ff6b6b>Shop not found: <#ffa502>%shop%";
    if (commandShopDeleted == null || commandShopDeleted.isBlank()) commandShopDeleted = "%prefix% <#2ecc71>Deleted shop: <#ffa502>%shop%";
    if (commandShopDeleteError == null || commandShopDeleteError.isBlank()) commandShopDeleteError = "%prefix% <#ff6b6b>Error deleting shop: <#ffa502>%error%";
    if (commandNoRepository == null || commandNoRepository.isBlank()) commandNoRepository = "%prefix% <#ff6b6b>No repository available";
    if (commandNoTransactionsFound == null || commandNoTransactionsFound.isBlank()) commandNoTransactionsFound = "%prefix% <#a0aec0>No transactions found for <#ffa502>%player%";
    if (commandTransactionsHeader == null || commandTransactionsHeader.isBlank()) commandTransactionsHeader = " <#feca57>« <#2ecc71>Transactions for %player% <#feca57>»";
    if (commandTransactionLine == null || commandTransactionLine.isBlank()) commandTransactionLine = " <#a0aec0>[%date%] %action% <#a0aec0>x%amount% <#ffa502>%product% <#a0aec0>(%price% %currency%)";
    if (commandStatsConsoleTitle == null || commandStatsConsoleTitle.isBlank()) commandStatsConsoleTitle = " <#feca57>« <#2ecc71>UltraShop Stats (30d) <#feca57>»";
    if (commandStatsConsoleTransactions == null || commandStatsConsoleTransactions.isBlank()) commandStatsConsoleTransactions = " <#a0aec0>• Transactions: <#ffffff>%value%";
    if (commandStatsConsolePlayers == null || commandStatsConsolePlayers.isBlank()) commandStatsConsolePlayers = " <#a0aec0>• Players: <#ffffff>%value%";
    if (commandStatsConsoleRevenue == null || commandStatsConsoleRevenue.isBlank()) commandStatsConsoleRevenue = " <#a0aec0>• Revenue: <#2ecc71>$%value%";
    if (commandStatsConsolePayouts == null || commandStatsConsolePayouts.isBlank()) commandStatsConsolePayouts = " <#a0aec0>• Payouts: <#ff6b6b>$%value%";
    if (commandStatsConsoleNet == null || commandStatsConsoleNet.isBlank()) commandStatsConsoleNet = " <#a0aec0>• Net Profit: <#feca57>$%value%";
    if (transactionMenuTitle == null || transactionMenuTitle.isBlank()) transactionMenuTitle = "<#feca57>Transactions: <#ffffff>%player%";
    if (transactionBuyLabel == null || transactionBuyLabel.isBlank()) transactionBuyLabel = "<#2ecc71>BUY";
    if (transactionSellLabel == null || transactionSellLabel.isBlank()) transactionSellLabel = "<#ff6b6b>SELL";
    if (transactionDateLabel == null || transactionDateLabel.isBlank()) transactionDateLabel = "<#a0aec0>Date: <#ffffff>%value%";
    if (transactionShopLabel == null || transactionShopLabel.isBlank()) transactionShopLabel = "<#a0aec0>Shop: <#ffffff>%value%";
    if (transactionProductLabel == null || transactionProductLabel.isBlank()) transactionProductLabel = "<#a0aec0>Product: <#ffffff>%value%";
    if (transactionAmountLabel == null || transactionAmountLabel.isBlank()) transactionAmountLabel = "<#a0aec0>Amount: <#ffffff>%value%";
    if (transactionPriceLabel == null || transactionPriceLabel.isBlank()) transactionPriceLabel = "<#a0aec0>Price: <#feca57>%value%";
    if (statsMenuTitle == null || statsMenuTitle.isBlank()) statsMenuTitle = "<#feca57><b>UltraShop Stats</b> <#a0aec0>(30 days)";
    if (statsServerOverviewTitle == null || statsServerOverviewTitle.isBlank()) statsServerOverviewTitle = "<#feca57>⚡ <b>Server Overview</b> <#a0aec0>(30d)";
    if (statsPlayerOverviewTitle == null || statsPlayerOverviewTitle.isBlank()) statsPlayerOverviewTitle = "<#54a0ff>👤 <b>Your Stats</b> <#a0aec0>(30d)";
    if (statsShopBreakdownTitle == null || statsShopBreakdownTitle.isBlank()) statsShopBreakdownTitle = "<#feca57>🏪 <b>Shop Breakdown</b> <#a0aec0>(30d)";
    if (statsTotalTransactionsLabel == null || statsTotalTransactionsLabel.isBlank()) statsTotalTransactionsLabel = "<#a0aec0>Total Transactions: <#ffffff>%value%";
    if (statsUniquePlayersLabel == null || statsUniquePlayersLabel.isBlank()) statsUniquePlayersLabel = "<#a0aec0>Unique Players: <#ffffff>%value%";
    if (statsRevenueLabel == null || statsRevenueLabel.isBlank()) statsRevenueLabel = "<#a0aec0>Revenue (buys): <#2ecc71>$%value%";
    if (statsPayoutLabel == null || statsPayoutLabel.isBlank()) statsPayoutLabel = "<#a0aec0>Payouts (sells): <#ff6b6b>$%value%";
    if (statsNetProfitLabel == null || statsNetProfitLabel.isBlank()) statsNetProfitLabel = "<#a0aec0>Net Profit: <#feca57>$%value%";
    if (statsItemsBoughtLabel == null || statsItemsBoughtLabel.isBlank()) statsItemsBoughtLabel = "<#a0aec0>Items Bought: <#ffffff>%value%";
    if (statsItemsSoldLabel == null || statsItemsSoldLabel.isBlank()) statsItemsSoldLabel = "<#a0aec0>Items Sold: <#ffffff>%value%";
    if (statsTotalSpentLabel == null || statsTotalSpentLabel.isBlank()) statsTotalSpentLabel = "<#a0aec0>Total Spent: <#ff6b6b>$%value%";
    if (statsTotalEarnedLabel == null || statsTotalEarnedLabel.isBlank()) statsTotalEarnedLabel = "<#a0aec0>Total Earned: <#2ecc71>$%value%";
    if (statsNoData == null || statsNoData.isBlank()) statsNoData = "<#a0aec0>No data yet";
    if (statsShopBreakdownEntry == null || statsShopBreakdownEntry.isBlank()) statsShopBreakdownEntry = "<#feca57>%shop%<#a0aec0>: <#2ecc71>$%revenue% <#a0aec0>/ <#ff6b6b>$%payout% <#718096>(%transactions% tx)";
    if (statsTopProductTitle == null || statsTopProductTitle.isBlank()) statsTopProductTitle = "<#feca57>#%rank% <#ffffff>%product%";
    if (statsTopProductShopLabel == null || statsTopProductShopLabel.isBlank()) statsTopProductShopLabel = "<#a0aec0>Shop: <#ffffff>%value%";
    if (statsTopProductRankLabel == null || statsTopProductRankLabel.isBlank()) statsTopProductRankLabel = "<#a0aec0>Rank: <#feca57>#%value%";
    if (statsTopProductBoughtLabel == null || statsTopProductBoughtLabel.isBlank()) statsTopProductBoughtLabel = "<#a0aec0>Bought: <#54a0ff>%amount%x <#a0aec0>by <#ffffff>%players% <#a0aec0>players";
    if (statsTopProductSoldLabel == null || statsTopProductSoldLabel.isBlank()) statsTopProductSoldLabel = "<#a0aec0>Sold: <#ff4757>%amount%x <#a0aec0>by <#ffffff>%players% <#a0aec0>players";
    if (statsTopProductUniquePlayersLabel == null || statsTopProductUniquePlayersLabel.isBlank()) statsTopProductUniquePlayersLabel = "<#a0aec0>Unique Players: <#ffffff>%value%";
    if (editorTitleShopList == null || editorTitleShopList.isBlank()) editorTitleShopList = "<#feca57><b>Shop Editor</b>";
    if (editorTitleProductList == null || editorTitleProductList.isBlank()) editorTitleProductList = "<#feca57><b>Products:</b> <#feca57>%shop%";
    if (editorTitleProductEdit == null || editorTitleProductEdit.isBlank()) editorTitleProductEdit = "<#feca57><b>Edit:</b> <#feca57>%product%";
    if (editorTitleShopSettings == null || editorTitleShopSettings.isBlank()) editorTitleShopSettings = "<#feca57><b>Settings:</b> <#feca57>%shop%";
    if (editorTitleInventoryPicker == null || editorTitleInventoryPicker.isBlank()) editorTitleInventoryPicker = "<#feca57><b>Pick Item from Inventory</b>";
    if (editorTitleInventoryPickerEdit == null || editorTitleInventoryPickerEdit.isBlank()) editorTitleInventoryPickerEdit = "<#feca57><b>Pick Item for:</b> <#feca57>%product%";
    if (editorTitleConditionList == null || editorTitleConditionList.isBlank()) editorTitleConditionList = "<#feca57><b>Conditions:</b> <#feca57>%shop%";
    if (editorTitleAddCondition == null || editorTitleAddCondition.isBlank()) editorTitleAddCondition = "<#feca57><b>Add Condition</b>";
    if (editorTitleProductConditionList == null || editorTitleProductConditionList.isBlank()) editorTitleProductConditionList = "<#feca57><b>%label% Conditions:</b> <#feca57>%product%";
    if (editorButtonClose == null || editorButtonClose.isBlank()) editorButtonClose = "<#ff6b6b>✕ Close";
    if (editorButtonBack == null || editorButtonBack.isBlank()) editorButtonBack = "<#a0aec0>← Back";
    if (editorButtonStockControl == null || editorButtonStockControl.isBlank()) editorButtonStockControl = "<#54a0ff>📦 Stock Control";
    if (editorPromptExactBuyPrice == null || editorPromptExactBuyPrice.isBlank()) editorPromptExactBuyPrice = "Enter exact buy price:";
    if (editorPromptExactSellPrice == null || editorPromptExactSellPrice.isBlank()) editorPromptExactSellPrice = "Enter exact sell price:";
    if (sellGuiTitle == null || sellGuiTitle.isBlank()) sellGuiTitle = "<#54a0ff>Sell GUI - Place items to sell";
    if (messageShopNotFound == null || messageShopNotFound.isBlank()) messageShopNotFound = "<#ff6b6b>Shop not found: <#feca57>%shop%";
    if (messageErrorParsingArgs == null || messageErrorParsingArgs.isBlank()) messageErrorParsingArgs = "<#ff6b6b>Error parsing command arguments.";
    if (messagePropertiesCannotBeEmpty == null || messagePropertiesCannotBeEmpty.isBlank()) messagePropertiesCannotBeEmpty = "<#ff6b6b>Properties cannot be empty.";
    if (messageAddedPokemon == null || messageAddedPokemon.isBlank()) messageAddedPokemon = "<#2ecc71>Added Pokémon: <#54a0ff>%pokemon% <#2ecc71>to shop: <#feca57>%shop%";
    if (messageShopMaintenanceStatus == null || messageShopMaintenanceStatus.isBlank()) messageShopMaintenanceStatus = "%prefix% <#a0aec0>Shop <#feca57>%shop% <#a0aec0>is now %status%";
    if (shopStatusOpen == null || shopStatusOpen.isBlank()) shopStatusOpen = "<#2ecc71>OPEN";
    if (shopStatusClosed == null || shopStatusClosed.isBlank()) shopStatusClosed = "<#ff6b6b>CLOSED (Maintenance)";
    if (titleSearch == null || titleSearch.isBlank()) titleSearch = "<#feca57><b>Search:</b> <#ffffff>%query%";

    if (editorTitleSubShopList == null || editorTitleSubShopList.isBlank()) editorTitleSubShopList = "<#feca57><b>Sub-Shops:</b> <#feca57>%shop%";
    if (editorTitleSelectShopType == null || editorTitleSelectShopType.isBlank()) editorTitleSelectShopType = "<#feca57><b>Select Shop Type</b>";
    if (editorTitleConfirmWarning == null || editorTitleConfirmWarning.isBlank()) editorTitleConfirmWarning = "<#ff6b6b><b>WARNING</b>";
    if (editorTitleAddPokemonOptions == null || editorTitleAddPokemonOptions.isBlank()) editorTitleAddPokemonOptions = "<#54a0ff><b>Add Pokémon Options</b>";
    if (editorTitleRectangle == null || editorTitleRectangle.isBlank()) editorTitleRectangle = "<#feca57><b>Rectangle Settings</b>";
    if (editorTitleEconomies == null || editorTitleEconomies.isBlank()) editorTitleEconomies = "<#feca57><b>Shop Economies</b>";
    if (editorTitleDiscounts == null || editorTitleDiscounts.isBlank()) editorTitleDiscounts = "<#feca57><b>Permission Discounts</b>";
    if (editorTitleProductPrices == null || editorTitleProductPrices.isBlank()) editorTitleProductPrices = "<#feca57><b>Prices:</b> <#feca57>%product%";

    if (editorChatInputPrefix == null || editorChatInputPrefix.isBlank()) editorChatInputPrefix = "<#feca57>[UltraShop Editor] <#f5f6fa>";
    if (editorChatInputPrompt == null || editorChatInputPrompt.isBlank()) editorChatInputPrompt = "<#a0aec0>Type your value in chat. Type <#ff6b6b>cancel <#a0aec0>to abort.";
    if (editorChatInputCancelled == null || editorChatInputCancelled.isBlank()) editorChatInputCancelled = "<#ff6b6b>Input cancelled.";
    if (editorChatInputError == null || editorChatInputError.isBlank()) editorChatInputError = "<#ff6b6b>Error: %error%";
    if (messagePokemonAddedFromParty == null || messagePokemonAddedFromParty.isBlank()) messagePokemonAddedFromParty = "<#2ecc71>Added Pokémon from Party/PC: <#54a0ff>%pokemon%";
    if (messageErrorOpeningPartyPc == null || messageErrorOpeningPartyPc.isBlank()) messageErrorOpeningPartyPc = "<#ff6b6b>Error opening Party/PC menu: %error%";
    if (editorInvalidId == null || editorInvalidId.isBlank()) editorInvalidId = "<#ff6b6b>Invalid ID.";
    if (editorHoldItemInHand == null || editorHoldItemInHand.isBlank()) editorHoldItemInHand = "<#ff6b6b>Hold an item in your hand first.";
    if (editorInvalidSlotsInput == null || editorInvalidSlotsInput.isBlank()) editorInvalidSlotsInput = "<#ff6b6b>Invalid slots input. Must be numbers between 0 and %max%";
    if (editorInvalidFormat == null || editorInvalidFormat.isBlank()) editorInvalidFormat = "<#ff6b6b>Invalid format or number: %input%";
    if (editorInvalidKeyValueFormat == null || editorInvalidKeyValueFormat.isBlank()) editorInvalidKeyValueFormat = "<#ff6b6b>Invalid format. Use key:value.";


    if (infoProduct == null || infoProduct.isEmpty()) {
      infoProduct = List.of(
        "%info%",
        "§8─────────────────────────────",
        " <#a0aec0>📦 Quantity: <#54a0ff>%amount%x <#718096>(Pack: %pack%x)",
        " ",
        " <#a0aec0>💰 Buy: <#2ecc71>%buy% %removebuy%",
        " <#a0aec0>🏷️ Discount: <#ffa502>%discount% %removediscount%",
        " <#a0aec0>💵 Sell: <#ff4757>%sell% %removesell%",
        " ",
        " <#a0aec0>📦 Stock: <#74b9ff>%stock_remaining%<#a0aec0>/<#f4d03f>%stock_limit% §7(%stock_mode%) %removestock%",
        " <#a0aec0>⏱ Cooldown: <#ffa502>%cooldown_time% %removelimit%",
        "§8─────────────────────────────",
        " <#2ecc71>▶ Left Click: <#a0aec0>Buy %removebuy%",
        " <#ff4757>▶ Right Click: <#a0aec0>Sell %removesell%",
        " ",
        " <#54a0ff>👤 Your Balance: <#2ecc71>%balance%"
      );
    }
    if (shopInfoPermanent == null) {
      shopInfoPermanent = new ItemModel(0, "minecraft:book", "§e✦ Permanent Shop", List.of(
        "§8─────────────────────",
        " §7Browse and purchase items",
        " §7from our permanent catalog.",
        "§8─────────────────────"
      ), 0);
    }
    if (shopInfoDynamic == null) {
      shopInfoDynamic = new ItemModel(1, "minecraft:clock", "§b⏰ Rotating Catalog", List.of(
        "§8─────────────────────",
        " §7Items in this catalog rotate",
        " §7automatically over time.",
        " ",
        " §7Next rotation in: §e%cooldown%",
        " §7Products shown: §a%number% §8/ §f%totalProducts%",
        "§8─────────────────────"
      ), 0);
    }
    if (globalDisplay == null) {
      globalDisplay = new ItemModel(0, "cobblemon:poke_ball", "§a🏪 Shop: §f%shop%", List.of(""), 0);
    }
    if (globalItemInfoShop == null) {
      globalItemInfoShop = new ItemModel(0, "minecraft:book", "§e✦ Info", List.of("%info%"), 0);
    }
    if (globalItemBalance == null) {
      globalItemBalance = new ItemModel(0, "minecraft:emerald", "§6⛃ Your Balance", List.of("§7You currently have: §e%amount%"), 0);
    }
    if (globalItemPrevious == null) {
      globalItemPrevious = new ItemModel(0, "minecraft:arrow", "§a« Go Back", List.of(""), 0);
    }
    if (globalItemClose == null) {
      globalItemClose = new ItemModel(0, "minecraft:barrier", "§c✕ Close", List.of(""), 0);
    }
    if (globalItemNext == null) {
      globalItemNext = new ItemModel(0, "minecraft:arrow", "§aNext page »", List.of(""), 0);
    }
    if (add1 == null) add1 = new ItemModel(21, "item:1:minecraft:lime_stained_glass_pane", "<#2ecc71>Add 1", List.of(""), 0);
    if (add8 == null) add8 = new ItemModel(20, "item:8:minecraft:lime_stained_glass_pane", "<#2ecc71>Add 8", List.of(""), 0);
    if (add16 == null) add16 = new ItemModel(20, "item:16:minecraft:lime_stained_glass_pane", "<#2ecc71>Add 16", List.of(""), 0);
    if (add64 == null) add64 = new ItemModel(19, "item:64:minecraft:lime_stained_glass_pane", "<#2ecc71>Add 64", List.of(""), 0);
    if (addMax == null) addMax = new ItemModel(18, "minecraft:gold_block", "<#feca57>Max Amount", List.of("<#eccc68>Click to set to maximum"), 0);
    if (customAmount == null) customAmount = new ItemModel(31, "minecraft:name_tag", "<#54a0ff>Custom Amount", List.of("<#70a1ff>Click to enter amount via chat"), 0);
    if (remove1 == null) remove1 = new ItemModel(23, "item:1:minecraft:red_stained_glass_pane", "<#ff6b6b>Remove 1", List.of(""), 0);
    if (remove8 == null) remove8 = new ItemModel(24, "item:8:minecraft:red_stained_glass_pane", "<#ff6b6b>Remove 8", List.of(""), 0);
    if (remove16 == null) remove16 = new ItemModel(24, "item:16:minecraft:red_stained_glass_pane", "<#ff6b6b>Remove 16", List.of(""), 0);
    if (remove64 == null) remove64 = new ItemModel(25, "item:64:minecraft:red_stained_glass_pane", "<#ff6b6b>Remove 64", List.of(""), 0);
    if (menuBuyAndSell == null) {
      menuBuyAndSell = new BuyAndSellConfig();
    } else {
      menuBuyAndSell.check();
    }
  }

  /**
   * Resolves an item model with a fallback if the override is null or empty.
   */
  public static ItemModel resolve(ItemModel override, ItemModel fallback) {
    if (override == null) return fallback;
    String item = override.getItem();
    if (item == null || item.isEmpty()) return fallback;
    return override;
  }
}

