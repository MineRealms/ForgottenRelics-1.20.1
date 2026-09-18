# 依赖与下载地址总表

本文件汇总 Forgotten Relics 1.20.1 移植工程及其参考生态用到的所有下载源、maven 坐标与本地路径。

---

## 1. 工具链

| 项 | 版本/路径 |
|---|---|
| JDK | 17（Adoptium `C:\Program Files\Eclipse Adoptium\jdk-17.0.18.8-hotspot`；机器 JAVA_HOME 是 21，工程内用 `org.gradle.java.home` 固定 17） |
| Gradle Wrapper | 8.8（MDK 自带；本地离线包 `H:\MinecraftMods\gradle-8.14.5-bin.zip`） |
| 构建命令 | `gradlew build` / `gradlew runClient`（工作目录 `H:\MinecraftMods\ForgottenRelics-1.20.1`） |

## 2. Forge MDK（1.20.1）

| 项 | 地址/路径 |
|---|---|
| MDK 下载 | https://maven.minecraftforge.net/net/minecraftforge/forge/1.20.1-47.4.23/forge-1.20.1-47.4.23-mdk.zip |
| Forge 版本 | `net.minecraftforge:forge:1.20.1-47.4.23`（maven: https://maven.minecraftforge.net/） |
| MDK 解压位置 | `H:\MinecraftMods\ForgottenRelics-1.20.1`（工程根目录） |
| MDK 临时 zip | `%TEMP%\opencode\forge-1.20.1-47.4.23-mdk.zip` |

## 3. 神秘时代 4R（Thaumcraft 4.2.3.5 1.20.1 重构包）—— 本地

| 项 | 路径 |
|---|---|
| 源码+API 包（dev 包） | `D:\Downloads\1.20.1-forge-20711-dev.zip`（`fm/tc/te/tm/tt` 的 sources + TC api jar） |
| 完整构建包 | `D:\Downloads\1.20.1-forge-20711.zip`（TC 18.9MB + 各附属 mod jar） |
| 解压目录 | `H:\MinecraftMods\FM-port-deps\{fm,tc,te,tm,tt}`、`full\`、`src-fm\`、`src-tc\`… |
| 工程内本地 maven | `H:\MinecraftMods\ForgottenRelics-1.20.1\local-repo\dev\tc4port\thaumcraft-forge\0.1.0-20711\thaumcraft-forge-0.1.0-20711.jar` |
| 依赖写法 | `compileOnly/runtimeOnly fg.deobf("dev.tc4port:thaumcraft-forge:0.1.0-20711")`（SRG 生产 jar，需 `fg.deobf`） |
| TC4 1.7.10 原始代码 | `H:\MinecraftMods\TC4\decompiled-Thaumcraft-1.7.10-4.2.3.5.zip`、`TC4\thaumcraft-4.2.3.5-1.21.1-port.0.1.0-*.sources.jar`、`TC4\MIGRATION_REPORT_1~4.md` |

## 4. 本工程 maven 依赖（全部验证可用）

| 依赖 | 仓库 | 坐标 | 备注 |
|---|---|---|---|
| Curios | https://maven.theillusivec4.top/ | `top.theillusivec4.curios:curios-forge:5.14.1+1.20.1`（+ `:api`） | 替代 Baubles |
| Botania | https://maven.blamejared.com/ | `vazkii.botania:Botania:1.20.1-456-FORGE` | |
| Patchouli | https://maven.blamejared.com/ | `vazkii.patchouli:Patchouli:1.20.1-84-FORGE` | Botania 运行依赖 |
| JEI | https://maven.blamejared.com/ | `mezz.jei:jei-1.20.1-forge:15.59.0.212`、`jei-1.20.1-forge-api`、`jei-1.20.1-common-api` | **必须 ≥15.59.0.212**：TC4R 的 JEI 插件需要后期才恢复的 `ISubtypeInterpreter`；15.20/15.2/15.0 会报 `NoClassDefFoundError` |
| KubeJS | https://maven.latvian.dev/releases | `dev.latvian.mods:kubejs-forge:2001.6.5-build.26` | 替代 MineTweaker3 |
| Rhino | https://maven.latvian.dev/releases | `dev.latvian.mods:rhino-forge:2001.2.3-build.10` | KubeJS 依赖 |
| Architectury | https://maven.architectury.dev/ | `dev.architectury:architectury-forge:9.2.14` | KubeJS 依赖 |
| TerraBlender | https://maven.minecraftforge.net/ | `com.github.glitchfiend:TerraBlender-forge:1.20.1-3.0.1.10` | TC4R 运行依赖 |

> 备用本地 jar（Downloads 里已有）：`kubejs-forge-2001.6.5-build.26.jar`、`rhino-forge-2001.2.3-build.10.jar`、`architectury-9.2.14-forge.jar`。

## 5. GTCEU（GregTech CEu Modern，工作区其他工程用）

| 项 | 地址/路径 |
|---|---|
| 官方 maven | https://maven.gtceu.com （README：`includeGroup 'com.gregtechceu.gtceu'`） |
| 坐标格式 | `com.gregtechceu.gtceu:gtceu-<mc_version>:<version>`，如 `com.gregtechceu.gtceu:gtceu-1.20.1:<version>`（Forge 需 `fg.deobf`） |
| 本地版本 | Gradle 缓存里见 `gtceu-1.20.1` 的 `1.8.0` 与 `7.5.3`：`~\.gradle\caches\forge_gradle\deobf_dependencies\blank\gtceu-1.20.1\` |
| 本地源码 | `H:\MinecraftMods\GregTech-Modern`（8.0.0）、`H:\MinecraftMods\GregTech-Modern-7.5.2`（7.5.2） |
| 反编译包 | `H:\MinecraftMods\gtceu-1.20.1-1.8.0-decompiled{,.zip}`、`gtocore-0.5.5-pre4-decompiled.zip`、`gtolib-1.0-decompiled.zip` |
| 其仓库清单 | 见 `GregTech-Modern-7.5.2\repositories.gradle`：BlameJared、modmaven、firstdark、createmod、latvian、ftb、cursemaven、terraformers、shedaniel、tterrag、theillusivec4 等 |

## 6. 参考源码（非依赖）

| 用途 | 路径 |
|---|---|
| 目标 mod 原版（1.7.10 Forgotten Relics） | `H:\MinecraftMods\Forgotten-Relics` |
| Botania 1.20.1 源码 | `H:\MinecraftMods\Botania`（多 loader：Xplat/Forge/Fabric） |
| Botania 1.7.10 源码（粒子/FX 移植来源） | `H:\MinecraftMods\Botania\1710Source\Botania\src\main` |
| Thaumic Tinkerer 参考 | `H:\MinecraftMods\ThaumicTinkerer`、`ThaumicTinkerer-1.21` |

## 7. 离线/缓存位置

| 项 | 路径 |
|---|---|
| Gradle modules 缓存 | `~\.gradle\caches\modules-2\files-2.1\`（JEI/Curios/Botania/KubeJS 等原始 jar） |
| ForgeGradle 反编译缓存 | `~\.gradle\caches\forge_gradle\deobf_dependencies\`（含 gtceu、ldlib 等历史工程） |
| MCP 映射 | `~\.gradle\caches\forge_gradle\mcp_repo\de\oceanlabs\mcp\mcp_config\1.20.1-20230612.114412\joined\downloadClientMappings\client_mappings.txt` |

## 8. 校验命令

```powershell
# 列出实际解析到的依赖版本
.\gradlew.bat dependencies --configuration compileClasspath
.\gradlew.bat dependencyInsight --dependency jei
```
