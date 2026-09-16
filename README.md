<h1 align="center">🍩 aDonut</h1>

<p align="center">A modern, high-performance client-side automation mod tailored specifically for <b>DonutSMP</b> and economy servers, featuring an Elementa/Vigilance-inspired dual-pane cyber-violet glassmorphism UI.</p>

<p align="center">
<a href="https://modrinth.com/mod/adonut"><img alt="Modrinth" src="https://img.shields.io/badge/Modrinth-aDonut-00AF5C?logo=modrinth&logoColor=white"></a>
<a href="https://github.com/adrayim/aDonut/releases"><img alt="Supported Minecraft Versions" src="https://img.shields.io/badge/Available%20for-1.21%20%E2%80%93%2026.3-00AF5C"></a>
<a href="https://github.com/adrayim/aDonut/releases"><img alt="GitHub Downloads" src="https://img.shields.io/github/downloads/adrayim/aDonut/total?logo=github&logoColor=white&label=GitHub%20Downloads&color=6e5494"></a>
<a href="https://github.com/adrayim/aDonut/releases/latest"><img alt="GitHub Release" src="https://img.shields.io/github/v/release/adrayim/aDonut?logo=github&logoColor=white&label=Release&color=6e5494"></a>
<a href="https://github.com/adrayim/aDonut/issues"><img alt="GitHub Issues" src="https://img.shields.io/github/issues/adrayim/aDonut?logo=github&logoColor=white&label=Issues&color=blue"></a>
</p>

> [!NOTE]
> **DonutSMP Optimized**: **aDonut** is built specifically to streamline shop routines, inventory management, and bulk order drop operations with ultra-low latency and zero FPS impact.

> [!IMPORTANT]
> **Multi-Version Support**: aDonut is actively maintained for both modern **Minecraft 26.x (Wilderness Bound / Java 25+)** and **Minecraft 1.21.x (Java 21)**. Make sure to download the build matching your Minecraft client version.

> [!WARNING]
> Only download official aDonut releases directly from **[GitHub Releases](https://github.com/adrayim/aDonut/releases)** or **[Modrinth](https://modrinth.com/mod/adonut)**. Builds from untrusted third-party sites or unofficial Discord servers may be trojanized or malicious.

---

## ✨ Features

- **[Auto Sell](#auto-sell)** — Fast batch transfer and smart inventory selling with whitelist item selection.
- **[Auto Drop](#auto-drop)** — Automated item dumping and order fulfillment with slot configuration.
- **[Default Keybindings](#keybindings)** — Quick reference for in-game shortcuts and toggles.
- **[Requirements & Installation](#installation)** — Easy step-by-step setup guide for Fabric.
- **[Recommended Utility Mods](#recommended-mods)** — Privacy and anti-leak companion mods.

---

### <a id="auto-sell"></a>🛒 Auto Sell

- **Batch Transfer**: Instantly and safely moves all selected items from your inventory into server `/sell` chests or shop menus.
- **Custom Whitelist Selection**: Full in-game item picker with real-time search to choose exactly which items should be sold.
- **Customizable Delay**: Adjust the delay (in milliseconds) to fit your server's ping and rate limits.
- **Menu Title Matching**: Automatically detects custom shop titles and chest menus.

---

### <a id="auto-drop"></a>📦 Auto Drop (Order Fulfillment)

- **Automated Dumping**: Fast inventory clearing for server drop orders and bulk item hand-ins.
- **Configurable Drop & Next Slots**: Customize the exact target slot and next-page arrow slot.
- **Adjustable Speed**: Fine-tune delay (ms) between actions to prevent packet drops or anti-cheat triggers.

> [!TIP]
> **How to fulfill DonutSMP Orders with Auto Drop:**
> 1. Open the `/orders` menu.
> 2. Click on the item order you want to fulfill to enter that item's **deposit/drop sub-screen**.
> 3. Press your **Auto Drop keybind** (`R` by default) to automatically transfer your inventory into the order.

---

### <a id="keybindings"></a>⌨️ Default Keybindings

| Key | Action | Description |
| :--- | :--- | :--- |
| **`K`** | **Open Config** | Opens the modern aDonut configuration screen |
| **`G`** | **Toggle Auto Sell** | Toggles the Auto Sell module ON / OFF |
| **`R`** | **Toggle Auto Drop** | Toggles the Auto Drop module ON / OFF |

*(All keybinds can be rebound anytime under Minecraft `Options` → `Controls` → `Key Binds`)*

---

### <a id="installation"></a>📋 Requirements & Installation

- **Fabric Loader**: `>= 0.16.x` (or `>= 0.15.x` for 1.21.x)
- **Fabric API**: Matching your Minecraft version
- **Java**:
  - Java 25 / 26 for Minecraft 26.x
  - Java 21 for Minecraft 1.21.x

#### Steps:
1. Install [Fabric Loader](https://fabricmc.net/use/) for your Minecraft version.
2. Download the matching [Fabric API](https://modrinth.com/mod/fabric-api) for your client.
3. Download the latest `aDonut-[version].jar` from **[GitHub Releases](https://github.com/adrayim/aDonut/releases)**.
4. Place the `.jar` file into your `.minecraft/mods` directory.
5. Launch Minecraft and press `K` in-game to configure.

---

### 🛠️ Building from Source

Clone the repository and build using Gradle:

```bash
git clone https://github.com/adrayim/aDonut.git
cd aDonut
```

#### Minecraft 26.x:
```bash
cd "aDonut 26.x"
./gradlew build
```
*Output jar: `build/libs/aDonut-26.x-*.jar`*

#### Minecraft 1.21.x:
```bash
cd "aDonut 1.21.x"
./gradlew build
```
*Output jar: `build/libs/aDonut-1.21.x-*.jar`*

---

### <a id="recommended-mods"></a>🛡️ Recommended Security & Utility Mods

For maximum privacy, anti-leak protection, and safety on multiplayer servers, we recommend pairing **aDonut** with:

- **[OpSec Community Edition](https://github.com/tufkan1/OpSec)** - Modern community fork supporting Minecraft 1.20 – 26.3 with tracking exploit protection, channel spoofing, and account management.

---

## 📜 License

This project is licensed under the [MIT License](LICENSE).
