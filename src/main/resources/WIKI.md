# 🏪 UltraShop — Comprehensive Architectural Manual & Administrator Wiki

---

## 📑 Table of Contents
1. [Executive Overview](#-executive-overview)
2. [Core Architecture & Hexagonal Structure](#-core-architecture--hexagonal-structure)
   - [Domain, Infrastructure & Presentation Segregation](#domain-infrastructure--presentation-segregation)
   - [Shop Polymorphism (Normal, Rotation & Category Shops)](#shop-polymorphism-normal-rotation--category-shops)
   - [Automated Dynamic Schedulers (Cron & Duration)](#automated-dynamic-schedulers-cron--duration)
   - [Embedded Web Dashboard Engine](#embedded-web-dashboard-engine)
3. [Economic Integration & Dynamic Pricing Models](#-economic-integration--dynamic-pricing-models)
   - [Multi-Currency Support (`EconomyConfig`)](#multi-currency-support-economyconfig)
   - [Dynamic Stock & Player Purchase Limits](#dynamic-stock--player-purchase-limits)
4. [In-Game Visual GUI Editor](#-in-game-visual-gui-editor)
5. [Command & Subcommand Reference](#-command--subcommand-reference)
6. [Permissions & Privilege Hierarchy](#-permissions--privilege-hierarchy)
7. [Discord Webhook Dispatcher](#-discord-webhook-dispatcher)
8. [Database & Persistence Subsystem](#-database--persistence-subsystem)
9. [Configuration Reference (`config.json` & Shop Schemas)](#-configuration-reference-configjson--shop-schemas)
10. [Enterprise Production Templates](#-enterprise-production-templates)

---

## 📌 Executive Overview

**UltraShop** is an enterprise-grade GUI shop and commercial marketplace mod engineered for modern **Minecraft** and **Cobblemon** server ecosystems. Designed using Clean/Hexagonal Domain-Driven Design principles, UltraShop elevates server commerce far beyond static chest shops—delivering dynamic timed rotation shops (e.g., Black Market dealers rotating inventory on Cron expressions), category hierarchies, per-player stock quotas, sound triggers, condition gates, embedded HTTP web management dashboards, and multi-database persistence (JSON & MongoDB).

Whether deploying a permanent Poke Mart, a temporary weekend bazaar, or an evolving seasonal festival vendor, UltraShop gives administrators total command over server pricing, inflation sinks, and player accessibility.

### Key Capabilities
- **Three Polymorphic Shop Archetypes**: Create fixed static shops (`NormalShop`), hierarchical tabbed directory shops (`CategoryShop`), and procedural timed rotation shops (`RotationShop`).
- **Flexible Cron & Duration Scheduling**: Rotate rare items, discount sales, and seasonal offerings automatically using standard 5-field Quartz Cron expressions or time durations.
- **Embedded Web Administration Dashboard**: Built-in HTTP server (`DashboardHttpServer`) allows server staff to inspect inventory metrics and sales ledgers in real time via web browsers.
- **Condition Gates**: Restrict purchases based on player rank, LuckPerms permissions, Cobblemon badges, player level, or team affiliations.
- **Complete In-Game Visual GUI Editor**: Construct and edit products, prices, lore, and visual templates live in-game without touching JSON files.

---

## 🏗️ Core Architecture & Hexagonal Structure

```
┌────────────────────────────────────────────────────────────────────────┐
│                        UltraShop Hexagonal Core                        │
└────────────────────────────────────┬───────────────────────────────────┘
                                     │
           ┌─────────────────────────┼─────────────────────────┐
           ▼                         ▼                         ▼
┌────────────────────┐    ┌────────────────────┐    ┌────────────────────┐
│   Domain Layer     │    │Infrastructure Layer│    │ Presentation Layer │
│ Models, Schedulers │    │ Mongo / JSON Repos │    │ GooeyLibs GUI/Cmds │
│ PriceCalculators   │    │ DashboardHttpServer│    │ In-Game Editor     │
└──────────┬─────────┘    └──────────┬─────────┘    └──────────┬─────────┘
           │                         │                         │
           └─────────────────────────┼─────────────────────────┘
                                     │
                                     ▼
┌────────────────────────────────────────────────────────────────────────┐
│                      Transaction & Stock Engine                        │
│   • Per-Player Quotas   • Global Stock Limits   • Webhook Analytics   │
└────────────────────────────────────┬───────────────────────────────────┘
                                     │
           ┌─────────────────────────┴─────────────────────────┐
           ▼                                                   ▼
┌────────────────────────────────────┐    ┌──────────────────────────────┐
│        JSON Repository             │    │       MongoDB Engine         │
│   `config/ultrashop/shops/`        │    │  High-Throughput Clustering  │
└────────────────────────────────────┘    └──────────────────────────────┘
```

### Domain, Infrastructure & Presentation Segregation
UltraShop strictly enforces clean architectural boundaries:
- **`domain`**: Pure business logic containing shop models (`Shop`, `RotationShop`, `CategoryShop`), price calculators (`PriceCalculator`), and schedulers (`CronScheduler`, `DurationScheduler`).
- **`infrastructure`**: I/O drivers, JSON adapters, MongoDB repositories (`MongoShopRepository`, `MongoTransactionRepository`), and the embedded HTTP dashboard.
- **`presentation`**: User-facing interfaces, GooeyLibs2 inventory builders (`MainMenuBuilder`, `ShopMenuBuilder`, `BuyAndSellMenuBuilder`), and Brigadier command dispatchers.

### Shop Polymorphism (Normal, Rotation & Category Shops)
1. **`NormalShop`**: Permanent standard marketplace with static product offerings (e.g., standard Poke Balls, Potions, Evolution Stones).
2. **`CategoryShop`**: Directory hub grouping multiple sub-shops under an organized, navigable category menu.
3. **`RotationShop`**: Dynamically chooses a random subset from an item pool on a timer (e.g., rolls 6 random TMs or Mega Stones every 6 hours), creating excitement and scarcity.

### Automated Dynamic Schedulers (Cron & Duration)
`RotationShop` instances utilize `SchedulerFactory`:
- **`CronScheduler`**: Fires on exact clock timings (e.g., `"0 0 * * *"` for midnight daily, or `"0 18 ? * FRI"` for Friday weekend markets).
- **`DurationScheduler`**: Fires on rolling intervals (e.g., every 4 hours).
- When triggered, previous stock is cleared, a fresh seed is rolled, and an announcement is broadcasted.

### Embedded Web Dashboard Engine
`DashboardHttpServer` spins up a lightweight embedded HTTP daemon on a configurable port (default: 8085), serving a responsive dashboard displaying active shops, top-selling items, total currency sunk from the economy, and transaction logs.

---

## 💰 Economic Integration & Dynamic Pricing Models

### Multi-Currency Support (`EconomyConfig`)
Each product or shop can define independent currency providers via `CobbleUtils.EconomyApi`:
- Standard economy dollars (Vault / Impactor).
- Custom secondary tokens (CobbleDollars, Battle Points, Raid Tokens).
- Item bartering (e.g., trade 3 Diamonds for 1 Apricorn).

### Dynamic Stock & Player Purchase Limits
- **Global Stock Limits**: Caps total server purchases before an item sells out.
- **Per-Player Quotas**: Limits individual purchases per cooldown cycle (e.g., max 2 Master Balls per player per week).

---

## 🛠️ In-Game Visual GUI Editor

Administrators can build entire commercial complexes without writing raw JSON:
- `/shop admin edit`: Opens the root editor menu.
- **`ShopListEditor`**: Create, clone, or delete shops.
- **`ProductEditor`**: Insert items from hand, adjust buy/sell prices, attach sound effects, and define permission condition gates.

---

## ⌨️ Command & Subcommand Reference

### Player Commands (`/shop`)
| Command | Arguments | Permission Node | Description |
| :--- | :--- | :--- | :--- |
| `/shop` | *None* | `ultrashop.user` | Opens the main shop category hub. |
| `/shop <shop_id>` | `<shop_id>` | `ultrashop.user` | Directly opens a specific shop by ID. |
| `/shop search` | `<query>` | `ultrashop.user` | Searches all shops for items matching the search query. |
| `/shop sell` | *None* | `ultrashop.user` | Opens quick-sell inventory GUI. |

### Administrator Commands (`/shop admin`)
| Command | Arguments | Permission Node | Description |
| :--- | :--- | :--- | :--- |
| `/shop admin edit` | *None* | `ultrashop.admin` | Opens in-game visual shop and product editor. |
| `/shop admin reload`| *None* | `ultrashop.admin` | Hot-reloads all shop JSONs, language, and configs. |
| `/shop admin stats` | *None* | `ultrashop.admin` | Displays total economic turnover and sales metrics. |

---

## 🛡️ Permissions & Privilege Hierarchy

```
ultrashop.user                   # Base permission allowing players to open shops
├── ultrashop.sell               # Allows selling items via shop sell menus
└── ultrashop.search             # Allows global keyword search across shops

ultrashop.admin                  # Administrative management
├── ultrashop.admin.edit         # Access to visual GUI editor
├── ultrashop.bypass.limit       # Bypasses player stock purchase limits
└── ultrashop.bypass.condition   # Bypasses rank and permission purchase conditions
```

---

## 📢 Discord Webhook Dispatcher

When significant transactions occur or rotation shops refresh, `DiscordWebhookHelper` dispatches embed alerts detailing buyer, shop, item, and total currency exchanged.

---

## 💾 Database & Persistence Subsystem

- **`JSON`**: Stores shop configurations and user stock quotas under `config/ultrashop/`.
- **`MONGODB`**: High-concurrency clustering supporting network-wide synchronized stock counters and transaction history.

---

## ⚙️ Configuration Reference (`config.json`)

| Field | Type | Default | Description |
| :--- | :--- | :--- | :--- |
| `debug` | `Boolean` | `false` | Enables domain query and serialization logging. |
| `lang` | `String` | `"en"` | Language file identifier. |
| `database.type` | `String` | `"JSON"` | Persistence backend: `"JSON"` or `"MONGODB"`. |
| `dashboard.enabled` | `Boolean` | `false` | Enables embedded web dashboard HTTP server. |
| `dashboard.port` | `Integer` | `8085` | Port for the embedded HTTP dashboard. |

---

## 📋 Enterprise Production Templates

### Production `config.json` Template
```json
{
  "debug": false,
  "lang": "en",
  "database": {
    "type": "MONGODB",
    "database": "ultrashop",
    "url": "mongodb://shopAdmin:SecureShopPass123@10.0.0.5:27017/ultrashop?authSource=admin"
  },
  "dashboard": {
    "enabled": true,
    "port": 8085,
    "secretToken": "SecureWebDashboardToken789"
  },
  "commands": [
    "shop",
    "tienda",
    "store"
  ]
}
```
