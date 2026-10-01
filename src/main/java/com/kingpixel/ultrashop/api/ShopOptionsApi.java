package com.kingpixel.ultrashop.api;

import com.kingpixel.ultrashop.api.ui.ShopUiProvider;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

/**
 * Options that identify a mod's shop registration.
 * Other mods pass this to {@link ShopApi#register} to set up their shops and optional custom UI.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ShopOptionsApi {
  private String modId;
  private String path;
  @Builder.Default
  private List<String> commands = new ArrayList<>();
  private ShopUiProvider uiProvider;

  public ShopOptionsApi(String modId, String path, List<String> commands) {
    this(modId, path, commands, null);
  }

  /**
   * Returns the path to the shop directory.
   */
  public String getPathShop() {
    return path + "shop/";
  }
}
