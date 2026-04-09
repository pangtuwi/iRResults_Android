# iRaceResults Color Themes

Color themes are configured per-league via the `color_theme` field in `data/{LEAGUEID}/config.json`. The theme is applied dynamically to `tables2.html` using CSS custom properties. The default theme is `"green"`.

Available themes: `green`, `orange`, `blue`, `red`, `purple`

---

## How Themes Work

Each theme defines a set of color values that are set as CSS variables on the document root at page load. The three base colors — `primary`, `primaryDark`, and `primaryDarker` — are solid hex colors used for main UI elements. The `neon` color is a brighter or lighter accent used for glow effects and highlights. All other properties are semi-transparent `rgba()` variants of these base colors, used for shadows, backgrounds, borders, and hover states.

| CSS Variable | Theme Property | Usage |
|---|---|---|
| `--primary-color` | `primary` | Main accent color — headers, badges, borders |
| `--primary-dark` | `primaryDark` | Darker shade — button hover states, gradient stops |
| `--primary-darker` | `primaryDarker` | Darkest shade — deep gradient stops |
| `--neon-color` | `neon` | Bright accent — total score glow, highlights |
| `--grid-color` | `gridColor` | Very faint background grid tint (3% opacity) |
| `--header-shadow` | `headerShadow` | Page header drop shadow (30% opacity) |
| `--btn-hover-bg` | `btnHoverBg` | Button background on hover (20% opacity) |
| `--btn-hover-shadow` | `btnHoverShadow` | Button shadow on hover (30% opacity) |
| `--btn-active-shadow` | `btnActiveShadow` | Button shadow when pressed (50% opacity) |
| `--table-header-bg` | `tableHeaderBg` | Table header row gradient background (10% opacity) |
| `--table-header-border` | `tableHeaderBorder` | Table header bottom border (20% opacity) |
| `--header-cell-hover` | `headerCellHover` | Table header cell hover highlight (15% opacity) |
| `--row-hover-shadow` | `rowHoverShadow` | Table row outer glow on hover (15% opacity) |
| `--row-hover-inset` | `rowHoverInset` | Table row inner inset shadow on hover (5% opacity) |
| `--points-hover-bg` | `pointsHoverBg` | Points cell background on hover (10% opacity) |
| `--best-score-bg` | `bestScoreBg` | Best score cell highlight gradient (20% opacity) |
| `--best-score-shadow` | `bestScoreShadow` | Best score cell shadow (30% opacity) |
| `--total-text-shadow` | `totalTextShadow` | Total score text glow (50% opacity, uses neon color) |
| `--mobile-total-bg` | `mobileTotalBg` | Mobile total row background (10% opacity) |
| `--mobile-total-border` | `mobileTotalBorder` | Mobile total row border (30% opacity) |
| `--scroll-btn-shadow` | `scrollBtnShadow` | Scroll button shadow (40% opacity) |
| `--scroll-btn-hover-shadow` | `scrollBtnHoverShadow` | Scroll button shadow on hover (60% opacity) |
| `--position-shadow` | `positionShadow` | Position badge shadow (20% opacity) |
| `--position-hover-shadow` | `positionHoverShadow` | Position badge shadow on hover (40% opacity) |

---

## Theme Reference

### green (default)

The original theme. Bright emerald green with a neon lime accent.

| Property | Hex / Value |
|---|---|
| `primary` | `#2ecc71` — Emerald green |
| `primaryDark` | `#27ae60` — Medium green |
| `primaryDarker` | `#1e8449` — Deep forest green |
| `neon` | `#00ff88` — Neon lime green |

Gradient direction on table header: green → steel blue (`rgba(52, 152, 219, 0.1)`)

---

### orange

Warm amber-orange theme.

| Property | Hex / Value |
|---|---|
| `primary` | `#e67e22` — Carrot orange |
| `primaryDark` | `#d35400` — Deep orange |
| `primaryDarker` | `#ba4a00` — Burnt orange |
| `neon` | `#ff8c42` — Bright peach-orange |

Gradient direction on table header: orange → warm red (`rgba(231, 76, 60, 0.1)`)

---

### blue

Cool steel blue theme.

| Property | Hex / Value |
|---|---|
| `primary` | `#3498db` — Peter River blue |
| `primaryDark` | `#2980b9` — Belize Hole blue |
| `primaryDarker` | `#21618c` — Deep navy blue |
| `neon` | `#5dade2` — Sky blue |

Gradient direction on table header: blue → darker blue (`rgba(41, 128, 185, 0.1)`)

---

### red

Coral-red theme. Designed to match Type Two Racing branding.

| Property | Hex / Value |
|---|---|
| `primary` | `#db4437` — Coral red |
| `primaryDark` | `#c53929` — Deep red |
| `primaryDarker` | `#a52714` — Dark crimson |
| `neon` | `#ff6b6b` — Bright coral |

Gradient direction on table header: coral red → bright coral (`rgba(255, 107, 107, 0.1)`)

Note: `bestScoreShadow` and `totalTextShadow` use the `neon` color (`#ff6b6b`) rather than `primary`, giving highlights a brighter coral tone.

---

### purple

Amethyst purple theme. Designed to match NXTGEN Racing Legends branding.

| Property | Hex / Value |
|---|---|
| `primary` | `#9b59b6` — Amethyst purple |
| `primaryDark` | `#8e44ad` — Wisteria purple |
| `primaryDarker` | `#7d3c98` — Deep purple |
| `neon` | `#bb6bd9` — Bright lavender |

Gradient direction on table header: amethyst → lavender (`rgba(187, 107, 217, 0.1)`)

Note: `bestScoreShadow` and `totalTextShadow` use the `neon` color (`#bb6bd9`) rather than `primary`, giving highlights a lighter lavender tone.

---

## Adding a New Theme

To add a new theme:

1. Add an entry to the `colorThemes` object in `html/tables2.html` following the same property structure as existing themes
2. Add the theme name as an `<option>` in the selector in `html/config.html`

The `applyColorTheme()` function will fall back to `green` if an unknown theme name is specified.
