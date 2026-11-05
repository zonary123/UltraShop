package com.kingpixel.ultrashop.presentation.gui.edit;

import ca.landonjw.gooeylibs2.api.UIManager;
import ca.landonjw.gooeylibs2.api.button.Button;
import ca.landonjw.gooeylibs2.api.button.GooeyButton;
import ca.landonjw.gooeylibs2.api.helpers.PaginationHelper;
import ca.landonjw.gooeylibs2.api.page.GooeyPage;
import ca.landonjw.gooeylibs2.api.page.LinkedPage;
import ca.landonjw.gooeylibs2.api.template.types.ChestTemplate;
import com.kingpixel.cobbleutils.Model.Rectangle;
import com.kingpixel.cobbleutils.Model.conditions.Condition;
import com.kingpixel.cobbleutils.util.AdventureTranslator;
import com.kingpixel.cobbleutils.Model.EconomyUse;
import com.kingpixel.cobbleutils.ui.conditions.ConditionListMenu;
import com.kingpixel.cobbleutils.util.PlayerUtils;
import com.kingpixel.cobbleutils.util.TypeMessage;
import com.kingpixel.ultrashop.ShopContext;
import com.kingpixel.ultrashop.domain.model.PriceEntry;
import com.kingpixel.ultrashop.domain.model.Product;
import com.kingpixel.ultrashop.domain.model.StockMode;
import com.kingpixel.ultrashop.domain.model.shop.RotationShop;
import com.kingpixel.ultrashop.domain.model.shop.Shop;
import com.kingpixel.ultrashop.infrastructure.config.ConfigLoader;
import com.kingpixel.ultrashop.infrastructure.config.LangConfig;
import com.kingpixel.ultrashop.infrastructure.config.ShopConfig;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.LoreComponent;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Unit;

import net.minecraft.registry.Registries;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import static com.kingpixel.ultrashop.presentation.gui.edit.EditorHelpers.*;

public final class ProductEditor {

  private ProductEditor() {}

  public static void openProductEditor(ServerPlayerEntity player, Shop shop, Product product,
                                       ShopConfig config, String modId) {
    ShopContext ctx = ShopContext.get();
    LangConfig lang = ctx.getLang();
    ChestTemplate template = ChestTemplate.builder(6).build();

    ItemStack icon;
    try {
      icon = product.getItemStack();
      if (icon.isEmpty()) icon = new ItemStack(Items.BARRIER);
    } catch (Exception e) {
      icon = new ItemStack(Items.BARRIER);
    }
    List<String> previewLore = new ArrayList<>();
    previewLore.add(SEP);
    previewLore.add("§7Type: " + productTypeTag(product));
    previewLore.add("§7ID: §8" + truncate(product.getProduct(), 40));
    previewLore.add("");

    if (product.getPrices() != null && !product.getPrices().isEmpty()) {
      previewLore.add("§e⛃ Multi-Currency");
      for (PriceEntry pe : product.getPrices()) {
        String eco = pe.getEconomy() != null ? pe.getEconomy().getCurrency() : "?";
        previewLore.add("  §7" + eco + ": §aBuy " + fmt(pe.getBuy()) + " §c Sell " + fmt(pe.getSell()));
      }
    } else {
      previewLore.add("§a⬆ Buy: §f" + fmt(product.getBuy()) + (product.isBuyable() ? " §a✓" : " §c✗"));
      previewLore.add("§c⬇ Sell: §f" + fmt(product.getSell()) + (product.isSellable() ? " §a✓" : " §c✗"));
    }
    if (product.getDiscount() != null) previewLore.add("§e✦ Discount: §f" + product.getDiscount() + "%");

    previewLore.add("");
    previewLore.add("§f◆ Display");
    previewLore.add("  §7Name: §f" + (product.getDisplayname() != null ? product.getDisplayname() : "§8auto"));
    previewLore.add("  §7Item: §f" + (product.getDisplay() != null ? product.getDisplay() : "§8none"));
    previewLore.add("  §7CMD: §f" + (product.getCustomModelData() != null ? product.getCustomModelData() : "§8none"));

    if (product.getLore() != null && !product.getLore().isEmpty()) {
      previewLore.add("  §7Lore: §f" + product.getLore().size() + " lines");
      for (int li = 0; li < Math.min(product.getLore().size(), 3); li++) {
        previewLore.add("    §8" + truncate(product.getLore().get(li), 35));
      }
      if (product.getLore().size() > 3) previewLore.add("    §8...");
    }

    previewLore.add("");
    previewLore.add("§f◆ Settings");
    previewLore.add("  §7Slot: §f" + (product.getSlot() != null ? product.getSlot() : "§8auto"));
    previewLore.add("  §7OneByOne: " + boolIcon(Boolean.TRUE.equals(product.getOneByOne())));
    previewLore.add("  §7Stack: §f" + safeMaxStack(product));
    if (product.getMax() != null) {
      previewLore.add("  §7Limit: §f" + product.getMax() + " §7every §f" + product.getCooldown());
      previewLore.add("  §7UUID: §8" + (product.getUuid() != null ? product.getUuid().toString().substring(0, 8) + "..." : "none"));
    }
    if (product.getSellMax() != null) {
      previewLore.add("  §7Sell Limit: §f" + product.getSellMax() + " §7every §f" + product.getSellCooldown());
      previewLore.add("  §7Sell UUID: §8" + (product.getSellUuid() != null ? product.getSellUuid().toString().substring(0, 8) + "..." : "none"));
    }
    if (product.hasStockControl()) {
      previewLore.add("  §7Stock: §f" + product.getStockAmount() + " §8(" + product.getStockMode() + ")");
    }
    if (product.getChance() != null) {
      previewLore.add("  §7Rotation Chance: §f" + product.getChance() + "%");
    }

    if (product.getConditions() != null && !product.getConditions().isEmpty()) {
      previewLore.add("");
      previewLore.add("§c⚡ Buy Conditions §7(" + product.getConditions().size() + ")");
      for (var cond : product.getConditions()) {
        previewLore.add("  §7• §f" + cond.getType());
      }
    }
    if (product.getVisibilityConditions() != null && !product.getVisibilityConditions().isEmpty()) {
      previewLore.add("§c👁 Visibility §7(" + product.getVisibilityConditions().size() + ")");
      for (var cond : product.getVisibilityConditions()) {
        previewLore.add("  §7• §f" + cond.getType());
      }
    }

    if (product.hasErrors()) {
      previewLore.add("");
      previewLore.add("§c§l⚠ ERROR: Sell price > Buy price!");
    }

    previewLore.add(SEP);
    template.set(4, button(icon, "§e§l" + truncate(product.getProduct(), 25), previewLore, a -> {
    }));

    template.set(9, GooeyButton.builder().display(new ItemStack(Items.EMERALD))
      .with(DataComponentTypes.CUSTOM_NAME, AdventureTranslator.toNative("§a§l--- Buy Price ---"))
      .with(DataComponentTypes.LORE, new LoreComponent(AdventureTranslator.toNativeL(List.of(
        "§7Current: §a" + fmt(product.getBuy()),
        product.isBuyable() ? "§a✓ Buyers can purchase this" : "§c✗ Not buyable (price is 0)"
      )))).build());
    template.set(10, priceBtn("§a+100", product::getBuy, v -> product.setBuy(v), 100, shop, player, product, config, modId));
    template.set(11, priceBtn("§a+10", product::getBuy, v -> product.setBuy(v), 10, shop, player, product, config, modId));
    template.set(12, priceBtn("§a+1", product::getBuy, v -> product.setBuy(v), 1, shop, player, product, config, modId));
    template.set(13, priceBtn("§c-1", product::getBuy, v -> product.setBuy(v), -1, shop, player, product, config, modId));
    template.set(14, priceBtn("§c-10", product::getBuy, v -> product.setBuy(v), -10, shop, player, product, config, modId));
    template.set(15, priceBtn("§c-100", product::getBuy, v -> product.setBuy(v), -100, shop, player, product, config, modId));
    template.set(16, button(new ItemStack(Items.OAK_SIGN), "§a✎ Set Exact Buy Price",
      List.of("§7Current: §a" + fmt(product.getBuy()), "", "§7Type a number in chat."),
      a -> ChatInputManager.requestInput(player, lang.getEditorPromptExactBuyPrice(), input -> {
        try {
          product.setBuy(new BigDecimal(input));
          ConfigLoader.saveShop(shop);
          ctx.runOnServer(() -> openProductEditor(player, shop, product, config, modId));
        } catch (NumberFormatException e) {
          sendConfiguredMessage(player, lang.getMessageInvalidNumber().replace("%input%", input));
        }
      })));

    template.set(18, GooeyButton.builder().display(new ItemStack(Items.REDSTONE))
      .with(DataComponentTypes.CUSTOM_NAME, AdventureTranslator.toNative("§c§l--- Sell Price ---"))
      .with(DataComponentTypes.LORE, new LoreComponent(AdventureTranslator.toNativeL(List.of(
        "§7Current: §c" + fmt(product.getSell()),
        product.isSellable() ? "§a✓ Players can sell this" : "§c✗ Not sellable (price is 0)",
        product.canBeSold() ? "" : "§8(commands/pokemon/multi-items can't be sold)"
      )))).build());
    template.set(19, priceBtn("§a+100", product::getSell, v -> product.setSell(v), 100, shop, player, product, config, modId));
    template.set(20, priceBtn("§a+10", product::getSell, v -> product.setSell(v), 10, shop, player, product, config, modId));
    template.set(21, priceBtn("§a+1", product::getSell, v -> product.setSell(v), 1, shop, player, product, config, modId));
    template.set(22, priceBtn("§c-1", product::getSell, v -> product.setSell(v), -1, shop, player, product, config, modId));
    template.set(23, priceBtn("§c-10", product::getSell, v -> product.setSell(v), -10, shop, player, product, config, modId));
    template.set(24, priceBtn("§c-100", product::getSell, v -> product.setSell(v), -100, shop, player, product, config, modId));
    template.set(25, button(new ItemStack(Items.OAK_SIGN), "§c✎ Set Exact Sell Price",
      List.of("§7Current: §c" + fmt(product.getSell()), "", "§7Type a number in chat."),
      a -> ChatInputManager.requestInput(player, lang.getEditorPromptExactSellPrice(), input -> {
        try {
          product.setSell(new BigDecimal(input));
          ConfigLoader.saveShop(shop);
          ctx.runOnServer(() -> openProductEditor(player, shop, product, config, modId));
        } catch (NumberFormatException e) {
          sendConfiguredMessage(player, lang.getMessageInvalidNumber().replace("%input%", input));
        }
      })));

    template.set(27, button(new ItemStack(Items.NAME_TAG), "§e✎ Display Name",
      List.of(SEP,
        "§7Current: §f" + (product.getDisplayname() != null ? product.getDisplayname() : "§8auto (from item)"),
        "",
        "§7Overrides the item name shown in the shop.",
        "§7Supports §6& §7color codes and §6<#hex> §7format.",
        SEP,
        "§a▶ Click §7→ Set via chat",
        "§c▶ Shift §7→ Clear (use item name)"),
      a -> {
        if (a.getClickType().name().contains("SHIFT")) {
          product.setDisplayname(null);
          ConfigLoader.saveShop(shop);
          openProductEditor(player, shop, product, config, modId);
        } else ChatInputManager.requestInput(player, "Enter display name (supports & color codes):", input -> {
          product.setDisplayname(input);
          ConfigLoader.saveShop(shop);
          ctx.runOnServer(() -> openProductEditor(player, shop, product, config, modId));
        });
      }));

    template.set(28, button(new ItemStack(Items.PAINTING), "§e✎ Display Item",
      List.of(SEP,
        "§7Current: §f" + (product.getDisplay() != null ? product.getDisplay() : "§8none (uses product item)"),
        "",
        "§7Overrides the icon shown in the shop GUI.",
        "§7Useful for commands/pokemon to show a",
        "§7representative item instead of barrier.",
        SEP,
        "§a▶ Left §7→ Set from hand",
        "§e▶ Right §7→ Set via chat",
        "§c▶ Shift §7→ Clear"),
      a -> {
        switch (a.getClickType()) {
          case SHIFT_LEFT_CLICK, SHIFT_RIGHT_CLICK -> {
            product.setDisplay(null);
            ConfigLoader.saveShop(shop);
            openProductEditor(player, shop, product, config, modId);
          }
          case RIGHT_CLICK -> ChatInputManager.requestInput(player, "Enter display item ID:", input -> {
            product.setDisplay(input);
            ConfigLoader.saveShop(shop);
            ctx.runOnServer(() -> openProductEditor(player, shop, product, config, modId));
          });
          default -> {
            ItemStack hand = player.getMainHandStack();
            if (!hand.isEmpty()) {
              product.setDisplay(Registries.ITEM.getId(hand.getItem()).toString());
              ConfigLoader.saveShop(shop);
              openProductEditor(player, shop, product, config, modId);
            }
          }
        }
      }));

    template.set(29, button(new ItemStack(Items.GOLD_NUGGET), "§e✦ Discount",
      List.of(SEP,
        "§7Current: §e" + (product.getDiscount() != null ? product.getDiscount() + "%" : "§8none"),
        "",
        "§7Per-product discount applied to buy price.",
        "§7Stacks with shop's global discount.",
        SEP,
        "§a▶ Left §7→ +5%",
        "§c▶ Right §7→ -5%",
        "§e▶ Shift §7→ Clear"),
      a -> {
        switch (a.getClickType()) {
          case SHIFT_LEFT_CLICK, SHIFT_RIGHT_CLICK -> {
            product.setDiscount(null);
            ConfigLoader.saveShop(shop);
            openProductEditor(player, shop, product, config, modId);
          }
          case LEFT_CLICK -> {
            float c = product.getDiscount() != null ? product.getDiscount() : 0f;
            product.setDiscount(Math.min(c + 5f, 100f));
            ConfigLoader.saveShop(shop);
            openProductEditor(player, shop, product, config, modId);
          }
          default -> {
            float c = product.getDiscount() != null ? product.getDiscount() : 0f;
            float n = Math.max(c - 5f, 0f);
            product.setDiscount(n > 0f ? n : null);
            ConfigLoader.saveShop(shop);
            openProductEditor(player, shop, product, config, modId);
          }
        }
      }));

    template.set(30, button(new ItemStack(Boolean.TRUE.equals(product.getOneByOne()) ? Items.IRON_BARS : Items.GRAY_DYE),
      "§eOneByOne: " + boolIcon(Boolean.TRUE.equals(product.getOneByOne())),
      List.of(SEP,
        "§7Current: " + boolIcon(Boolean.TRUE.equals(product.getOneByOne())),
        "",
        "§7When enabled, forces stack size to 1.",
        "§7Players buy/sell one at a time.",
        "§7Auto-enabled for pokemon & commands.",
        SEP,
        "§a▶ Click §7→ Toggle"),
      a -> {
        product.setOneByOne(Boolean.TRUE.equals(product.getOneByOne()) ? null : true);
        ConfigLoader.saveShop(shop);
        openProductEditor(player, shop, product, config, modId);
      }));

    if (shop instanceof RotationShop) {
      template.set(31, button(new ItemStack(Items.RABBIT_FOOT), "§d⟳ Rotation Chance",
        List.of(SEP,
          "§7Current: §d" + (product.getChance() != null ? product.getChance() + "%" : "§f100% §8(default)"),
          "",
          "§7Weight for dynamic rotation selection.",
          "§7Higher = more likely to appear.",
          SEP,
          "§a▶ Left §7→ +10",
          "§c▶ Right §7→ -10",
          "§e▶ Shift §7→ Clear (100%)"),
        a -> {
          switch (a.getClickType()) {
            case SHIFT_LEFT_CLICK, SHIFT_RIGHT_CLICK -> {
              product.setChance(null);
              ConfigLoader.saveShop(shop);
              openProductEditor(player, shop, product, config, modId);
            }
            case LEFT_CLICK -> {
              int c = product.getChance() != null ? product.getChance() : 100;
              product.setChance(c + 10);
              ConfigLoader.saveShop(shop);
              openProductEditor(player, shop, product, config, modId);
            }
            default -> {
              int c = product.getChance() != null ? product.getChance() : 100;
              product.setChance(Math.max(c - 10, 1));
              ConfigLoader.saveShop(shop);
              openProductEditor(player, shop, product, config, modId);
            }
          }
        }));
    }

    template.set(36, button(new ItemStack(Items.IRON_DOOR), "§6⏱ Max Purchases",
      List.of(SEP,
        "§7Max: §f" + (product.getMax() != null ? product.getMax() : "§8unlimited"),
        "§7Cooldown: §f" + (product.getCooldown() != null ? product.getCooldown() : "§8none"),
        "§7UUID: §8" + (product.getUuid() != null ? product.getUuid().toString().substring(0, 8) + "..." : "auto-generated"),
        "",
        "§7Limits how many times a player can buy.",
        "§7Resets after the cooldown period.",
        "§8(Clears permanent stock if active)",
        SEP,
        "§a▶ Left §7→ +1",
        "§c▶ Right §7→ -1",
        "§a▶ Shift+Left §7→ +10",
        "§c▶ Shift+Right §7→ Clear (unlimited)"),
      a -> {
        switch (a.getClickType()) {
          case SHIFT_RIGHT_CLICK -> {
            product.setMax(null);
            product.setCooldown(null);
            product.setUuid(null);
            ConfigLoader.saveShop(shop);
            openProductEditor(player, shop, product, config, modId);
          }
          case SHIFT_LEFT_CLICK -> {
            product.setMax((product.getMax() != null ? product.getMax() : 0) + 10);
            if (product.getCooldown() == null) product.setCooldown("60m");
            product.setStockAmount(null);
            product.setStockMode(null);
            ConfigLoader.saveShop(shop);
            openProductEditor(player, shop, product, config, modId);
          }
          case LEFT_CLICK -> {
            product.setMax((product.getMax() != null ? product.getMax() : 0) + 1);
            if (product.getCooldown() == null) product.setCooldown("60m");
            product.setStockAmount(null);
            product.setStockMode(null);
            ConfigLoader.saveShop(shop);
            openProductEditor(player, shop, product, config, modId);
          }
          default -> {
            if (product.getMax() != null && product.getMax() > 1) {
              product.setMax(product.getMax() - 1);
            } else {
              product.setMax(null);
              product.setCooldown(null);
              product.setUuid(null);
            }
            ConfigLoader.saveShop(shop);
            openProductEditor(player, shop, product, config, modId);
          }
        }
      }));

    template.set(37, button(new ItemStack(Items.CLOCK), "§6⏱ Cooldown (duration/cron)",
      List.of(SEP,
        "§7Current: §f" + (product.getCooldown() != null ? product.getCooldown() : "§8none"),
        "",
        "§7Time before the purchase limit resets.",
        "§7Requires §fMax Purchases §7to be set.",
        SEP,
        "§a▶ Left §7→ +10m",
        "§c▶ Right §7→ -10m",
        "§e▶ Shift §7→ Set exact via chat"),
      a -> {
        switch (a.getClickType()) {
          case SHIFT_LEFT_CLICK, SHIFT_RIGHT_CLICK ->
            ChatInputManager.requestInput(player, "Enter cooldown (e.g. 60m, 1d, 0 0 * * *):", input -> {
              if (input == null || input.isBlank() || input.equalsIgnoreCase("none")) {
                product.setCooldown(null);
              } else {
                product.setCooldown(input.trim());
                if (product.getMax() == null) product.setMax(1);
                product.setStockAmount(null);
                product.setStockMode(null);
              }
              ConfigLoader.saveShop(shop);
              ctx.runOnServer(() -> openProductEditor(player, shop, product, config, modId));
            });
          case LEFT_CLICK -> {
            product.setCooldown(addMinutesToCooldown(product.getCooldown(), 10));
            if (product.getMax() == null) product.setMax(1);
            product.setStockAmount(null);
            product.setStockMode(null);
            ConfigLoader.saveShop(shop);
            openProductEditor(player, shop, product, config, modId);
          }
          default -> {
            product.setCooldown(addMinutesToCooldown(product.getCooldown(), -10));
            if (product.getMax() == null) product.setMax(1);
            product.setStockAmount(null);
            product.setStockMode(null);
            ConfigLoader.saveShop(shop);
            openProductEditor(player, shop, product, config, modId);
          }
        }
      }));

    template.set(38, button(new ItemStack(Items.WRITABLE_BOOK), "§e✎ Change Product ID",
      List.of(SEP,
        "§7Current: §f" + truncate(product.getProduct(), 35),
        "§7Type: " + productTypeTag(product),
        "§7Max Stack: §f" + safeMaxStack(product),
        "",
        "§7Supported formats:",
        "§f  minecraft:diamond",
        "§f  item:1:minecraft:diamond#[...]",
        "§f  pokemon:pikachu level:50",
        "§f  command:give %player% diamond 1",
        SEP,
        "§a▶ Left §7→ Pick from inventory",
        "§e▶ Right §7→ Type in chat"),
      a -> {
        switch (a.getClickType()) {
          case RIGHT_CLICK, SHIFT_RIGHT_CLICK ->
            ChatInputManager.requestInput(player, "Enter product ID (item/pokemon:/command:):", input -> {
              product.setProduct(input);
              ConfigLoader.saveShop(shop);
              ctx.runOnServer(() -> openProductEditor(player, shop, product, config, modId));
            });
          default -> openInventoryPickerForEdit(player, shop, product, config, modId);
        }
      }));

    template.set(39, button(new ItemStack(Items.ITEM_FRAME), "§e⊞ Slot Position",
      List.of(SEP,
        "§7Current: §f" + (product.getSlot() != null ? "Slot " + product.getSlot() : "§8auto (shop fills grid)"),
        "",
        "§7Fixed slot in the shop GUI.",
        "§7Only used when AutoPlace is OFF.",
        "§7Slots: 0-" + (((shop.getDisplayConfig() != null ? shop.getDisplayConfig().getRows() : 6) * 9) - 1),
        SEP,
        "§a▶ Left §7→ +1",
        "§c▶ Right §7→ -1",
        "§e▶ Shift §7→ Clear (auto)"),
      a -> {
        switch (a.getClickType()) {
          case SHIFT_LEFT_CLICK, SHIFT_RIGHT_CLICK -> {
            product.setSlot(null);
            ConfigLoader.saveShop(shop);
            openProductEditor(player, shop, product, config, modId);
          }
          case LEFT_CLICK -> {
            int c = product.getSlot() != null ? product.getSlot() : 0;
            product.setSlot(Math.min(c + 1, 53));
            ConfigLoader.saveShop(shop);
            openProductEditor(player, shop, product, config, modId);
          }
          default -> {
            int c = product.getSlot() != null ? product.getSlot() : 0;
            product.setSlot(Math.max(c - 1, 0));
            ConfigLoader.saveShop(shop);
            openProductEditor(player, shop, product, config, modId);
          }
        }
      }));

    List<String> loreBtnLore = new ArrayList<>();
    loreBtnLore.add(SEP);
    loreBtnLore.add("§7Lines: §f" + (product.getLore() != null ? product.getLore().size() : 0));
    if (product.getLore() != null && !product.getLore().isEmpty()) {
      loreBtnLore.add("");
      loreBtnLore.add("§7Preview:");
      for (int li = 0; li < Math.min(product.getLore().size(), 5); li++) {
        loreBtnLore.add("  §8" + (li + 1) + ". §7" + truncate(product.getLore().get(li), 32));
      }
      if (product.getLore().size() > 5) loreBtnLore.add("  §8... +" + (product.getLore().size() - 5) + " more");
    }
    loreBtnLore.add(SEP);
    loreBtnLore.add("§a▶ Left §7→ Add line via chat");
    loreBtnLore.add("§c▶ Right §7→ Remove last line");
    loreBtnLore.add("§c▶ Shift §7→ Clear all");

    template.set(40, button(new ItemStack(Items.BOOK), "§e✎ Lore", loreBtnLore,
      a -> {
        switch (a.getClickType()) {
          case SHIFT_LEFT_CLICK, SHIFT_RIGHT_CLICK -> {
            product.setLore(null);
            ConfigLoader.saveShop(shop);
            openProductEditor(player, shop, product, config, modId);
          }
          case RIGHT_CLICK -> {
            if (product.getLore() != null && !product.getLore().isEmpty()) {
              List<String> l = new ArrayList<>(product.getLore());
              l.remove(l.size() - 1);
              product.setLore(l.isEmpty() ? null : l);
              ConfigLoader.saveShop(shop);
            }
            openProductEditor(player, shop, product, config, modId);
          }
          default -> ChatInputManager.requestInput(player, "Enter lore line (supports & colors):", input -> {
            List<String> l = product.getLore() != null ? new ArrayList<>(product.getLore()) : new ArrayList<>();
            l.add(input);
            product.setLore(l);
            ConfigLoader.saveShop(shop);
            ctx.runOnServer(() -> openProductEditor(player, shop, product, config, modId));
          });
        }
      }));

    {
      int condCount = product.getConditions() != null ? product.getConditions().size() : 0;
      List<String> condLore = new ArrayList<>();
      condLore.add(SEP);
      if (condCount == 0) {
        condLore.add("§7No buy conditions — anyone can buy.");
      } else {
        for (int ci = 0; ci < product.getConditions().size(); ci++) {
          condLore.add("§7" + (ci + 1) + ". §f" + product.getConditions().get(ci).getType());
        }
      }
      condLore.add(SEP);
      condLore.add("§7Conditions checked BEFORE a player");
      condLore.add("§7can purchase this product.");
      condLore.add(SEP);
      condLore.add("§a▶ Click §7→ Manage conditions");
      template.set(41, button(new ItemStack(Items.IRON_BARS),
        "§c⚡ Buy Conditions §7(" + condCount + ")",
        condLore, a -> openProductConditionsList(player, shop, product, config, modId, false)));
    }

    {
      int visCount = product.getVisibilityConditions() != null ? product.getVisibilityConditions().size() : 0;
      List<String> visLore = new ArrayList<>();
      visLore.add(SEP);
      if (visCount == 0) {
        visLore.add("§7No visibility conditions — always visible.");
      } else {
        for (int ci = 0; ci < product.getVisibilityConditions().size(); ci++) {
          visLore.add("§7" + (ci + 1) + ". §f" + product.getVisibilityConditions().get(ci).getType());
        }
      }
      visLore.add(SEP);
      visLore.add("§7Conditions that determine if this");
      visLore.add("§7product is shown in the shop GUI.");
      visLore.add(SEP);
      visLore.add("§a▶ Click §7→ Manage conditions");
      template.set(42, button(new ItemStack(Items.ENDER_EYE),
        "§c👁 Visibility Conditions §7(" + visCount + ")",
        visLore, a -> openProductConditionsList(player, shop, product, config, modId, true)));
    }

    template.set(43, button(new ItemStack(Items.CHEST), lang.getEditorButtonStockControl(),
      List.of(SEP,
        "§7Enabled: " + boolIcon(product.hasStockControl()),
        "§7Amount: §f" + (product.getStockAmount() != null ? product.getStockAmount() : "§8none"),
        "§7Mode: §f" + (product.getStockMode() != null ? product.getStockMode() : "§8none"),
        "",
        "§7PLAYER: stock per player",
        "§7GLOBAL: shared stock for all players",
        "§c⚠ Permanent stock (no cooldown).",
        "§8(Clears purchase limit if active)",
        SEP,
        "§a▶ Left §7→ +1 stock",
        "§c▶ Right §7→ -1 stock",
        "§e▶ Middle §7→ Toggle mode",
        "§c▶ Shift §7→ Disable stock"),
      a -> {
        switch (a.getClickType()) {
          case SHIFT_LEFT_CLICK, SHIFT_RIGHT_CLICK -> {
            product.setStockAmount(null);
            product.setStockMode(null);
            ConfigLoader.saveShop(shop);
            openProductEditor(player, shop, product, config, modId);
          }
          case MIDDLE_CLICK -> {
            if (product.getStockAmount() == null || product.getStockAmount() <= 0) {
              product.setStockAmount(1);
            }
            product.setMax(null);
            product.setCooldown(null);
            StockMode mode = product.getStockMode();
            product.setStockMode(mode == StockMode.GLOBAL ? StockMode.PLAYER : StockMode.GLOBAL);
            ConfigLoader.saveShop(shop);
            openProductEditor(player, shop, product, config, modId);
          }
          case LEFT_CLICK -> {
            int current = product.getStockAmount() != null ? product.getStockAmount() : 0;
            product.setStockAmount(current + 1);
            if (product.getStockMode() == null) product.setStockMode(StockMode.PLAYER);
            product.setMax(null);
            product.setCooldown(null);
            ConfigLoader.saveShop(shop);
            openProductEditor(player, shop, product, config, modId);
          }
          default -> {
            if (product.getStockAmount() != null && product.getStockAmount() > 1) {
              product.setStockAmount(product.getStockAmount() - 1);
              product.setMax(null);
              product.setCooldown(null);
            } else {
              product.setStockAmount(null);
              product.setStockMode(null);
            }
            ConfigLoader.saveShop(shop);
            openProductEditor(player, shop, product, config, modId);
          }
        }
      }));

    {
      List<PriceEntry> prices = product.getPrices();
      int priceCount = prices != null ? prices.size() : 0;
      List<String> priceLore = new ArrayList<>();
      priceLore.add(SEP);
      if (priceCount == 0) {
        priceLore.add("§7Single currency mode (uses shop primary economy).");
      } else {
        for (PriceEntry pe : prices) {
          priceLore.add("§7• §f" + pe.getEconomy().getEconomyId() + "§8: §aB: " + fmt(pe.getBuy()) + " §8/ §cS: " + fmt(pe.getSell()));
        }
      }
      priceLore.add(SEP);
      priceLore.add("§a▶ Click §7→ Manage multi-currency prices");
      template.set(44, button(new ItemStack(Items.GOLD_INGOT),
        "§e⛃ Multi-Currency Prices §7(" + priceCount + ")",
        priceLore, a -> openProductPricesEditor(player, shop, product, config, modId)));
    }

    template.set(45, backBtn(lang, a -> ProductListEditor.openProductList(player, shop, config, modId)));

    template.set(46, button(new ItemStack(Items.HOPPER), "§6⏱ Max Sales (Limit)",
      List.of(SEP,
        "§7Max Sales: §f" + (product.getSellMax() != null ? product.getSellMax() : "§8unlimited"),
        "§7Cooldown: §f" + (product.getSellCooldown() != null ? product.getSellCooldown() : "§8none"),
        "§7UUID: §8" + (product.getSellUuid() != null ? product.getSellUuid().toString().substring(0, 8) + "..." : "auto-generated"),
        "",
        "§7Limits how many times a player can sell.",
        "§7Resets after the cooldown period.",
        SEP,
        "§a▶ Left §7→ +1",
        "§c▶ Right §7→ -1",
        "§a▶ Shift+Left §7→ +10",
        "§c▶ Shift+Right §7→ Clear (unlimited)"),
      a -> {
        switch (a.getClickType()) {
          case SHIFT_RIGHT_CLICK -> {
            product.setSellMax(null);
            product.setSellCooldown(null);
            product.setSellUuid(null);
            ConfigLoader.saveShop(shop);
            openProductEditor(player, shop, product, config, modId);
          }
          case SHIFT_LEFT_CLICK -> {
            product.setSellMax((product.getSellMax() != null ? product.getSellMax() : 0) + 10);
            if (product.getSellCooldown() == null) product.setSellCooldown("60m");
            if (product.getSellUuid() == null) product.setSellUuid(UUID.randomUUID());
            ConfigLoader.saveShop(shop);
            openProductEditor(player, shop, product, config, modId);
          }
          case LEFT_CLICK -> {
            product.setSellMax((product.getSellMax() != null ? product.getSellMax() : 0) + 1);
            if (product.getSellCooldown() == null) product.setSellCooldown("60m");
            if (product.getSellUuid() == null) product.setSellUuid(UUID.randomUUID());
            ConfigLoader.saveShop(shop);
            openProductEditor(player, shop, product, config, modId);
          }
          default -> {
            if (product.getSellMax() != null && product.getSellMax() > 1) {
              product.setSellMax(product.getSellMax() - 1);
            } else {
              product.setSellMax(null);
              product.setSellCooldown(null);
              product.setSellUuid(null);
            }
            ConfigLoader.saveShop(shop);
            openProductEditor(player, shop, product, config, modId);
          }
        }
      }));

    template.set(47, button(new ItemStack(Items.CLOCK), "§6⏱ Sell Cooldown (duration/cron)",
      List.of(SEP,
        "§7Current: §f" + (product.getSellCooldown() != null ? product.getSellCooldown() : "§8none"),
        "",
        "§7Time before the sell limit resets.",
        "§7Requires §fMax Sales §7to be set.",
        SEP,
        "§a▶ Left §7→ +10m",
        "§c▶ Right §7→ -10m",
        "§e▶ Shift §7→ Set exact via chat"),
      a -> {
        switch (a.getClickType()) {
          case SHIFT_LEFT_CLICK, SHIFT_RIGHT_CLICK ->
            ChatInputManager.requestInput(player, "Enter sell cooldown (e.g. 60m, 1d, 0 0 * * *):", input -> {
              if (input == null || input.isBlank() || input.equalsIgnoreCase("none")) {
                product.setSellCooldown(null);
              } else {
                product.setSellCooldown(input.trim());
              }
              ConfigLoader.saveShop(shop);
              ctx.runOnServer(() -> openProductEditor(player, shop, product, config, modId));
            });
          case LEFT_CLICK -> {
            product.setSellCooldown(addMinutesToCooldown(product.getSellCooldown(), 10));
            ConfigLoader.saveShop(shop);
            openProductEditor(player, shop, product, config, modId);
          }
          default -> {
            product.setSellCooldown(addMinutesToCooldown(product.getSellCooldown(), -10));
            ConfigLoader.saveShop(shop);
            openProductEditor(player, shop, product, config, modId);
          }
        }
      }));

    template.set(49, backBtn(lang, a -> ProductListEditor.openProductList(player, shop, config, modId)));

    GooeyPage page = GooeyPage.builder().template(template)
      .title(AdventureTranslator.toNative(lang.getEditorTitleProductEdit().replace("%product%", truncate(product.getProduct(), 20))))
      .build();
    ctx.runOnServer(() -> UIManager.openUIForcefully(player, page));
  }

  public static void openProductConditionsList(ServerPlayerEntity player, Shop shop, Product product,
                                               ShopConfig config, String modId, boolean isVisibility) {
    ShopContext ctx = ShopContext.get();
    List<Condition> conditions = isVisibility ? product.getVisibilityConditions() : product.getConditions();
    List<Condition> mutable = conditions != null ? new ArrayList<>(conditions) : new ArrayList<>();
    String label = isVisibility ? "Visibility" : "Buy";
    String title = label + " Conditions: " + truncate(product.getProduct(), 15);
    ConditionListMenu.open(player, mutable, title, updated -> {
      if (isVisibility) {
        product.setVisibilityConditions(updated == null || updated.isEmpty() ? null : updated);
      } else {
        product.setConditions(updated == null || updated.isEmpty() ? null : updated);
      }
      ConfigLoader.saveShop(shop);
      ctx.runOnServer(() -> openProductEditor(player, shop, product, config, modId));
    });
  }

  public static void openProductPricesEditor(ServerPlayerEntity player, Shop shop, Product product,
                                             ShopConfig config, String modId) {
    ShopContext ctx = ShopContext.get();
    LangConfig lang = ctx.getLang();
    List<PriceEntry> prices = product.getPrices();
    if (prices == null) {
      prices = new ArrayList<>();
      product.setPrices(prices);
    }
    List<Button> buttons = new ArrayList<>();

    for (int i = 0; i < prices.size(); i++) {
      PriceEntry pe = prices.get(i);
      final int index = i;
      List<String> lore = List.of(
        SEP,
        "§7Economy: §f" + pe.getEconomy().getEconomyId(),
        "§7Currency: §f" + pe.getEconomy().getCurrency(),
        "§7Buy Price: §e" + (pe.getBuy() != null ? fmt(pe.getBuy()) : "§8disabled"),
        "§7Sell Price: §e" + (pe.getSell() != null ? fmt(pe.getSell()) : "§8disabled"),
        SEP,
        "§a▶ Left click §7→ Edit buy price",
        "§e▶ Right click §7→ Edit sell price",
        "§c▶ Shift+Right click §7→ Remove entry"
      );

      buttons.add(button(new ItemStack(Items.GOLD_NUGGET), "§6§l" + pe.getEconomy().getEconomyId(), lore, a -> {
        if (a.getClickType().name().contains("SHIFT")) {
          product.getPrices().remove(index);
          if (product.getPrices().isEmpty()) product.setPrices(null);
          ConfigLoader.saveShop(shop);
          openProductPricesEditor(player, shop, product, config, modId);
        } else if (a.getClickType().name().contains("LEFT")) {
          ChatInputManager.requestInput(player, "Enter buy price for " + pe.getEconomy().getEconomyId() + ":", input -> {
            try {
              pe.setBuy(new BigDecimal(input.trim()));
              ConfigLoader.saveShop(shop);
            } catch (Exception e) {
              PlayerUtils.sendMessage(player, lang.getEditorInvalidFormat().replace("%input%", input), lang.getPrefix(), TypeMessage.CHAT);
            }
            ctx.runOnServer(() -> openProductPricesEditor(player, shop, product, config, modId));
          });
        } else {
          ChatInputManager.requestInput(player, "Enter sell price for " + pe.getEconomy().getEconomyId() + ":", input -> {
            try {
              pe.setSell(new BigDecimal(input.trim()));
              ConfigLoader.saveShop(shop);
            } catch (Exception e) {
              PlayerUtils.sendMessage(player, lang.getEditorInvalidFormat().replace("%input%", input), lang.getPrefix(), TypeMessage.CHAT);
            }
            ctx.runOnServer(() -> openProductPricesEditor(player, shop, product, config, modId));
          });
        }
      }));
    }

    ChestTemplate template = ChestTemplate.builder(6).build();
    template.set(45, backBtn(lang, a -> openProductEditor(player, shop, product, config, modId)));
    template.set(49, closeBtn(lang, player));
    template.set(53, nextBtn(lang));

    template.set(47, button(new ItemStack(Items.LIME_DYE), "§a§l+ Add Economy Price", List.of(
      SEP,
      "§7Add a price in another currency.",
      "§7Format: economyId:currency",
      "§7(e.g. impactor:dollars or item:minecraft:diamond)",
      SEP,
      "§a▶ Click §7→ Add"
    ), a -> {
      ChatInputManager.requestInput(player, "Enter economy in format 'id:currency' (e.g. impactor:dollars):", ecoInput -> {
        String[] parts = ecoInput.trim().split(":", 2);
        if (parts.length < 2) {
          PlayerUtils.sendMessage(player, lang.getEditorInvalidKeyValueFormat(), lang.getPrefix(), TypeMessage.CHAT);
          ctx.runOnServer(() -> openProductPricesEditor(player, shop, product, config, modId));
          return;
        }
        EconomyUse eco = new EconomyUse(parts[0].trim(), parts[1].trim());
        ChatInputManager.requestInput(player, "Enter buy price (or 0):", buyInput -> {
          BigDecimal buy = BigDecimal.ZERO;
          try { buy = new BigDecimal(buyInput.trim()); } catch (Exception ignored) {}
          final BigDecimal finalBuy = buy;
          ChatInputManager.requestInput(player, "Enter sell price (or 0):", sellInput -> {
            BigDecimal sell = BigDecimal.ZERO;
            try { sell = new BigDecimal(sellInput.trim()); } catch (Exception ignored) {}
            product.getPrices().add(new PriceEntry(eco, finalBuy, sell));
            ConfigLoader.saveShop(shop);
            ctx.runOnServer(() -> openProductPricesEditor(player, shop, product, config, modId));
          });
        });
      });
    }));

    new Rectangle(0, 0, 5, 9).apply(template);

    LinkedPage.Builder lp = LinkedPage.builder().template(template)
      .title(AdventureTranslator.toNative(lang.getEditorTitleProductPrices().replace("%product%", truncate(product.getProduct(), 15))));
    LinkedPage page = buttons.isEmpty() ? lp.build()
      : PaginationHelper.createPagesFromPlaceholders(template, buttons, lp);
    ctx.runOnServer(() -> UIManager.openUIForcefully(player, page));
  }

  public static void openInventoryPickerForEdit(ServerPlayerEntity player, Shop shop, Product product,
                                                ShopConfig config, String modId) {
    ShopContext ctx = ShopContext.get();
    LangConfig lang = ctx.getLang();
    List<Button> buttons = new ArrayList<>();

    Set<String> seen = new LinkedHashSet<>();
    List<ItemStack> allStacks = new ArrayList<>(player.getInventory().main);
    allStacks.addAll(player.getInventory().armor);
    allStacks.add(player.getInventory().offHand.getFirst());

    for (ItemStack stack : allStacks) {
      if (stack.isEmpty()) continue;
      String productId = itemStackToProductId(stack);
      String simpleId = Registries.ITEM.getId(stack.getItem()).toString();
      if (!seen.add(productId)) continue;

      ItemStack display = stack.copy();
      display.setCount(1);

      boolean hasComponents = !productId.equals(simpleId);
      final String finalProductId = productId;
      List<String> lore = new ArrayList<>();
      lore.add(SEP);
      lore.add("§7Item: §f" + simpleId);
      if (hasComponents) lore.add("§7Components: §a✓");
      lore.add("§7Max stack: §f" + stack.getMaxCount());
      lore.add(SEP);
      lore.add("§a▶ Click §7→ Set as product ID");

      buttons.add(GooeyButton.builder()
        .display(display)
        .with(DataComponentTypes.CUSTOM_NAME, AdventureTranslator.toNative("§f" + simpleId + (hasComponents ? " §a[+]" : "")))
        .with(DataComponentTypes.LORE, new LoreComponent(AdventureTranslator.toNativeL(lore)))
        .with(DataComponentTypes.HIDE_ADDITIONAL_TOOLTIP, Unit.INSTANCE)
        .onClick(a -> {
          product.setProduct(finalProductId);
          ConfigLoader.saveShop(shop);
          openProductEditor(player, shop, product, config, modId);
        })
        .build());
    }

    ChestTemplate template = ChestTemplate.builder(6).build();
    template.set(45, backBtn(lang, a -> openProductEditor(player, shop, product, config, modId)));
    template.set(49, backBtn(lang, a -> openProductEditor(player, shop, product, config, modId)));
    template.set(53, nextBtn(lang));
    new Rectangle(0, 0, 5, 9).apply(template);

    LinkedPage.Builder lp = LinkedPage.builder().template(template)
      .title(AdventureTranslator.toNative(lang.getEditorTitleInventoryPickerEdit().replace("%product%", truncate(product.getProduct(), 18))));
    GooeyPage page = buttons.isEmpty() ? lp.build()
      : PaginationHelper.createPagesFromPlaceholders(template, buttons, lp);
    ctx.runOnServer(() -> UIManager.openUIForcefully(player, page));
  }

  private static String addMinutesToCooldown(String cooldown, int minutes) {
    if (cooldown == null || cooldown.isBlank() || cooldown.equalsIgnoreCase("none")) {
      return minutes > 0 ? minutes + "m" : null;
    }
    try {
      if (cooldown.endsWith("m")) {
        int m = Integer.parseInt(cooldown.substring(0, cooldown.length() - 1));
        int next = Math.max(0, m + minutes);
        return next > 0 ? next + "m" : null;
      }
      if (cooldown.endsWith("h")) {
        int h = Integer.parseInt(cooldown.substring(0, cooldown.length() - 1));
        int next = Math.max(0, h * 60 + minutes);
        return next > 0 ? next + "m" : null;
      }
      if (cooldown.endsWith("d")) {
        int d = Integer.parseInt(cooldown.substring(0, cooldown.length() - 1));
        int next = Math.max(0, d * 24 * 60 + minutes);
        return next > 0 ? next + "m" : null;
      }
    } catch (NumberFormatException ignored) {
    }
    return cooldown;
  }
}
