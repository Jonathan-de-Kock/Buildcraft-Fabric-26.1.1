# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

BuildCraft-Fabric is a port of the BuildCraft Minecraft mod to the Fabric mod loader, targeting **Minecraft 26.1.1** with **Java 25**. It uses Fabric Loom 1.16.1, Fabric Loader 0.18.6, and Fabric API 0.145.4. License: MPL-2.0.

## Build Commands

```bash
./gradlew build                    # Build all modules
./gradlew :buildcraft-core:build   # Build a single module
./gradlew runClient                # Launch Minecraft client with the mod (from root)
./gradlew runServer                # Launch dedicated server with the mod
./gradlew genSources               # Decompile Minecraft sources for IDE navigation
```

There are no tests configured. The `run/` directories for dev runtime exist at `buildcraft-core/run/` and `buildcraft-all/run/`.

## Module Architecture

10-module Gradle project with Kotlin DSL (`build.gradle.kts`). Dependency chain:

```
buildcraft-api          (pure API interfaces, no mod dependencies)
  └─ buildcraft-lib     (utilities, registration, networking, energy; depends on Team Reborn Energy)
      └─ buildcraft-core (core blocks/items, engines, creative tab, commands)
          ├─ buildcraft-transport  (pipe networks: item/fluid/power pipes)
          ├─ buildcraft-energy     (energy generation - stub)
          ├─ buildcraft-factory    (industrial machines: pump, tank, mining well, etc.)
          ├─ buildcraft-silicon    (logic gates, assembly tables, lasers; also depends on transport)
          ├─ buildcraft-builders   (quarry, builder, filler, blueprints)
          └─ buildcraft-robotics   (robots, zone planner; depends on transport + silicon)
buildcraft-all          (aggregator - depends on all modules)
```

Each module has its own `fabric.mod.json`, mod ID (e.g., `buildcraftcore`, `buildcrafttransport`), and entry points.

## Key Patterns

### Module Entry Points
Each module follows `BC{Name}` / `BC{Name}Client` pattern implementing `ModInitializer` / `ClientModInitializer`. Initialization order in `onInitialize()`:
1. `BC{Name}Blocks.register()`
2. `BC{Name}BlockEntities.register()`
3. `BC{Name}Items.register()`
4. Creative tab registration

### Block/Item Registration (MC 26.1 requirement)
Use `BCRegistration` from `buildcraft.lib.registry`. In MC 26.1, you **must** set the ID on `BlockBehaviour.Properties` before constructing the block. Use `BCRegistration.registerBlock()`, `registerItem()`, `registerBlockAndItem()`, or `registerBlockEntity()`.

### Networking
`BCNetworking` in `buildcraft.lib.net` wraps Fabric Networking API v1. Block entities implement `IPayloadReceiver` to handle `BlockEntityUpdatePayload`. Payloads registered in `BCLib.onInitialize()`, client handlers in `BCLibClient.onInitializeClient()`.

### Pipe System (transport module)
Central to the mod. Key types in `buildcraft.api.transport.pipe`:
- `PipeDefinition` - pipe type template
- `PipeFlowType` - flow enum (items, fluids, power, structure)
- `PipeApi` / `PipeRegistryImpl` - pipe registry

Flow implementations in `buildcraft.transport.pipe.flow`: `PipeFlowItems`, `PipeFlowFluids`, `PipeFlowPower`, `PipeFlowStructure`.

## Resource Layout

Per module: `src/main/resources/assets/buildcraft{name}/` (textures, models, blockstates, lang) and `src/main/resources/data/buildcraft{name}/` (recipes, tags). No mixins are used; the project relies entirely on Fabric API hooks and events.

## Version Properties

All version constants live in `gradle.properties` at the root. Module build files reference them via `val prop: String by rootProject`.
