# PvM HUD

Compact, draggable overlay for the combat information you check constantly: Hitpoints, Prayer, special attack, spell states, and cooldowns.

### Chips layout that pairs well with Compact Orbs, with readable stats and spells that only appear when active.
![PvM HUD Chips style](images/chips.png)

---

## Features

- Tracks boosted **Hitpoints, Prayer, Special Attack, poison, and venom**
- Tracks **Thrall, Vengeance, Death Charge, Mark of Darkness, Corruption, Ward of Arceuus, and Heart**
- Multiple HUD styles: **Text, Game Icons, Bars, Chips, Orbs, Stack**
- Supports **horizontal and vertical layouts**
- Optional **local-only overhead alerts** (HP, Prayer, Spec)
- Highly configurable: **thresholds, colours, spacing, fonts, opacity, flashing**

---

## HUD Styles

- **Text** — minimal text-only layout  
- **Game Icons** — spell icons + stat icons with values  
- **Bars** — HP/Prayer/Spec bars with spell tiles  
- **Chips** — compact stat blocks with icons  
- **Orbs** — circular stat display with grouped spells  
- **Stack** — narrow vertical layout for tight spaces  

---

## Tracked States

- **Stats** — boosted values, poison/venom, threshold alerts  
- **Thrall** — duration, cooldown, expiry warning, reliable recast tracking  
- **Vengeance** — active + cooldown  
- **Death Charge** — active, consumed, cooldown, expiry warning  
- **Mark of Darkness** — active, expiring, faded  
- **Corruption** — cooldown  
- **Ward of Arceuus** — active duration (estimated) + cooldown  
- **Heart** — shared Imbued/Saturated cooldown  

---

## Changelog

### v1.3

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

### v1.2

- Improved Thrall tracking reliability and despawn handling  
- Reduced false clears from other players/NPCs  
- Removed unreliable Thrall varbit dependency  

---

### v1.1

- Added multiple HUD styles and layouts  
- Added overhead alerts for HP and Prayer  
- Added extensive configuration options  
- Improved layout alignment and rendering  
- Fixed overlay and opacity issues  

---

## Notes

- Overhead alerts are **local-only** and do not send chat messages  
- Some durations (Thrall, Ward) are **estimated based on stats**  