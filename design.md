# Design — SamsungControll

Locked design system ("Graphite & Signal") for the Android app. Screens read these
tokens from `ui/theme/RemoteTokens.kt`; do not add raw colour literals in UI code.
To change the system, amend this file and the tokens together.

## Genre
Modern-minimal, utilitarian. The screen is the remote: no device-shaped frame,
no gradients or glows, nothing decorative.

## Theme (OKLCH → sRGB in `RemoteTokens`)
- Background       oklch(0.17 0.01 250)
- Surface 1/2/3    oklch(0.21 / 0.26 / 0.32, 0.01, 250): panels, idle keys, raised
- Text primary     oklch(0.96 0.005 250); secondary oklch(0.74 0.012 250)
- Accent (signal)  oklch(0.86 0.18 125), only for state: connected, pressed key/wedge,
  selected tab, pinned app. Keep under ~5% of any screen. Ink on accent: oklch(0.20 0.05 125).
- Power / danger   oklch(0.68 0.20 25), only the power key and destructive actions.
- Warn             amber, only "waiting for permission".

## Typography
- Titles / body: sans (`DisplayFont`), 12 / 14 / 16 / 18 sp.
- Key legends and digits: monospace (`LegendFont`), like the print on a physical remote.
- Both are single constants in `ui/theme/Type.kt`; swap in bundled Space Grotesk and
  JetBrains Mono there (not bundled yet, so the app ships no font files).
- Minimum text size 12 sp.

## Shape and spacing
- Two radii: 12 dp keys, 20 dp surfaces. Circles only for the D-Pad and power key.
- 4 dp spacing scale (`Space1…Space8`); minimum touch target 48 dp.

## Structure
- One-line header: status dot + TV name (opens the TV menu: switch, nickname, details,
  reconnect, settings), search, power.
- Body: four pages (Control, Touch, Numbers, Apps) with a tab rail at the bottom, inside thumb reach.
- Control: four-wedge D-Pad, BACK/HOME/EXIT, VOL / MUTE / CH rockers.
- Touch: gesture pad (swipe = arrows, tap = OK, double-tap = Home, long-press = Back).
- Numbers: keypad, inputs, advanced keys behind "More keys", "type on TV".
- Apps: grid (installed apps from the TV, with featured fallback), long-press menu (pin,
  wake-and-open scene, home-screen shortcut), sticky transport bar.

## Motion and interaction
- Press feedback: 90 ms scale to 0.96 plus the accent fill. No spring, no bounce.
- Direction, volume, channel and rewind/forward keys repeat while held.
- Every control has a content description; the D-Pad exposes its directions as
  TalkBack custom actions.

## Do not
- Re-draw device chrome (frames, fake cameras) around the remote.
- Add gradients, glows or a second accent colour.
- Hard-code colours, sizes below 12 sp or targets below 48 dp.
