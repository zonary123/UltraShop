package com.kingpixel.ultrashop.api.model;

import com.kingpixel.ultrashop.domain.model.ShopType;
import lombok.Builder;
import lombok.Data;
import net.minecraft.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * Read-only view model representing a shop for external custom interfaces.
 */
@Data
@Builder
public class ShopView {

  @NotNull
  private final String id;

  @NotNull
  private final String modId;

  @NotNull
  private final String name;

  @NotNull
  private final String title;

  @NotNull
  private final ShopType type;

  @Nullable
  private final ItemStack displayStack;

  private final boolean canAccess;

  private final boolean maintenance;

  private final int productsCount;

  @NotNull
  private final List<ProductView> activeProducts;

  @NotNull
  private final List<String> subShopIds;

  @Nullable
  private final List<ShopView> subShops;
}
