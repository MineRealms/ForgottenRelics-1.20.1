# Forgotten Relics 1.20.1 迁移可行性与实施报告

- 目标源码：`H:\MinecraftMods\Forgotten-Relics`（1.7.3 Beta，MC 1.7.10，Forge 10.13.4.1614）
- 依赖基线：同生态的 Thaumcraft 4R 1.20.1 port（`1.20.1-forge-20711-dev.zip` / `-20711.zip`）
- 本工程：`H:\MinecraftMods\ForgottenRelics-1.20.1`（Forge 1.20.1-47.4.23，Java 17）
- 结论：**可行，且已产出可编译、可打出 jar 的 0.1.0 骨架**

---

## 1. 原 mod 架构分析

103 个 Java 文件，结构非常清晰：

| 包 | 内容 | 规模 |
|---|---|---|
| `Main` | 单例 mod 类：45 物品静态注册、9 实体、24 网络包、两个事件处理器、代理 | 376 行 |
| `items/` | 45 件遗物；`ItemBaubleBase` 是 Botania `ItemBauble` 的复刻版（Baubles 饰品） | 45 文件 |
| `entities/` | 9 个投射物/球体 + `FXBurst`/`FXWisp` 粒子 | 11 文件 |
| `handlers/` | 配置(23KB)、事件(21KB)、SuperpositionHandler(22KB，研究/触发器核心)、JusticeHandler、传送器、区块加载、实体材料 | 14 文件 |
| `packets/` | 24 个 `SimpleNetworkWrapper` 消息（1.7.10 IMessage） | 25 文件 |
| `research/` | 硬编码方面注册（27KB）+ 研究注册（50KB，含灌注配方） | 3 文件 |
| `minetweaker/` | MineTweaker3 集成：`ResearchSuperset`、`JusticeHandlerInteraction`、命令、Reload 事件 | 5 文件 |
| `proxy/` | 客户端渲染（CrimsonOrb/BabylonWeapon 的 `ItemRenderer`）与按键 | 3 文件 |

外部依赖面：
- **Thaumcraft 4 API/内部类**：`Aspect/AspectList`、`ThaumcraftApi.registerObjectTag`、`ResearchItem/ResearchPage/ScanResult`、`IWarpingGear/IRunicArmor/IVisDiscountGear/IRepairable`、`WandManager/ItemWandCasting`、`ScanManager/ResearchManager`、`Config/ConfigBlocks/ConfigItems`、`EntityUtils`、客户端 `ParticleEngine/UtilsFX/renderers`
- **Baubles 1.0.1.16**：`IBauble/BaubleType`、`InventoryBaubles/PlayerHandler`
- **Botania r1.8-237+**：`Botania.proxy.wispFX/setExtraReach`、`EntityThrowableCopy/EntityDoppleganger`、`ICosmeticAttachable/IPhantomInkable`、`ItemNBTHelper`
- **MineTweaker3**：ZenScript 注册 + 命令

## 2. 迁移映射（已在工程中落地）

| 1.7.10 | 1.20.1 目标 | 状态 |
|---|---|---|
| `@Mod` + `GameRegistry.registerItem` | `DeferredRegister<Item>` + 注册事件 | ✅ |
| 物品 NBT（`NBTTagHelper`） | **TC4R `ItemStateKey` + `ItemStatePlatform`**（1.20.1 后端为 NBT，1.21 自动切组件） | ✅ 已按生态写法实现 |
| `IWarpingGear/IRevivable...` | TC4R `WarpingGear/RunicGear/VisDiscountGear/RepairableGear` + 数据包 map/tag | ✅ 接口已接（数据 map 待补） |
| `Aspect/AspectList` | `dev.tc4port.thaumcraft.api.aspect.*`（AspectId/VisChannel/VisCost） | ✅ |
| `WandManager` | `WandApi.insert/extract/craftingCost`、`ThaumcraftApiHelper.consumeVisFromInventory` | ✅（OmegaCore 已用） |
| 程序式研究/方面注册 | **数据包 JSON**（`data/forgottenrelics/research/*.json`、`thaumcraft/aspects/*.json`）+ `ResearchApi` 运行时 overlay | ⏳ 分类/条目待生成 |
| `ScanManager` | `ScanApi.isObjectScanned` + 数据 Map | ✅（Justice 已用） |
| `@JusticeHandler` 线程 | `PlayerTickEvent` 周期扫描 + `ResearchApi.discover` | ✅ |
| Baubles | **Curios 5.14.1**（`ICurioItem` + `curios:necklace/ring` 标签） | ✅ |
| Botania `setExtraReach` | 1.20.1 无此 API → 改用 `ForgeMod.BLOCK_REACH` 属性修饰符 | ⏳（MiningCharm 批次） |
| Botania `wispFX` | 无公开等价 API → 用原版粒子或 TC4R `ThaumcraftClientEffectsApi` | ⏳（实体/FX 批次） |
| MineTweaker 研究改旗 | 数据包覆盖：KubeJS `ServerEvents.highPriorityData`（等价且持久化） | ✅ 示例脚本已提供 |
| MineTweaker 正义触发器 | KubeJS bindings：`ForgottenRelics.addJusticeTrigger/obliterate...` | ✅ |
| 24× `IMessage` | `SimpleChannel`/`PacketDistributor`（现代网络层） | ⏳（投掷物批次） |
| GL11/`ItemRenderer`/`ModelBase` | `EntityRenderer` + `ModelPart`/GeckoLib，或 BER | ⏳ |
| `DamageSource` 子类 | 数据驱动 `damage_type` JSON + `DamageSource(Holder,...)` | ✅ 6 种伤害类型 JSON 已建 |

## 3. 已完成（0.1.0，可构建）

- 构建系统：Forge 1.20.1-47.4.23、Gradle 8.8、JDK17 toolchain、本地 maven（TC4R dev jar，`fg.deobf` 验证通过）
- 依赖接线：Curios 5.14.1、Botania 1.20.1-456、Patchouli、JEI 15.20、KubeJS 2001.6.5 + Rhino + Architectury、TerraBlender（全部从公网 maven 解析成功）
- 主类/DeferredRegister/创造模式标签页/完整 50+ 项 `ForgeConfigSpec`（沿用原 relics.cfg 键名与默认值）
- `ItemStateKey` 状态层：`supersolid`、`cooldown`、`fate_id`、`stored_damage`、`mode`
- 9 件物品 + Oblivion Stone：False Justice、Paradox、Deific Amulet、Oblivion Amulet、Fate Tome、Chaos Core、Omega Core、Ghastly Skull、Wastelayer（含原版战利品文本键）
- 遗忘知识系统：配置解析、`/forgottenknowledge` 命令、玩家 tick 扫描、`ResearchApi.discover` 授予
- Oblivion Stone 自定义合成（`forgottenrelics:oblivion_crafting`，SimpleCraftingRecipe）+ 三种原版工作模式移植
- **JEI**：`Oblivion Stone` 绑定配方分类 + 信息页；TC4R 自带 JEI 插件负责灌注配方
- **KubeJS**：plugin（`kubejs.plugins.txt`）+ `ForgottenRelics` bindings（正义触发器/旗标查询）+ `kubejs_examples` 研究覆盖脚本
- 资源：10 个物品模型、材质、en_US/ru_RU 原版语言文件（保持原键名）、6 个伤害类型、Curios/Thaumcraft 标签、KubeJS 插件清单
- 构建产物：`forgottenrelics-1.20.1-1.7.3-beta-1.20.1-port.0.1.0.jar`

## 4. 剩余工作量与批次建议

| 批次 | 内容 | 难度 |
|---|---|---|
| B1 物品批 2 | 剩余 ~35 件（Tome 系列的法术逻辑、Terror Crown、Overthrower、Apotheosis 等） | 中高（依赖 SuperpositionHandler 的 wand/研究逻辑） |
| B2 研究数据 | 由 `RelicsResearchRegistry` 生成 ~60 条 research JSON + 灌注配方 JSON | 机械但量大，可脚本化 |
| B3 网络层 | 24 个包的 SimpleChannel 迁移（大部分可改为无包：粒子走 `CustomPacketPayload`） | 中 |
| B4 实体/投射物 | 9 实体 + 数据化伤害 + `IEntityAdditionalSpawnData` → `defineSynchedData/addAdditionalSaveData` | 中 |
| B5 渲染/FX | 2 个实体渲染器、粒子、Botania FX 替代 | 高（1.7.10→1.20.1 渲染差异最大） |
| B6 事件系统 | `RelicsEventHandler`(21KB)/`SuperpositionHandler`(22KB)：真伤转换、守卫者、外域、通知 | 高（需逐一映射到 Forge 事件/TC4R API） |
| B7 数据 map | 静态 warp/runic/vis 折扣、物品方面映射（`data_maps`） | 中 |

## 5. 风险与已验证的结论

- ✅ TC4R dev jar 可直接作为 `fg.deobf` 本地依赖编译；运行时需同时放 TC4R + Curios + TerraBlender 到 `run/mods`（或由 Gradle runtimeOnly 注入）
- ✅ KubeJS 2001.6.5 的 `KubeJSPlugin#registerBindings`、`ServerEvents.highPriorityData` 均实测存在
- ⚠️ TC4R 的研究旗标**运行时只读**（数据包为准）——原 MineTweaker 的 `setHidden/setLost` 对应实现是覆盖对应 research JSON（示例已给），行为等价且更稳定
- ⚠️ Botania 1.20.1 移除了 `setExtraReach/wispFX/ICosmeticAttachable`，这几处必须换实现而非直接映射
- ⚠️ `Recipe#getId` 与 `SimpleCraftingRecipeSerializer` 工厂签名以当前 Forge 1.20.1 为准（已按 `(ResourceLocation, CraftingBookCategory)` 适配）
