# BuildCraft for Fabric

An unofficial port of [BuildCraft](https://github.com/BuildCraft/BuildCraft) to the **Fabric mod loader**, targeting **Minecraft 26.1.1** with **Java 25**.

BuildCraft is one of Minecraft's oldest and most iconic automation mods, originally created by SpaceToad. It introduced pipes, engines, quarries, and automated building systems that shaped the modded Minecraft experience for over a decade. The original mod was built for Minecraft Forge and last officially supported Minecraft 1.12.2.

This project aims to bring BuildCraft back to life on modern Minecraft using the Fabric ecosystem.

## What is BuildCraft?

BuildCraft adds automation, logistics, and construction systems to Minecraft:

- **Pipes** — Transport items, fluids, and power between machines and inventories
- **Engines** — Generate Minecraft Joules (MJ) power to fuel machines and pipes
- **Quarry** — Automatically mine large areas down to bedrock
- **Builder & Filler** — Construct and fill structures from blueprints and patterns
- **Factory Machines** — Pumps, tanks, mining wells, auto workbenches, and more
- **Silicon & Gates** — Programmable logic on pipes with triggers and actions
- **Assembly System** — Laser-powered crafting for advanced components
- **Robotics** — Zone planning and automated robot workers

## Project Status

This port is a **work in progress**. The module structure, block/item registration, networking, and build system are all in place. Visual parity is largely complete, with proper textures and 3D models matching the original. Core gameplay systems (pipe transport, machine logic, power generation) are being implemented phase by phase.

See [ROADMAP.md](ROADMAP.md) for the full implementation plan and current progress.

## Modules

The mod is split into 8 functional modules, mirroring the original architecture:

| Module | Description |
|--------|-------------|
| **buildcraft-api** | Public API interfaces for cross-mod compatibility |
| **buildcraft-lib** | Shared utilities, registration, networking, energy system |
| **buildcraft-core** | Engines, markers, wrench, gears, creative tab |
| **buildcraft-transport** | Pipe networks for items, fluids, and power |
| **buildcraft-factory** | Industrial machines: pump, tank, mining well, auto workbench |
| **buildcraft-energy** | Oil & fuel fluids, combustion engines, world generation |
| **buildcraft-silicon** | Assembly tables, lasers, gates, integration tables |
| **buildcraft-builders** | Quarry, filler, builder, architect table, blueprints |
| **buildcraft-robotics** | Zone planner, robot infrastructure |
| **buildcraft-all** | Aggregator module that bundles everything |

## Building

Requires **Java 25** and **Minecraft 26.1.1**.

```bash
./gradlew build                        # Build all modules
./gradlew :buildcraft-all:runClient    # Launch Minecraft with the full mod
./gradlew clean build                  # Clean rebuild
```

## License

This project is licensed under the [Mozilla Public License 2.0](https://www.mozilla.org/en-US/MPL/2.0/), the same license as the original BuildCraft.

## Credits

- **SpaceToad** and the **BuildCraft Team** — Original mod (BuildCraft 8.0.x for Minecraft 1.12.2)
- This port is an independent community effort and is not affiliated with or endorsed by the original BuildCraft team
