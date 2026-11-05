package com.kingpixel.ultrashop.presentation.gui.edit;

import ca.landonjw.gooeylibs2.api.UIManager;
import ca.landonjw.gooeylibs2.api.button.Button;
import ca.landonjw.gooeylibs2.api.helpers.PaginationHelper;
import ca.landonjw.gooeylibs2.api.page.LinkedPage;
import ca.landonjw.gooeylibs2.api.template.types.ChestTemplate;
import com.kingpixel.cobbleutils.Model.ItemModel;
import com.kingpixel.cobbleutils.Model.Rectangle;
import com.kingpixel.cobbleutils.util.AdventureTranslator;
import com.kingpixel.cobbleutils.util.PlayerUtils;
import com.kingpixel.cobbleutils.util.TypeMessage;
import com.kingpixel.ultrashop.ShopContext;
import com.kingpixel.ultrashop.domain.model.SubShop;
import com.kingpixel.ultrashop.domain.model.shop.CategoryShop;
import com.kingpixel.ultrashop.domain.model.shop.Shop;
import com.kingpixel.ultrashop.infrastructure.config.ConfigLoader;
import com.kingpixel.ultrashop.infrastructure.config.LangConfig;
import com.kingpixel.ultrashop.infrastructure.config.ShopConfig;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.server.network.ServerPlayerEntity;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static com.kingpixel.ultrashop.presentation.gui.edit.EditorHelpers.*;

/**
 * Editor menu for category shop sub-shops (navigation links to child shops).
 */
public final class SubShopListEditor {

  private SubShopListEditor() {}

  public static void openSubShopList(ServerPlayerEntity player, CategoryShop categoryShop, ShopConfig config, String modId) {
    ShopContext ctx = ShopContext.get();
    LangConfig lang = ctx.getLang();
    List<SubShop> subShops = categoryShop.getSubShops();
    if (subShops == null) {
      subShops = new ArrayList<>();
      categoryShop.setSubShops(subShops);
    }
    List<Button> buttons = new ArrayList<>();

    for (int i = 0; i < subShops.size(); i++) {
      SubShop sub = subShops.get(i);
      final int index = i;
      Shop targetShop = ctx.getTypedShops(modId).stream()
        .filter(s -> s.getId().equalsIgnoreCase(sub.getIdShop()))
        .findFirst()
        .orElse(null);

      ItemStack displayStack;
      if (targetShop != null && targetShop.getDisplayConfig() != null && targetShop.getDisplayConfig().getDisplayItem() != null) {
        displayStack = targetShop.getDisplayConfig().getDisplayItem().getItemStack();
      } else {
        displayStack = new ItemStack(Items.CHEST);
      }

      List<String> lore = new ArrayList<>();
      lore.add(SEP);
      lore.add("§7Target Shop: §f" + sub.getIdShop());
      lore.add("§7Assigned Slot: §e" + sub.getSlot());
      if (targetShop != null) {
        lore.add("§7Shop Type: §b" + targetShop.getType());
        lore.add("§7Name: §f" + (targetShop.getDisplayConfig() != null ? targetShop.getDisplayConfig().getName() : ""));
      } else {
        lore.add("§c⚠ Target shop not found in this mod");
      }
      lore.add(SEP);
      lore.add("§a▶ Left click §7→ Change slot");
      lore.add("§c▶ Shift+Right click §7→ Remove sub-shop");

      buttons.add(button(displayStack, "§6§l" + sub.getIdShop(), lore, action -> {
        if (action.getClickType().name().contains("SHIFT")) {
          categoryShop.getSubShops().remove(index);
          ctx.replaceShop(modId, categoryShop);
          ConfigLoader.saveShop(categoryShop);
          openSubShopList(player, categoryShop, config, modId);
        } else {
          ChatInputManager.requestInput(player, "Enter slot number for " + sub.getIdShop() + " (0 - 53):", input -> {
            try {
              int slot = Integer.parseInt(input.trim());
              if (slot < 0 || slot > 53) {
                PlayerUtils.sendMessage(player, lang.getEditorInvalidSlotsInput().replace("%max%", "53"), lang.getPrefix(), TypeMessage.CHAT);
              } else {
                sub.setSlot(slot);
                ctx.replaceShop(modId, categoryShop);
                ConfigLoader.saveShop(categoryShop);
              }
            } catch (Exception e) {
              PlayerUtils.sendMessage(player, lang.getEditorInvalidFormat().replace("%input%", input), lang.getPrefix(), TypeMessage.CHAT);
            }
            ctx.runOnServer(() -> openSubShopList(player, categoryShop, config, modId));
          });
        }
      }));
    }

    ChestTemplate template = ChestTemplate.builder(6).build();
    template.set(45, backBtn(lang, a -> ShopListEditor.openShopList(player, config, modId)));
    template.set(49, closeBtn(lang, player));
    template.set(53, nextBtn(lang));

    template.set(47, button(new ItemStack(Items.WRITABLE_BOOK), "§a§l+ Link Sub-Shop", List.of(
      SEP,
      "§7Add a child shop to this category.",
      "§7Click to select from available shops.",
      SEP,
      "§a▶ Click §7→ Pick shop"
    ), a -> openShopPickerForCategory(player, categoryShop, config, modId)));

    new Rectangle(0, 0, 5, 9).apply(template);

    String title = lang.getEditorTitleSubShopList().replace("%shop%", categoryShop.getId());
    LinkedPage.Builder lp = LinkedPage.builder().template(template)
      .title(AdventureTranslator.toNative(title));
    LinkedPage page = buttons.isEmpty() ? lp.build()
      : PaginationHelper.createPagesFromPlaceholders(template, buttons, lp);
    ctx.runOnServer(() -> UIManager.openUIForcefully(player, page));
  }

  private static void openShopPickerForCategory(ServerPlayerEntity player, CategoryShop categoryShop, ShopConfig config, String modId) {
    ShopContext ctx = ShopContext.get();
    LangConfig lang = ctx.getLang();
    Set<String> alreadyLinked = categoryShop.getSubShops().stream()
      .map(s -> s.getIdShop().toLowerCase())
      .collect(Collectors.toSet());

    List<Shop> available = ctx.getTypedShops(modId).stream()
      .filter(s -> !s.getId().equalsIgnoreCase(categoryShop.getId()))
      .filter(s -> !alreadyLinked.contains(s.getId().toLowerCase()))
      .toList();

    List<Button> buttons = new ArrayList<>();
    for (Shop shop : available) {
      ItemModel display = LangConfig.resolve(
        shop.getDisplayConfig() != null ? shop.getDisplayConfig().getDisplayItem() : null,
        lang.getGlobalDisplay()
      );
      List<String> lore = List.of(
        SEP,
        "§7Type: §f" + shop.getType(),
        "§7Name: §f" + (shop.getDisplayConfig() != null ? shop.getDisplayConfig().getName() : ""),
        SEP,
        "§a▶ Click §7→ Link to category"
      );
      buttons.add(button(display.getItemStack(), "§6§l" + shop.getId(), lore, a -> {
        ChatInputManager.requestInput(player, "Enter slot number for " + shop.getId() + " (0 - 53):", input -> {
          try {
            int slot = Integer.parseInt(input.trim());
            if (slot < 0 || slot > 53) {
              PlayerUtils.sendMessage(player, lang.getEditorInvalidSlotsInput().replace("%max%", "53"), lang.getPrefix(), TypeMessage.CHAT);
            } else {
              categoryShop.getSubShops().add(new SubShop(slot, shop.getId()));
              ctx.replaceShop(modId, categoryShop);
              ConfigLoader.saveShop(categoryShop);
            }
          } catch (Exception e) {
            PlayerUtils.sendMessage(player, lang.getEditorInvalidFormat().replace("%input%", input), lang.getPrefix(), TypeMessage.CHAT);
          }
          ctx.runOnServer(() -> openSubShopList(player, categoryShop, config, modId));
        });
      }));
    }

    ChestTemplate template = ChestTemplate.builder(6).build();
    template.set(45, backBtn(lang, a -> openSubShopList(player, categoryShop, config, modId)));
    template.set(49, closeBtn(lang, player));
    template.set(53, nextBtn(lang));
    new Rectangle(0, 0, 5, 9).apply(template);

    LinkedPage.Builder lp = LinkedPage.builder().template(template)
      .title(AdventureTranslator.toNative("<#feca57><b>Select Shop to Link</b>"));
    LinkedPage page = buttons.isEmpty() ? lp.build()
      : PaginationHelper.createPagesFromPlaceholders(template, buttons, lp);
    ctx.runOnServer(() -> UIManager.openUIForcefully(player, page));
  }
}

