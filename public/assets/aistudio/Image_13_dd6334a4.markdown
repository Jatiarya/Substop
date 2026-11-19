---
name: SubStop Mobile Design System
colors:
  surface: '#faf8ff'
  surface-dim: '#d2d9f4'
  surface-bright: '#faf8ff'
  surface-container-lowest: '#ffffff'
  surface-container-low: '#f2f3ff'
  surface-container: '#eaedff'
  surface-container-high: '#e2e7ff'
  surface-container-highest: '#dae2fd'
  on-surface: '#131b2e'
  on-surface-variant: '#434655'
  inverse-surface: '#283044'
  inverse-on-surface: '#eef0ff'
  outline: '#737686'
  outline-variant: '#c3c6d7'
  surface-tint: '#0053db'
  primary: '#004ac6'
  on-primary: '#ffffff'
  primary-container: '#2563eb'
  on-primary-container: '#eeefff'
  inverse-primary: '#b4c5ff'
  secondary: '#006c49'
  on-secondary: '#ffffff'
  secondary-container: '#6cf8bb'
  on-secondary-container: '#00714d'
  tertiary: '#784b00'
  on-tertiary: '#ffffff'
  tertiary-container: '#996100'
  on-tertiary-container: '#ffeedd'
  error: '#ba1a1a'
  on-error: '#ffffff'
  error-container: '#ffdad6'
  on-error-container: '#93000a'
  primary-fixed: '#dbe1ff'
  primary-fixed-dim: '#b4c5ff'
  on-primary-fixed: '#00174b'
  on-primary-fixed-variant: '#003ea8'
  secondary-fixed: '#6ffbbe'
  secondary-fixed-dim: '#4edea3'
  on-secondary-fixed: '#002113'
  on-secondary-fixed-variant: '#005236'
  tertiary-fixed: '#ffddb8'
  tertiary-fixed-dim: '#ffb95f'
  on-tertiary-fixed: '#2a1700'
  on-tertiary-fixed-variant: '#653e00'
  background: '#faf8ff'
  on-background: '#131b2e'
  surface-variant: '#dae2fd'
typography:
  display-lg:
    fontFamily: Plus Jakarta Sans
    fontSize: 40px
    fontWeight: '800'
    lineHeight: 48px
    letterSpacing: -0.03em
  headline-xl:
    fontFamily: Plus Jakarta Sans
    fontSize: 32px
    fontWeight: '700'
    lineHeight: 40px
    letterSpacing: -0.025em
  headline-lg:
    fontFamily: Plus Jakarta Sans
    fontSize: 26px
    fontWeight: '700'
    lineHeight: 34px
    letterSpacing: -0.02em
  headline-md:
    fontFamily: Plus Jakarta Sans
    fontSize: 22px
    fontWeight: '600'
    lineHeight: 28px
    letterSpacing: -0.015em
  title-lg:
    fontFamily: Plus Jakarta Sans
    fontSize: 18px
    fontWeight: '600'
    lineHeight: 24px
    letterSpacing: -0.01em
  title-md:
    fontFamily: Plus Jakarta Sans
    fontSize: 16px
    fontWeight: '600'
    lineHeight: 22px
    letterSpacing: -0.005em
  body-lg:
    fontFamily: Plus Jakarta Sans
    fontSize: 16px
    fontWeight: '400'
    lineHeight: 24px
  body-md:
    fontFamily: Plus Jakarta Sans
    fontSize: 14px
    fontWeight: '400'
    lineHeight: 20px
  body-sm:
    fontFamily: Plus Jakarta Sans
    fontSize: 13px
    fontWeight: '400'
    lineHeight: 18px
  label-md:
    fontFamily: Plus Jakarta Sans
    fontSize: 12px
    fontWeight: '600'
    lineHeight: 16px
    letterSpacing: 0.01em
  label-sm:
    fontFamily: Plus Jakarta Sans
    fontSize: 11px
    fontWeight: '700'
    lineHeight: 14px
    letterSpacing: 0.04em
  currency-stat:
    fontFamily: Plus Jakarta Sans
    fontSize: 34px
    fontWeight: '800'
    lineHeight: 42px
    letterSpacing: -0.03em
rounded:
  sm: 0.5rem
  DEFAULT: 1rem
  md: 1.5rem
  lg: 2rem
  xl: 3rem
  full: 9999px
spacing:
  gutter: 1rem
  gutter-sm: 0.75rem
  margin: 1.25rem
  margin-mobile: 1rem
  margin-desktop: 2rem
  space-xs: 0.25rem
  space-sm: 0.5rem
  space-md: 1rem
  space-lg: 1.5rem
  space-xl: 2rem
---

## Brand & Style
The design system embodies a modern, welcoming, and reassuring financial control center optimized for iOS. Subscriptions often introduce ambient anxiety around recurring deductions and hidden fees; this UI directly counters that friction with crisp clarity, fluid tactility, and visual delight.

The style converges **Modern Corporate Clarity** with **Soft Tactile Humanism**:
- **Clarity over Complexity:** Complex billing timelines and cumulative totals are distilled into high-legibility typographic cards and glanceable metrics.
- **Friendly Authority:** Trustworthy financial posture paired with pill-shaped chips, generous touch points, and bright accentuating tones.
- **Native iOS Polish:** Fluid spatial scaling, tactile press states, and ambient light-derived elevation that feels native to modern iOS interfaces.

## Colors
The color architecture establishes an airy, optimistic canvas anchored by an electric royal blue for high-intent primary interactions and financial telemetry.

- **Primary (`#2563EB`):** Electric royal blue driving call-to-actions, active navigation pills, recurring cycle highlights, and interactive toggles.
- **Secondary (`#10B981`):** Vivid emerald green assigned strictly to positive signals—active subscription badges, renewal affirmations, savings notifications, and net balance gains.
- **Tertiary (`#F59E0B`):** Warm amber used selectively for renewal warnings, upcoming cycle alerts (e.g., "Renews in 2 days"), and trial expiration cautions.
- **Background (`#F8FAFC`):** Crisp, ultra-light sky tint surface underpinning views to maintain depth separation against pure-white card modules.
- **Card Surface (`#FFFFFF`):** High-opacity pure white for elevation cards, metric clusters, bottom sheets, and modal sheets.
- **Neutral Core:**
  - Slate 900 (`#0F172A`): Primary headings, monetary values, and core subscription titles.
  - Slate 500 (`#64748B`): Secondary supporting metadata, billing intervals, and muted labels.
  - Slate 300 (`#CBD5E1`): Structural divider rules, inactive track paths, and unselected control rings.
  - Error/Destructive (`#EF4444`): Immediate cancellation prompts, overdue charges, and account removal.

## Typography
Plus Jakarta Sans serves as the single typographic voice across headlines, metrics, and functional UI. Its modern geometric backbone and soft rounded terminals bridge analytical precision with financial friendliness.

- **Monetary Telemetry:** Subscription values and monthly burn totals utilize `currency-stat` and `display-lg` with tight negative tracking (`-0.03em`) to keep numeric combinations compact and prominent.
- **Hierarchy Enforcement:** Titles must use Slate 900 (`#0F172A`) for immediate scan-readiness, while supporting descriptors transition directly into Slate 500 (`#64748B`) using `body-md` or `body-sm`.
- **Badges and Status Tags:** Status tokens, cadence chips (e.g., "MONTHLY", "ANNUAL"), and micro indicators strictly employ `label-sm` with slight uppercase tracking (`0.04em`) to maintain legibility at compact dimensions.

## Layout & Spacing
The layout model follows a responsive, mobile-first iOS fluid grid that preserves strict vertical rhythm and standard touch-target accessibility.

- **Mobile Canvas Rules:** Standard mobile views leverage a `1rem` (16px) margin on narrow devices (iPhone SE/mini) scaling to `1.25rem` (20px) on standard viewports (iPhone Pro/Max).
- **Component Gap Rhythm:**
  - `space-xs` (4px): Inset spacing between status dots and associated text labels.
  - `space-sm` (8px): Icon-to-copy proximity inside actionable items and chip padding.
  - `space-md` (16px): Standard inner padding for cards, subscription row list items, and form groups.
  - `space-lg` (24px): Structural clearance between dashboard card blocks and section breaks.
  - `space-xl` (32px): Primary section stack spacing and floating navigation clearances.
- **Safe Area Insets:** Fixed bottom CTA sheets and navigation bars respect iOS bottom safe areas (`env(safe-area-inset-bottom)`) with an additional `1rem` clearance to eliminate gesture interference.

## Elevation & Depth
Depth is produced through ambient, color-tinted drop shadows paired with pure white surfaces set against the ultra-light sky canvas (`#F8FAFC`). Pure black or stark grey drop shadows are strictly prohibited.

- **Surface Rest:** Ambient soft card shadow:
  - `box-shadow: 0 10px 25px -5px rgba(37, 99, 235, 0.08), 0 8px 10px -6px rgba(37, 99, 235, 0.04);`
  - This blue-tinted dispersion maintains a luminous, clean look without visual muddiness.
- **Active / Pressed Elevation:** Cards and action components sink on press:
  - `transform: scale(0.985);`
  - `box-shadow: 0 4px 12px -2px rgba(37, 99, 235, 0.06);`
- **Floating Modals & Action Sheets:** Top-level sheets, action sheets, and detached floating navigation pills utilize increased displacement:
  - `box-shadow: 0 20px 35px -8px rgba(15, 23, 42, 0.08), 0 1px 3px 0 rgba(37, 99, 235, 0.04);`
- **Subtle Boundaries:** Surface boundaries are defined by shadow displacement; thin border strokes (`1px solid rgba(226, 232, 240, 0.8)`) are reserved exclusively for inner card dividers, nested data lists, or input perimeter wells.

## Shapes
The design system adopts a hyper-rounded, organic geometry (Level 3 / Pill-shaped scale) optimized for capacitive iOS interactions and friendly aesthetics.

- **Primary Cards & Containers:** Structured with `rounded-2xl` (1.25rem / 20px) to `rounded-3xl` (1.5rem / 24px) continuous squircle corners.
- **Interactive Buttons & Chips:** Standard primary buttons, category filters, and state tags utilize full pill radiuses (`rounded-full` / 9999px).
- **Service & Vendor Avatars:** Service brand logos (e.g., Netflix, Spotify, iCloud) sit inside rounded squares with an explicit `rounded-2xl` corner radius, creating visual consistency across varied external brand iconography.

## Components

### Buttons
- **Primary Action Button:** Full-width or inline pill (`rounded-full`), height `52px`, background Electric Royal Blue (`#2563EB`), text pure white with `title-md`. Dynamic press state compresses slightly (`scale(0.98)`).
- **Secondary / Ghost Button:** Light sky tint fill (`#EFF6FF`), text Electric Royal Blue (`#2563EB`), zero border, matching pill geometry.
- **Destructive Button:** High-translucency red tint (`#FEF2F2`), text `#EF4444`, `title-md`.

### Status Tags & Chips
- **Active Subscription Badge:** Emerald Green base tint (`#ECFDF5`), borderless, text `#047857` or `#10B981` in `label-sm`. Houses a 6px circular `#10B981` status dot with a 2px offset.
- **Expiring / Trial Badge:** Amber tint (`#FFFBEB`), text `#B45309`, `label-sm`.
- **Category Filter Chips:** Floating horizontal pills with `space-sm` vertical and `space-md` horizontal padding. Selected: `#0F172A` text on pure white with ambient shadow; unselected: `#64748B` on transparent background.

### Cards & Subscription Row Items
- **Summary Metrics Card:** Pure white background, `rounded-3xl`, padded with `space-lg`, ambient blue shadow (`0 10px 25px -5px rgba(37,99,235,0.08)`). Top displays time cycle ("Total Monthly Spend"), middle displays `currency-stat` figure, footer shows month-over-month trend.
- **Subscription List Row:** Nested within white grouping containers or presented as standalone modular cards. Left contains `44px` squircle service icon, center houses service name (`title-md`) and next billing date (`body-sm`, Slate 500), right contains recurring price (`title-md`) paired with interval cadence.

### Inputs & Search Bars
- **Search & Text Fields:** Height `48px`, background `#F1F5F9`, border `1px solid transparent`, radius `rounded-2xl` (16px). Focus shifts background to `#FFFFFF`, border to `#2563EB`, and introduces an ambient focus glow (`0 0 0 3px rgba(37,99,235,0.15)`).

### Toggles & Switches
- **iOS-style Toggle Switch:** Track dimensions `50px × 30px`, radius `rounded-full`. Inactive track `#E2E8F0`; active track `#2563EB`. Thumb is pure white `#FFFFFF` with slight elevation shadow.

### Circular Spending Progress Rings
- **Subscription Analytics Ring:** SVG-based circular progress gauge. Background track Slate 200 (`#E2E8F0`), active indicator filled with Electric Royal Blue (`#2563EB`) with a rounded stroke cap. Center label indicates percentage of budget allocated.