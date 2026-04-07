# BuildCraft Fabric Port — Parity Roadmap

## Current State Summary

| | Reference (1.12.2/Forge) | Port (26.1/Fabric) | Gap |
|---|---|---|---|
| **Java files** | 1,062 | 149 | ~86% missing |
| **Blocks** | 41 | 24 | 17 missing |
| **Items** | 60+ | 20 | 40+ missing |
| **Tile/Block Entities** | 42 | ~15 | 27 missing |
| **Pipe types** | 40+ | 14 | 26+ missing |
| **Textures** | 985 | ~52 | ~95% missing/broken |
| **Models** | 414 | ~136 | ~67% missing |
| **Recipes** | 52 | 0 | 100% missing |
| **GUIs/Containers** | 98/27 | 0 | 100% missing |
| **Statements (gates)** | 79 | 0 | 100% missing |

The port has correct scaffolding (module structure, registration, networking, build system) but is functionally hollow — pipe flows don't move anything, machines don't operate, many textures are broken placeholders, and there are zero GUIs or recipes.

---

## Phase 0: Fix Broken Foundations (Textures & Models) ✅ COMPLETE

Nothing else matters if items/blocks are invisible in-game. This is the fastest way to make the mod *feel* like BuildCraft.

- [x] **Replace placeholder textures** — Factory (6→18 textures), silicon (4→12), builders (5→24), robotics (3→5) upgraded from single-color placeholders to proper multi-face textures copied from the reference project.
- [x] **Fix pipe item models** — Verified all 14 pipe item textures are already the correct originals from BuildCraft (small file sizes are genuine 16x16 sprites, not placeholders).
- [x] **Audit all blockstate → model → texture chains** — All 24 blockstates, 44 item models, and 68 total model files verified. Zero broken references.
- [x] **Port missing textures for existing blocks** — Verified core (engines, markers, gears, wrench), energy (oil, fuel), and transport textures are all identical to reference originals. No changes needed.

**Deliverable:** Every registered block/item renders its correct BuildCraft texture in-game.

---

## Phase 1: Core Infrastructure (Lib + Core)

Everything downstream depends on these systems. Without GUIs, energy transfer, and config, no machine or pipe can function.

### 1a. GUI Framework

- [ ] Port `buildcraft.lib.gui` — the container/screen framework (27 containers, 98 GUI classes in reference)
- [ ] Implement `ContainerBC_Neptune` base class → Fabric `ScreenHandler`
- [ ] Implement `GuiBC8` base class → Fabric `HandledScreen`
- [ ] Widget system: buttons, slots, fluid gauges, energy bars, ledgers
- [ ] **Priority GUIs:** Engine GUI (shows power output), Tank GUI (shows fluid level)

### 1b. MJ Power System

- [ ] Full port of `buildcraft.api.mj` — `IMjReceiver`, `IMjConnector`, `IMjPassiveProvider`, `IMjRedstoneReceiver`
- [ ] `MjAPI` utilities, `MjBattery` storage
- [ ] Replace the current stub `BCEnergyStorage` with proper MJ implementation
- [ ] MJ ↔ Fabric Energy API bridge (Team Reborn Energy interop)

### 1c. Engine Logic

- [ ] `TileEngineBase_BC8` → proper engine tick cycle (warmup → green → yellow → red → overheat)
- [ ] Redstone Engine: produces MJ when powered by redstone
- [ ] Stone Engine: burns solid fuels, produces more MJ
- [ ] Iron Engine: burns liquid fuels (oil/fuel), highest output
- [ ] Creative Engine: infinite MJ
- [ ] Engine block model animation (piston movement per power stage)

### 1d. Config System

- [ ] Port per-module config classes (`BCCoreConfig`, `BCTransportConfig`, etc.)
- [ ] Use Fabric config or simple JSON config files
- [ ] Key configs: engine power output, pipe speeds, machine rates

### 1e. Missing Core Items & Blocks

- [ ] `ItemPaintbrush_BC8` (16 colors)
- [ ] `ItemList_BC8` (item sorting lists)
- [ ] `ItemMapLocation`
- [ ] `BlockSpring` (oil spring world gen)
- [ ] `BlockDecorated` variants

**Deliverable:** Engines actually produce power, GUIs open and display data, config is tunable.

---

## Phase 2: Transport — Make Pipes Work

Pipes are BuildCraft's identity. A BuildCraft port without working pipes is not BuildCraft.

### 2a. Dynamic Pipe Shapes

- [ ] Replace the hardcoded cross shape in `BlockPipeHolder` with connection-aware bounding boxes
- [ ] Center section + per-direction extension (match reference `PipeBlockModel`)
- [ ] Proper collision/selection shapes

### 2b. Item Pipe Flow (`PipeFlowItems`)

- [ ] Item extraction from adjacent inventories (wood pipe behavior)
- [ ] Item movement through pipe network with travel time
- [ ] Item rendering inside pipes (client-side entity-like rendering)
- [ ] Insertion into destination inventories
- [ ] Bounce-back when destination is full
- [ ] Speed system (gold pipes = faster)

### 2c. Fluid Pipe Flow (`PipeFlowFluids`)

- [ ] Fluid extraction from tanks/machines (wood pipe)
- [ ] Fluid transfer between connected pipes
- [ ] Fluid rendering inside pipes (partial fill levels)
- [ ] Insertion into destination tanks
- [ ] Capacity limits per pipe section

### 2d. Power Pipe Flow (`PipeFlowPower`)

- [ ] MJ transfer from engines to machines
- [ ] Power loss over distance
- [ ] Visual power level rendering (animated textures)

### 2e. Missing Pipe Behaviours

- [ ] **Gold** — speed multiplier for items, higher capacity for fluids
- [ ] **Iron** — configurable output direction (GUI or wrench)
- [ ] **Diamond** — item filtering (6-color filter GUI)
- [ ] **Diamond-Wood** — filtered extraction
- [ ] **Clay** — insertion priority (nearest inventory first)
- [ ] **Obsidian** — pickup items from world (entity suction)
- [ ] **Lapis** — color items passing through (16 dyeable variants)
- [ ] **Daizuli** — color + direction routing
- [ ] **Emzuli** — extraction with color assignment from gate signals
- [ ] **Stripes** — interact with world (place/break blocks)
- [ ] **Sandstone** — no connect to machines, pipe-to-pipe only

### 2f. Missing Pipe Types

- [ ] Add remaining fluid pipes: gold, iron, diamond, diamond-wood, sandstone, clay, void, obsidian
- [ ] Add remaining power pipes: gold, iron, diamond, diamond-wood, sandstone, quartz
- [ ] Add all RF power pipe variants (if RF compat desired)

### 2g. Pluggables & Wire

- [ ] `PlugBlocker` — blocks a pipe face
- [ ] `PlugPowerAdaptor` — converts between power systems
- [ ] Wire system — 16-color redstone-like signals along pipes
- [ ] Facade system (cosmetic covers for pipes — silicon module, but wire/pluggable infra here)

### 2h. Filtered Buffer Block

- [ ] Port `TileFilteredBuffer` — item buffer with per-slot filters

**Deliverable:** Items, fluids, and power actually move through pipes. All 40+ pipe types functional.

---

## Phase 3: Factory — Working Machines

### 3a. Tank

- [ ] Multi-block stacking (tanks on top of each other share fluid)
- [ ] Right-click with bucket to fill/drain
- [ ] Comparator output based on fill level
- [ ] Proper fluid rendering inside the tank block
- [ ] GUI showing fluid type and amount

### 3b. Pump

- [ ] Scan downward for fluid sources
- [ ] Remove source blocks and collect fluid
- [ ] Energy consumption per block pumped
- [ ] Tube blocks extending downward (visual)
- [ ] GUI with energy bar and fluid tank display

### 3c. Mining Well

- [ ] Break blocks directly below, one at a time
- [ ] Output mined items to adjacent inventory or drop
- [ ] Energy consumption per block broken
- [ ] Stop at bedrock

### 3d. Flood Gate

- [ ] Accept fluid input (from pipes)
- [ ] Place fluid source blocks in area below
- [ ] BFS flood-fill algorithm

### 3e. Chute

- [ ] Drop items downward from inventory above
- [ ] Insert items into inventory below
- [ ] Simple gravity-based item movement

### 3f. Auto Workbench

- [ ] Accept items from pipes/hoppers
- [ ] Auto-craft when recipe ingredients are satisfied
- [ ] Output crafted items
- [ ] GUI with crafting grid

### 3g. Distiller

- [ ] Two-fluid input/output processing
- [ ] Heat-based fluid conversion (crude oil → refined fuels)
- [ ] Energy consumption
- [ ] GUI

### 3h. Heat Exchange

- [ ] Transfer heat between fluids
- [ ] Multi-block structure

**Deliverable:** All factory machines operate, consume energy, and interact with pipes.

---

## Phase 4: Energy — Fuels & World Gen

### 4a. Proper Fluid Implementation

- [ ] Oil and Fuel as `FlowableFluid` (not static blocks)
- [ ] Bucket items for oil and fuel
- [ ] Fluid rendering, flowing behavior, source/flowing states
- [ ] All heat variants (crudeOil[], fuelLight[], fuelDense[], etc. — 31 fluid blocks total)
- [ ] Tar fluid

### 4b. Oil World Generation

- [ ] Oil springs (small surface pools)
- [ ] Oil wells (large underground reservoirs with surface spout)
- [ ] Biome-based distribution

### 4c. Engine Fuel System

- [ ] Stone engine: burn solid fuels (coal, wood, etc.) with burn time
- [ ] Iron engine: burn liquid fuels (oil, fuel) with different efficiency/output
- [ ] Fuel heat values and burn rates from reference

### 4d. MJ Dynamo Block

- [ ] Convert external energy (RF/FE) → MJ

**Deliverable:** Oil spawns in world, engines burn fuels, full energy chain works.

---

## Phase 5: Silicon — Logic & Assembly

### 5a. Laser

- [ ] Beam rendering (animated laser beam from laser to table)
- [ ] Energy transfer to adjacent laser tables
- [ ] Multi-laser support (multiple lasers pointing at one table)

### 5b. Assembly Table

- [ ] Laser-powered crafting
- [ ] Recipe system (assemble complex items from components)
- [ ] Queue multiple recipes
- [ ] GUI with recipe selection and progress

### 5c. Integration Table

- [ ] Combine items/pluggables (e.g., add gates to pipes)
- [ ] Recipe system
- [ ] GUI

### 5d. Gate System (Triggers & Actions)

This is the biggest sub-system in silicon.

- [ ] ~20 trigger types (inventory levels, fluid levels, power, redstone, machine state)
- [ ] ~15 action types (redstone output, pipe color, signal, extraction control)
- [ ] Gate items (AND/OR logic, 1-4 slots)
- [ ] Gate GUI (drag-and-drop trigger/action assignment)
- [ ] Gate rendering on pipes
- [ ] Statement providers per module

### 5e. Facades

- [ ] Cosmetic covers for pipes (any block appearance)
- [ ] Facade item crafting (any solid block → facade)
- [ ] Facade rendering (overlay block model on pipe face)

### 5f. Advanced Crafting Table

- [ ] Crafting table that remembers recipe, pulls from adjacent inventories

**Deliverable:** Laser-powered assembly, programmable gate logic on pipes, facades.

---

## Phase 6: Builders — Quarry & Construction

### 6a. Quarry

- [ ] Frame construction (builds visible quarry frame)
- [ ] Drill head (animated, descends through frame)
- [ ] Block breaking layer by layer
- [ ] Item collection (pipe output or internal)
- [ ] Energy consumption scaling with depth
- [ ] Chunk loading (optional)
- [ ] GUI with energy and progress display

### 6b. Filler

- [ ] Area definition (marker volume)
- [ ] 15+ fill patterns (box, pyramid, sphere, staircase, cylinder, frame, flatten, etc.)
- [ ] Pattern parameter UI (axis, hollow, center, direction)
- [ ] Energy consumption
- [ ] GUI

### 6c. Builder

- [ ] Blueprint/template loading
- [ ] Block-by-block construction
- [ ] Resource requirements from inventory
- [ ] GUI

### 6d. Architect Table

- [ ] Scan area (defined by markers)
- [ ] Save to blueprint/template item
- [ ] GUI

### 6e. Electronic Library

- [ ] Store/retrieve blueprints and templates
- [ ] GUI

### 6f. Supporting Blocks

- [ ] Frame block (construction frame, breakable)
- [ ] Replacer block

### 6g. Snapshot System

- [ ] Blueprint serialization (block + entity data)
- [ ] Template serialization (structure only, no block identity)
- [ ] Schematic handlers per block type

**Deliverable:** Quarry mines, filler fills, builder builds from blueprints.

---

## Phase 7: Robotics

### 7a. Zone Planner

- [ ] Map rendering UI (chunk-level zone selection)
- [ ] Zone data storage and chunk mapping
- [ ] Color-coded zone layers

### 7b. Robot Infrastructure (stretch goal)

- [ ] Robot entity
- [ ] Robot AI boards (mining, farming, building, etc.)
- [ ] Docking stations
- [ ] Programming table

> **Note:** The reference robotics module is the smallest (24 files) and was considered experimental in the original mod. This could be deferred or descoped.

**Deliverable:** Zone planner functional; robots stretch goal.

---

## Phase 8: Recipes, Polish & Compat

### 8a. Recipes

- [ ] Port all 52+ recipe JSONs (shaped, shapeless, smelting)
- [ ] Assembly table recipes (programmatic registration)
- [ ] Integration table recipes
- [ ] Distiller/Heat Exchange fluid recipes

### 8b. Expression System

- [ ] Port `sub_projects/expression/` for dynamic model variables
- [ ] Needed for animated engine pistons, fluid levels in models

### 8c. Localization

- [ ] Verify/complete `en_us.json` for all modules
- [ ] Port tooltip text, GUI labels, config descriptions

### 8d. Particles & Sounds

- [ ] Engine sounds
- [ ] Pipe item/fluid flow particles
- [ ] Laser beam particles
- [ ] Quarry drill sounds

### 8e. Commands

- [ ] `/buildcraft` debug commands
- [ ] Version info, config reload

### 8f. Compat & Integration

- [ ] Fabric Energy API (Team Reborn) ↔ MJ bridge
- [ ] WTHIT/Jade tooltips (show energy, fluid, pipe contents)
- [ ] REI integration (show assembly/integration recipes)

**Deliverable:** Fully polished, craftable, compatible mod.

---

## Recommended Priority Order

If working iteratively, each stage produces a more playable mod:

| Priority | Phase | Effort | Result |
|---|---|---|---|
| **P0** | Phase 0 — Textures | Small | Blocks/items look correct |
| **P1** | Phase 1a-b — GUI + MJ | Medium | Can open GUIs, power system exists |
| **P1** | Phase 1c — Engines | Medium | Engines produce power |
| **P2** | Phase 2a-d — Pipe flows | Large | Core pipe loop works (items/fluids/power move) |
| **P2** | Phase 2e-f — All pipe types | Medium | Full pipe variety |
| **P3** | Phase 3a-f — Factory | Medium | Machines actually do things |
| **P3** | Phase 4 — Energy/Fluids | Medium | Oil/fuel, engine fuel chain |
| **P4** | Phase 8a — Recipes | Small | Everything is craftable in survival |
| **P4** | Phase 5 — Silicon | Large | Gates, assembly, lasers |
| **P5** | Phase 6 — Builders | Large | Quarry, filler, builder |
| **P6** | Phase 7 — Robotics | Medium | Zone planner, robots |
| **P7** | Phase 8b-f — Polish | Medium | Animations, compat, sounds |

---

## Key Technical Challenges

1. **Pipe rendering** — The reference uses a custom `PipeBlockModel` with dynamic mesh generation. Fabric equivalent needs custom `BakedModel` or block entity renderer.
2. **Gate system** — 79 statement classes with complex interaction logic. Biggest single subsystem to port.
3. **Quarry frame** — Multi-block structure with animated drill, needs careful tick logic and rendering.
4. **Fluid system** — 31 fluid variants with heat levels. Fabric's `FlowableFluid` API differs significantly from Forge's `Fluid`.
5. **Expression system** — Has a code generator; may need rewrite or simplification for the new context.
6. **Forge → Fabric API mapping** — Capability system (Forge) → Fabric API Lookup. Event bus → Fabric events. Tile entities → Block entities with different lifecycle.
