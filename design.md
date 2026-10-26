# Paros — Design System

Extracted from https://paros-os.vercel.app/ (copy/structure from page fetch; colors sampled from screenshots, so hex values are approximate. Fonts are visual identification, not read from CSS).

## Style Summary

**Neo-brutalist, playful, "indie cafe" look.** Warm off-white canvas, thick near-black outlines, hard offset shadows (no blur), pastel fills (yellow, mint, peach), and a single burnt-orange brand accent. Slightly rotated pills and a rotated headline block add a sticker/zine feel. Uppercase labels and monospace data give it a "POS terminal" character.

## Color

| Token | Hex (approx) | Use |
|---|---|---|
| `--bg` | `#E8E5E3` | Page background (with faint dot grid + soft pink radial glow) |
| `--ink` | `#1A1616` | Text, borders, hard shadows, dark buttons, terminal chrome |
| `--brand` | `#CD5535` | Primary CTA, logo tile, hero block, slider, prices, active tabs |
| `--yellow` | `#EAD97F` | Ticker bar, pills, ADD chips, floating "Role Switcher", stat card |
| `--mint` | `#9CDCBE` | "INDIE OS" badge, stat card, positive pills |
| `--peach` | `#E9A86C` | Stat card, "Unbilled" / secondary pills |
| `--green` | `#11A677` | Check icons, WhatsApp bill button, "synced" badge |
| `--danger` | `#D52C36` | "Extortionist way" label, legacy cost chips |
| `--danger-bg` | `#ECB6B8` / `#E6DCDE` | Legacy POS card tint and chip fill |
| `--section-mint` | `#CEE1D6` | Simulator and "Paros" comparison section bg |
| `--section-cream` | `#E7E0C4` | Pricing section bg |
| `--order-panel` | `#E6E0BD` | KOT / order ticket panel |
| `--surface` | `#E8E6E8` | Cards, inputs |
| `--muted` | `#5A5555` | Body/subtext (grey-brown) |

Pattern: each section gets its own tinted background (cream → mint → cream), separated by 2px ink rules.

## Typography

- **Display / headings:** Space Grotesk–style geometric grotesque (distinctive "y" and "g"). Bold/700, tight leading, very large hero (~72–80px desktop), section titles ~48px.
- **Body:** Same grotesque family, regular weight, ~18–20px for hero subtext, ~16px elsewhere, muted color.
- **Labels / nav / buttons / pills:** Uppercase, semi-bold, letter-spaced (~0.02–0.05em), 12–14px.
- **Data / terminal:** Monospace (Space Mono / JetBrains Mono style) for KOT numbers, subtotals, slider scale, terminal title bar.
- **Icons:** Material Symbols (outlined, brand-orange in cards) plus emoji in ticker and pills.

## Borders, Shadows, Radius

- **Border:** 2px solid `--ink` on nearly every card, button, pill, input.
- **Shadow:** Hard offset, no blur: `4px 4px 0 var(--ink)` (cards, buttons); larger `6px 6px 0` for big panels (terminal, comparison cards).
- **Radius:** Pills `999px`; buttons/chips `10–14px`; cards `16–20px`; terminal window `~28px`.
- **Dividers:** 2px solid ink between sections; dashed 1–2px lines inside cards and receipts.

## Layout

- Max content width ~1200px, centered; hero text centered, ~900px wide.
- Sticky top nav (cream, bottom 2px ink border) under a scrolling yellow ticker.
- Grids: 4-up stat cards, 2-up comparison, 2-up pricing, 3-up feature/testimonial.
- Generous vertical section padding (~96–120px).
- Floating "Role Switcher" pill fixed bottom-right (yellow, ink border, hard shadow, green status dot).

## Components

**Ticker bar** — Full-width yellow strip, uppercase mono-ish text, emoji + dot separators, infinite marquee.

**Nav** — Logo tile (orange rounded square, white "P", ink border + shadow) + "PAROS." wordmark with orange dot + mint "INDIE OS" pill with green dot. Links uppercase, no underline. Right: secondary button (surface, ink border) and primary button (orange).

**Buttons**
- Primary: `--brand` fill, white uppercase text, ink border, hard shadow, arrow `→` or bolt icon.
- Secondary: `--surface` fill, ink text/border, same shadow.
- Dark: `--ink` fill, white text (Silver plan CTA).
- Success: `--green` fill, white text with send icon (WhatsApp bill).
- Hover: translate toward shadow (shadow shrinks) — typical neo-brutalist press effect.

**Pills / badges** — Rotated ±1–2°, filled yellow / mint / peach, ink border + small shadow, uppercase 12px bold, leading emoji.

**Hero highlight block** — Second headline line sits on a solid orange rectangle, rotated ~-1°, white text, thick ink border and hard shadow.

**Stat cards** — Four equal cards, each a different fill (surface, mint, yellow, peach). Big orange numeral (₹0, <1s, 0%, 100%), small uppercase label below.

**Trust chips** — Surface pill with orange icon, venue name, and a small colored location tag (yellow / mint / peach).

**Simulator terminal** — Dark `--ink` title bar with red/yellow/green window dots, yellow mono title, green "CLOUD SYNCED" badge. Left: menu grid; right: cream order ticket.
- Tabs: segmented control in a bordered container; active tab orange with shadow.
- Table selector: small pill buttons (T1–T4), active in orange.
- Category filters: pills, active = ink fill / white text.
- Menu item card: orange icon top-left, yellow "+ ADD" chip top-right, name (bold), description (muted), price (bold).
- Order line: bordered mini-card with qty × name, modifier in muted text, price right-aligned.
- Totals: mono rows, dashed divider, "Grand Total" with orange amount.

**Comparison cards** — Left "Extortionist way": pink-tinted, red label, red cost chips, circular red decoration. Right "Revolutionary way": mint, green label, green outlined chips. Rows separated by dashed lines; "VS" circle badge between; rotated yellow banner on the top edge.

**Slider** — Orange fill + thumb, grey track, mono scale labels, value shown in yellow pill.

**Pricing cards** — Surface cards with ink border + shadow. Featured (Gold) has an orange top bar and orange "★ Barista Favorite" badge. Price large bold (Gold price in orange) with small "/ year" suffix. Feature list with green circle-check icons; "Everything in Silver" line in orange.

**Footer trust row** — Green check icons + uppercase labels.

## Voice & Content

- Hinglish-flavored, confident, a bit cheeky ("Table se Kitchen tak. Bas Paros.", "Petpooja Killer").
- Rupee-first numbers, concrete savings math, short punchy uppercase microcopy.
- Heavy use of emoji and checkmarks for scannability.

## Motion

- Marquee ticker; tab/section sync in the simulator; slider-driven live recalculation; button press (shadow collapse) on hover/active.

## Implementation Notes (Tailwind-ish)

```css
:root{
  --bg:#E8E5E3; --ink:#1A1616; --brand:#CD5535;
  --yellow:#EAD97F; --mint:#9CDCBE; --peach:#E9A86C;
  --green:#11A677; --danger:#D52C36;
  --shadow:4px 4px 0 var(--ink);
}
.card{background:#E8E6E8;border:2px solid var(--ink);border-radius:18px;box-shadow:var(--shadow)}
.btn{border:2px solid var(--ink);border-radius:12px;box-shadow:var(--shadow);
     text-transform:uppercase;font-weight:700;letter-spacing:.03em}
.btn:active{transform:translate(4px,4px);box-shadow:none}
.pill{border:2px solid var(--ink);border-radius:999px;box-shadow:2px 2px 0 var(--ink);
      transform:rotate(-1.5deg);text-transform:uppercase;font-weight:700;font-size:12px}
```
