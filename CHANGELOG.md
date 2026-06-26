# Changelog

## v1.7

- Added a separate draggable potion timer overlay for CoX overloads, ToA salts, and divine combat potions
- Added a separate draggable combat boost overlay for Attack, Strength, Defence, Ranged, and Magic boosts
- Added boost percentage colouring and configurable low-boost overhead alerts
- Added expiring and expired overhead alerts for timed potions
- Removed Orbs and Stack HUD styles from the codebase

---

## v1.6

- Replaced remaining raw varplayer and varbit IDs with RuneLite gameval constants
- Removed the redundant game-state ID wrapper and unused tracker cache methods
- Deduplicated Hitpoints and Prayer event caching into a shared tracker
- Simplified tracker event registration and shutdown bookkeeping
- Switched tracker cache expiry checks to a monotonic clock
- Cleared cached resized HUD icons when the plugin shuts down
- Added the plugin version to `runelite-plugin.properties`

---

## v1.5

- Moved release history out of the main README into this dedicated changelog
- Fixed HP, Prayer, and Spec overhead alerts firing from login-state synchronization
- Delayed alert baselining until the client has a logged-in local player
- Evaluated Special Attack alert changes on client ticks for same-game-tick responsiveness
- Filtered Special Attack alert handling to the Special Attack energy varplayer
- Improved Special Attack restore detection to ignore out-of-combat full restores such as POH pools
- Preserved alerts for legitimate incremental Spec restores, including natural regen, Lightbearer, Surge potion, and Death Charge
- Allowed large Special Attack restores when there is current or recent combat context
- Replaced the raw Special Attack varplayer ID with the RuneLite gameval constant

---

## v1.4

- Reorganized the plugin into feature packages (`runtime`, `alerts`, `overlay`, `tracking`)
- Moved runtime lifecycle and tracker registration into dedicated runtime classes
- Split overhead alerts into focused state, logic, and message-rendering components
- Moved `HudStyle` and `HudFont` into the overlay package
- Improved Special Attack overhead alert threshold-crossing reliability
- Reduced false Spec alerts from likely full-restore events
- Added safer Special Attack restore handling for Death Charge + passive regen overlaps
- Added Yama flare/spec-restore handling support in Special Attack overhead logic
- Refactored HUD renderers to share common drawing/layout helpers and reduce duplicate code
- Reduced per-frame allocations and unnecessary list work across overlay render paths
- Simplified tracker registry wiring and runtime access patterns
- Fixed Chips settings behavior so spacing controls consistently affect chip rows/items
- Standardized horizontal ordering so spells/cooldowns render above stats across HUD styles
- Fixed Stack layout stat rows showing an unwanted divider bar beside stat icons
- Clarified config descriptions so each setting states which HUD styles it affects
- Updated default HUD colors for brighter readability with stronger separation (especially Poisoned vs Venomed HP)

---

## v1.3

- Full overlay refactor into a modular system (builders, renderers, state handling)
- Reworked all trackers to use single reliable data sources
- Fixed Thrall tracking (recast, desync, first-cast break, cooldown flicker)
- Fixed Death Charge getting stuck active
- Fixed Vengeance and Ward of Arceuus state inconsistencies
- Fixed Game Icons layout (stats show icon + value, spells are icon-only)
- Fixed Text layout spacing and overlapping labels
- Fixed Chips layout alignment, sizing, and icon centering
- Fixed visual state priority (active overrides cooldown/ready)
- Fixed flickering and incorrect cooldown colouring
- Fixed layout shifting when stats change
- Fixed overhead alerts (colour, login triggers, overwrites, priority)
- Improved expiry warning and flashing behaviour
- Fixed tracker resets during loading/instances
- Improved icon loading and caching
- General performance and stability improvements

---

## v1.2

- Improved Thrall tracking reliability and despawn handling
- Reduced false clears from other players/NPCs
- Removed unreliable Thrall varbit dependency

---

## v1.1

- Added multiple HUD styles and layouts
- Added overhead alerts for HP and Prayer
- Added extensive configuration options
- Improved layout alignment and rendering
- Fixed overlay and opacity issues
