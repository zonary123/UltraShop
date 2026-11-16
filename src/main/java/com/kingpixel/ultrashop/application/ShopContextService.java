package com.kingpixel.ultrashop.application;

import com.kingpixel.cobbleutils.Model.EconomyUse;
import com.kingpixel.cobbleutils.api.EconomyApi;
import com.kingpixel.ultrashop.ShopContext;
import com.kingpixel.ultrashop.api.model.PlayerShopProfileContext;
import com.kingpixel.ultrashop.api.model.ProductView;
import com.kingpixel.ultrashop.api.model.ShopView;
import com.kingpixel.ultrashop.api.model.TransactionView;
import com.kingpixel.ultrashop.domain.model.Product;
import com.kingpixel.ultrashop.domain.model.SubShop;
import com.kingpixel.ultrashop.domain.model.Transaction;
import com.kingpixel.ultrashop.domain.model.UserInfo;
import com.kingpixel.ultrashop.domain.model.shop.CategoryShop;
import com.kingpixel.ultrashop.domain.model.shop.Shop;
import com.kingpixel.ultrashop.domain.service.ShopQueryService;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import org.jetbrains.annotations.Nullable;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Headless query service delivering rich ViewModels for companion mods and external custom UIs.
 */
public final class ShopContextService {

  private static final ShopContextService INSTANCE = new ShopContextService();

  private ShopContextService() {
  }

  public static ShopContextService getInstance() {
    return INSTANCE;
  }

  /**
   * Builds a full {@link ShopView} containing active products, sub-shops, and permissions for a player.
   */
  public ShopView getShopView(Shop shop, @Nullable ServerPlayerEntity player) {
    if (shop == null) return null;
    String modId = ShopContext.get().findModId(shop);
    boolean canAccess = player != null ? ShopQueryService.canAccessShop(shop, player) : !shop.isMaintenance();

    List<ProductView> productViews = ShopQueryService.getProductViews(shop, player);
    List<String> subShopIds = new ArrayList<>();
    List<ShopView> subShops = new ArrayList<>();

    if (shop instanceof CategoryShop categoryShop && categoryShop.getSubShops() != null) {
      for (SubShop sub : categoryShop.getSubShops()) {
        if (sub != null && sub.getIdShop() != null) {
          subShopIds.add(sub.getIdShop());
          Shop child = ShopContext.get().getTypedShops(modId).stream()
            .filter(s -> s.getId().equals(sub.getIdShop()))
            .findFirst().orElse(null);
          if (child != null) {
            subShops.add(getShopView(child, player));
          }
        }
      }
    }

    String name = shop.getDisplayConfig() != null && shop.getDisplayConfig().getName() != null
      ? shop.getDisplayConfig().getName()
      : shop.getId();
    String title = shop.getDisplayConfig() != null && shop.getDisplayConfig().getTitle() != null
      ? shop.getDisplayConfig().getTitle()
      : name;

    ItemStack displayStack = ItemStack.EMPTY;
    if (shop.getDisplayConfig() != null && shop.getDisplayConfig().getDisplayItem() != null) {
      displayStack = shop.getDisplayConfig().getDisplayItem().getItemStack();
    }

    return ShopView.builder()
      .id(shop.getId())
      .modId(modId)
      .name(name)
      .title(title)
      .type(shop.getType())
      .displayStack(displayStack)
      .canAccess(canAccess)
      .maintenance(shop.isMaintenance())
      .productsCount(productViews.size())
      .activeProducts(productViews)
      .subShopIds(subShopIds)
      .subShops(subShops)
      .build();
  }

  /**
   * Builds a {@link ShopView} for a given modId and shopId.
   */
  public ShopView getShopView(String modId, String shopId, @Nullable ServerPlayerEntity player) {
    Shop shop = ShopContext.get().getTypedShops(modId).stream()
      .filter(s -> s.getId().equals(shopId))
      .findFirst().orElse(null);
    return getShopView(shop, player);
  }

  /**
   * Retrieves all top-level shop views for a specific modId.
   */
  public List<ShopView> getCategoryViews(String modId, @Nullable ServerPlayerEntity player) {
    List<Shop> shops = ShopContext.get().getTypedShops(modId);
    if (shops == null || shops.isEmpty()) return List.of();
    List<ShopView> views = new ArrayList<>();
    for (Shop s : shops) {
      views.add(getShopView(s, player));
    }
    return views;
  }

  /**
   * Builds {@link ProductView} instances for all active products in a shop.
   */
  public List<ProductView> getProductViews(Shop shop, @Nullable ServerPlayerEntity player) {
    return ShopQueryService.getProductViews(shop, player);
  }

  /**
   * Builds a full {@link ProductView} containing calculated pricing, availability, and display data.
   */
  public ProductView getProductView(Product product, @Nullable ServerPlayerEntity player, Shop shop) {
    return ShopQueryService.getProductView(product, player, shop);
  }

  /**
   * Retrieves a player's profile containing their balances across shop economies and daily sell stats.
   */
  public PlayerShopProfileContext getPlayerProfile(ServerPlayerEntity player, String modId) {
    Map<EconomyUse, BigDecimal> balances = new LinkedHashMap<>();
    List<Shop> shops = ShopContext.get().getTypedShops(modId);
    if (shops != null) {
      for (Shop s : shops) {
        if (s.getEconomies() != null) {
          for (EconomyUse eco : s.getEconomies()) {
            if (eco != null && !balances.containsKey(eco)) {
              balances.put(eco, EconomyApi.getBalance(player.getUuid(), eco));
            }
          }
        }
      }
    }

    Map<String, BigDecimal> dailySell = new LinkedHashMap<>();
    UserInfo userInfo = ShopContext.get().getRepositories().getUserRepository().findByUuid(player.getUuid());
    if (userInfo != null && userInfo.getDailySellEarnings() != null) {
      dailySell.putAll(userInfo.getDailySellEarnings());
    }

    return PlayerShopProfileContext.builder()
      .playerUuid(player.getUuid())
      .playerName(player.getGameProfile().getName())
      .balances(balances)
      .dailySellEarnings(dailySell)
      .canSellAll(true)
      .build();
  }

  /**
   * Retrieves recorded transaction history for a player mapped to clean ViewModels.
   */
  public List<TransactionView> getTransactionHistory(UUID playerUuid, int limit) {
    List<Transaction> records = ShopContext.get().getRepositories().getTransactionRepository().findByPlayer(playerUuid, limit);
    if (records == null || records.isEmpty()) return List.of();

    List<TransactionView> views = new ArrayList<>(records.size());
    for (Transaction t : records) {
      views.add(TransactionView.builder()
        .playerUuid(t.getPlayerUuid())
        .playerName(t.getPlayerName())
        .shopId(t.getShopId())
        .productId(t.getProductId())
        .action(t.getAction())
        .amount(t.getAmount())
        .value(t.getValue())
        .currency(t.getCurrency())
        .formattedPrice(t.getValue() != null && t.getCurrency() != null ? t.getValue().toPlainString() + " " + t.getCurrency() : "")
        .timestamp(t.getTimestamp())
        .build());
    }
    return views;
  }
}
