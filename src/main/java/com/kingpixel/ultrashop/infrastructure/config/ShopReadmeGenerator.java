package com.kingpixel.ultrashop.infrastructure.config;

import com.kingpixel.ultrashop.UltraShop;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Generates user-facing markdown documentation and guides in the configuration directory.
 */
public final class ShopReadmeGenerator {

  private ShopReadmeGenerator() {
  }

  /**
   * Generates (or regenerates) the README.md in the config root folder.
   */
  public static void generateReadme(Path configRoot) {
    try {
      Files.createDirectories(configRoot);
      Path readme = configRoot.resolve("README.md");
      String content = """
        # UltraShop Configuration
        
        Welcome to UltraShop! This folder contains all configuration files for your shop system.
        
        ## Directory Structure
        
        ```
        ultrashop/
        ├── config.json          # Core settings (database, commands, discounts, etc.)
        ├── lang/                # Language files (en.json, es.json, etc.)
        │   └── en.json
        ├── shop/                # Shop definitions — one .json per shop
        │   ├── blocks.json
        │   ├── legendary_rotation.json
        │   └── categories.json
        ├── data/
        │   ├── rotations/       # Dynamic shop rotation state (per modId/shopId)
        │   │   └── ultrashop/
        │   │       └── legendary_rotation.json
        │   ├── transactions/    # Transaction logs (one file per day)
        │   │   └── 2025-01-15.json
        │   └── users/           # Per-player data (buy limits, cooldowns)
        │       └── <uuid>.json
        └── README.md            # This file
        ```
        
        ## config.json Fields
        
        | Field | Type | Default | Description |
        |-------|------|---------|-------------|
        | `debug` | boolean | `false` | Enable debug logging |
        | `saveTransactions` | boolean | `true` | Record buy/sell transactions |
        | `lang` | string | `"en"` | Language file name (without .json) |
        | `rows` | int | `6` | GUI rows for the main menu |
        | `title` | string | `"Shop"` | Default GUI title |
        | `soundOpen` | string | `""` | Sound on GUI open |
        | `soundClose` | string | `""` | Sound on GUI close |
        | `commands` | string[] | `["shop","ultrashop"]` | Command aliases for the shop |
        | `sellCommands` | string[] | `["sell"]` | Command aliases for /sell |
        | `maxBuyAmount` | int | `2304` | Maximum items per buy transaction |
        | `transactionPageSize` | int | `10` | Entries shown in /shop transactions |
        | `discounts` | map | `{"group.vip": 2.0}` | Permission-based discount percentages |
        | `dataBase` | object | JSON | Database config (JSON, MySQL, SQLite) |
        
        ## Shop JSON Fields
        
        | Field | Type | Required | Description |
        |-------|------|----------|-------------|
        | `name` | string | yes | Display name of the shop |
        | `title` | string | no | GUI title (use `%shop%` placeholder) |
        | `autoPlace` | boolean | no | Auto-arrange products in grid |
        | `rows` | int | no | Number of GUI rows |
        | `economies` | EconomyUse[] | yes | Default currencies (used by products without `prices`) |
        | `products` | Product[] | yes | List of products |
        | `subShops` | SubShop[] | no | Category navigation links |
        | `rotationSchedule` | object | no | Dynamic rotation config (see below) |
        | `rotationSlots` | int[] | no | Fixed GUI slots for rotated products (`ROTATION` shops only) |
        | `openConditions` | Condition[] | no | When the shop is accessible |
        | `announceRotation` | boolean | no | Broadcast rotation changes |
        | `globalDiscount` | float | no | Shop-wide discount percentage |
        | `discounts` | map | no | Permission-based discounts for this shop |
        
        ## Product JSON Fields
        
        All fields except `product`, `buy`, and `sell` are **nullable** (omit them if not needed).
        
        | Field | Type | Required | Description |
        |-------|------|----------|-------------|
        | `product` | string | yes | Item ID (`minecraft:stone`, `command:give %player% ...`, `pokemon:pikachu`) |
        | `buy` | decimal | yes | Buy price (0 = cannot buy). Used with shop's `economies` |
        | `sell` | decimal | yes | Sell price (0 = cannot sell). Used with shop's `economies` |
        | `prices` | PriceEntry[] | no | **Multi-currency override** — when set, `buy`/`sell` are ignored |
        | `display` | string | no | Override display item |
        | `displayname` | string | no | Custom display name |
        | `lore` | string[] | no | Custom lore lines |
        | `slot` | int | no | Fixed slot position (when autoPlace=false) |
        | `discount` | float | no | Per-product discount % |
        | `oneByOne` | boolean | no | Force stack size 1 |
        | `uuid` | UUID | no | Auto-generated for limited products |
        | `max` | int | no | Max purchases per cooldown period |
        | `cooldown` | int | no | Cooldown in minutes |
        | `chance` | int | no | Weight for rotation selection (default: 100) |
        | `conditions` | Condition[] | no | Conditions to buy |
        | `visibilityConditions` | Condition[] | no | Conditions to see the product |
        | `CustomModelData` | int | no | Custom model data for display item |
        
        ## Multi-Currency per Product
        
        By default, products use the simple `buy`/`sell` fields and the shop's `economies` list.
        For products that need different prices in different currencies, use the `prices` array:
        
        ### Simple product (most common — uses shop's economies):
        ```json
        {
          "product": "minecraft:diamond",
          "buy": 100,
          "sell": 50
        }
        ```
        
        ### Multi-currency product (optional — overrides buy/sell):
        ```json
        {
          "product": "minecraft:netherite_sword",
          "buy": 0,
          "sell": 0,
          "prices": [
            {
              "economy": { "type": "impactor", "currency": "impactor:dollars" },
              "buy": 500,
              "sell": 200
            },
            {
              "economy": { "type": "item", "currency": "minecraft:diamond" },
              "buy": 10,
              "sell": 5
            }
          ]
        }
        ```
        
        When `prices` is present and non-empty, the player must pay **ALL** listed currencies to buy,
        and receives **ALL** listed currencies when selling. The simple `buy`/`sell` fields are ignored.
        
        ### PriceEntry Fields
        
        | Field | Type | Description |
        |-------|------|-------------|
        | `economy` | EconomyUse | The economy provider (`type` + `currency`) |
        | `buy` | decimal | Buy price in this economy (0 = not charged) |
        | `sell` | decimal | Sell price in this economy (0 = not paid) |
        
        ## Shop Types
        
        Each shop has a `type` field with one of three values:
        
        | Value | Behavior |
        |-------|----------|
        | `NORMAL` | Static catalog. All `products` are always visible. `rotationSchedule` and `subShops` are ignored. |
        | `CATEGORY` | Menu shop. Shows the entries listed in `subShops` (which point to other shops by id). `products` and `rotationSchedule` are ignored. |
        | `ROTATION` | Dynamic catalog. A subset of `products` is rotated based on `rotationSchedule`. |
        
        ```json
        { "type": "ROTATION", "rotationSchedule": { "interval": "12h", "amount": 3 } }
        ```
        
        > **Back-compat:** if `type` is missing, it defaults to `NORMAL`. On load, if
        > `subShops` is non-empty it auto-promotes to `CATEGORY`; if `rotationSchedule`
        > is present it auto-promotes to `ROTATION`. Old configs keep working without
        > edits, but **adding `type` explicitly is strongly recommended**.
        
        ## Dynamic Rotations
        
        Set `type: "ROTATION"` and a `rotationSchedule` on a shop to enable rotating products:
        
        ### Using interval (relative cooldown):
        ```json
        "type": "ROTATION",
        "rotationSchedule": {
          "interval": "12h",
          "amount": 3
        }
        ```
        Supported intervals: `30m`, `1h`, `6h`, `12h`, `24h`, `7d`, etc.
        
        ### Using cron (fixed schedule):
        ```json
        "type": "ROTATION",
        "rotationSchedule": {
          "cron": "0 18 * * 5",
          "amount": 3
        }
        ```
        
        When `cron` is set, it **overrides** `interval`. The cron expression follows the standard 5-field format:
        `minute hour day-of-month month day-of-week`.
        
        Supported syntax: `*`, `n`, `a-b`, `a,b,c`, `*/n` (step). Day-of-week uses 0=Sunday..6=Saturday (7 also accepted as Sunday).
        
        Examples:
        - `0 18 * * 5` — every Friday at 18:00
        - `0 * * * *` — top of every hour
        - `*/15 * * * *` — every 15 minutes
        - `0 0,12 * * *` — at 00:00 and 12:00 every day
        - `0 9 1 * *` — at 09:00 on the 1st of every month
        
        **Cron examples:**
        | Expression | Description |
        |-----------|-------------|
        | `0 18 * * 5` | Every Friday at 18:00 |
        | `0 0 * * *` | Every day at midnight |
        | `0 12 * * 1,3,5` | Mon/Wed/Fri at noon |
        | `0 0 1 * *` | First day of each month at midnight |
        | `30 6 * * *` | Every day at 06:30 |
        
        ### Rotation shop fields (typed format)
        
        | Field | Type | Required | Description |
        |-------|------|----------|-------------|
        | `scheduler` | object | yes | When to rotate (`duration` or `cron`; see examples above) |
        | `rotationAmount` | int | yes | How many products are picked from `productPool` each rotation |
        | `rotationScope` | string | no | `GLOBAL` (default) or `PLAYER`. `GLOBAL` shares one catalog; `PLAYER` keeps a separate rotation per player. |
        | `rotationSlots` | int[] | no | Fixed GUI slots for rotated products, in order. The 1st picked product uses `rotationSlots[0]`, the 2nd uses `rotationSlots[1]`, etc. When set, overrides `autoPlace` for the active rotation. |
        | `productPool` | Product[] | yes | Full pool from which products are weighted-randomly selected |
        
        ### Fixed slots for rotated products
        
        By default, rotated products use `autoPlace` or per-product `slot` values from the pool.
        To pin each rotated product to specific chest slots, set `rotationSlots` with the same
        length as `rotationAmount`:
        
        ```json
        {
          "id": "hourly_rotation",
          "scheduler": { "type": "duration", "duration": "1h" },
          "rotationAmount": 4,
          "rotationSlots": [10, 11, 12, 13],
          "displayConfig": {
            "name": "Hourly Rotation",
            "autoPlace": false,
            "rows": 6
          },
          "productPool": [
            { "product": "minecraft:redstone", "buy": 30, "sell": 15, "chance": 100 },
            { "product": "minecraft:lapis_lazuli", "buy": 25, "sell": 12, "chance": 100 }
          ]
        }
        ```
        
        Slot numbers follow the chest GUI grid (0–53 for a 6-row menu). Invalid slots outside
        `rows * 9` are stripped on load. Configure slots in-game via the editor:
        **Shop Settings → ⊞ Rotation Slots**.
        
        ### Per-player rotations (`rotationScope: PLAYER`)
        
        Use `PLAYER` when each player should get their own rotation timer and product
        selection instead of sharing one global catalog:
        
        ```json
        {
          "id": "daily_specials",
          "scheduler": { "type": "duration", "duration": "24h" },
          "rotationAmount": 6,
          "rotationScope": "PLAYER",
          "productPool": [ ... ]
        }
        ```
        
        Player rotation state is stored in each player's user data file. Discord webhooks
        and `/shop restartShop` only apply to `GLOBAL` rotations.
        
        ## Transaction History
        
        Players can view their purchase/sale history via GUI:
        - `/shop transactions` — Opens a paginated GUI with your transaction history
        - `/shop transactions <player>` (admin) — Opens GUI showing another player's transactions
        
        Each transaction shows: date, action (BUY/SELL), product, amount, price, and currency.
        
        ## Commands
        
        | Command | Permission | Description |
        |---------|-----------|-------------|
        | `/shop` | `ultrashop.base` | Open main shop menu |
        | `/shop reload` | `ultrashop.admin` | Reload all configuration |
        | `/shop create <id> <dynamic>` | `ultrashop.admin` | Create a new shop |
        | `/shop delete <id>` | `ultrashop.admin` | Delete a shop |
        | `/shop edit` | `ultrashop.admin` | Open in-game shop/product editor GUI |
        | `/shop other <player> [shopId]` | `ultrashop.admin` | Open shop for another player |
        | `/shop restartShop <id>` | `ultrashop.restart.shop` | Force rotation restart |
        | `/shop transactions [player]` | `ultrashop.transactions` | View transaction history (GUI) |
        | `/shop search <query>` | `ultrashop.search.base` | Search products across shops |
        | `/sell hand` | `ultrashop.sell.base` | Sell item in hand |
        | `/sell all` | `ultrashop.sell.base` | Sell all sellable items |
        
        
        ---
        *Auto-generated by UltraShop. This file is regenerated on every reload.*
        """;
      Files.writeString(readme, content);
    } catch (IOException e) {
      UltraShop.LOGGER.error("Error generating README.md", e);
    }
  }
}
