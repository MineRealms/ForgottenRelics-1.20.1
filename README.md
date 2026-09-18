# Forgotten Relics · 遗忘遗物 — 1.20.1 移植

[中文](#中文) | [English](#english)

---

## 中文

### 这是什么

**Forgotten Relics（遗忘遗物）** 是 Integral（Extegral / VictorShadow）为 **Thaumcraft 4（神秘时代 4）** 制作的 1.7.10 扩展模组：以中后期为主的强力遗物，包含战斗、生存、位移、念力、放逐与死亡保护等一大批独特机制。

本仓库是它的 **非官方 1.20.1 Forge 重构移植**：

- 36 件遗物、9 类自定义实体（法球、导弹、能量精灵、巴比伦武器等）、6+2 种数据驱动伤害类型
- 研究/要素接入 1.20.1 数据包体系，支持「遗忘知识/正义裁决」隐藏研究触发器
- 完整移植 Botania 1.7.10 的 wisp/sparkle 粒子渲染（自定义 `ParticleRenderType`）
- 汉化（`zh_cn`）与英文本地化，保留原作者俄语文本

### 联动

| 模组 | 关系 | 说明 |
|---|---|---|
| **Thaumcraft 4R**（`dev.tc4port.thaumcraft`） | 硬依赖 | 要素、研究、法杖 Vis、扫描、Warp、装备接口等全部通过其 API；本移植基于其 1.20.1 重构包开发 |
| **Curios** | 硬依赖 | 替代 1.7.10 的 Baubles：护符/戒指/腰带饰品槽 |
| **Botania（植物魔法）** | 硬依赖 | 恐惧王冠用魔力修复；移植的粒子系统源自 Botania（1.7.10 版） |
| **JEI** | 可选 | 「湮灭之钥铭刻」自定义配方分类与信息页；TC4R 自带的 JEI 插件负责灌注等分类 |
| **KubeJS** | 可选 | 替代 1.7.10 的 MineTweaker3：正义触发器脚本绑定 + `ServerEvents.highPriorityData` 研究覆盖（示例见 `kubejs_examples/`） |
| TerraBlender / Patchouli | 间接 | 分别为 Thaumcraft 4R 与 Botania 的运行依赖 |

### 构建

1. JDK 17
2. 将 Thaumcraft 4R 开发包放入 `local-repo/dev/tc4port/thaumcraft-forge/0.1.0-20711/thaumcraft-forge-0.1.0-20711.jar`（说明见 `local-repo/README.md`）
3. `gradlew build`（产物在 `build/libs/`）；开发运行 `gradlew runClient`

### 状态

可运行的开发构建：所有物品/实体/网络/事件链路已打通；研究数据已转换主要条目；已知问题：Thaumcraft 4R 自带的 JEI 插件与公开 JEI 15.x 存在上游 API 不兼容（不影响本模组的 JEI 联动）。详见 `docs/MIGRATION_FEASIBILITY.md` 与 git 历史。

### 致谢

- **Integral（Extegral / VictorShadow）** — 原模组《Forgotten Relics》的作者。本移植是对其作品的致敬与延续，所有原创设计、贴图与文本版权归原作者所有；原项目已无限期冻结。
- **Thaumcraft 4R 重构作者们** — 提供了完整的 1.20.1 API 与开发包，本移植得以成立。
- **Vazkii 与 Botania 贡献者** — 粒子/FX 的实现源头（本仓库内的 wisp/sparkle 粒子为 1.20.1 重写版）。
- **GTNH 与所有 1.7.10 生态维护者** — 原版物品逻辑参考。

---

## English

### What is this

**Forgotten Relics** is a 1.7.10 addon for **Thaumcraft 4**, originally created by Integral (a.k.a. Extegral / VictorShadow). It adds a large set of mid- to endgame relics: combat tools, survival baubles, teleportation, telekinesis, banishment and death-prevention mechanics.

This repository is an **unofficial reconstruction port to Minecraft 1.20.1 / Forge**:

- 36 relics, 9 custom entities, 6+2 data-driven damage types
- Research and aspects rebuilt on the 1.20.1 data-pack system, including the "forgotten knowledge / justice" hidden-research trigger system
- A full port of Botania 1.7.10's wisp/sparkle particle rendering (custom `ParticleRenderType`s)
- Chinese and English localisation (the original Russian text is preserved)

### Integrations

| Mod | Kind | Notes |
|---|---|---|
| **Thaumcraft 4R** (`dev.tc4port.thaumcraft`) | required | aspects, research, wand vis, scanning, warp, gear hooks; this port is built against its 1.20.1 reconstruction |
| **Curios** | required | replaces Baubles (amulet / ring / belt slots) |
| **Botania** | required | mana-based Terror Crown repair; the ported particle system originates from Botania 1.7.10 |
| **JEI** | optional | custom "Oblivion Stone binding" category and info page; TC4R's own JEI plugin handles infusion categories |
| **KubeJS** | optional | replaces MineTweaker3: justice-trigger bindings plus `ServerEvents.highPriorityData` research overrides (see `kubejs_examples/`) |
| TerraBlender / Patchouli | transitive | runtime dependencies of Thaumcraft 4R and Botania |

### Building

1. JDK 17
2. Place the Thaumcraft 4R development jar at `local-repo/dev/tc4port/thaumcraft-forge/0.1.0-20711/thaumcraft-forge-0.1.0-20711.jar` (see `local-repo/README.md`)
3. `gradlew build` (output in `build/libs/`), or `gradlew runClient` for development

### Status

A runnable development build: items, entities, networking and event chains are wired up; the main research entries are converted. Known issue: the JEI plugin shipped inside Thaumcraft 4R is compiled against an unpublished JEI API class and fails to load on public JEI 15.x — this does not affect this mod's own JEI integration. See `docs/MIGRATION_FEASIBILITY.md` and the git history.

### Credits

- **Integral (Extegral / VictorShadow)** — author of the original *Forgotten Relics*. This port is a tribute and continuation; all original designs, textures and texts remain the property of the original author. The upstream project is frozen indefinitely.
- **The Thaumcraft 4R reconstruction contributors** — for the complete 1.20.1 API and dev bundle this port builds upon.
- **Vazkii and the Botania contributors** — source of the particle/FX behaviour (the wisp/sparkle particles here are 1.20.1 rewrites).
- **GTNH and the wider 1.7.10 ecosystem** — reference for the original item logic.
