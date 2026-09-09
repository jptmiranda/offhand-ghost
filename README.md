# Offhand Ghost

A [RuneLite](https://runelite.net) plugin that fills the empty equipment slot a two-handed item
blocks with a faded copy of that item's sprite — the way many RPGs indicate "this slot is taken by
what you're already holding" rather than leaving a bare, ambiguous square.

Wield a godsword and the shield slot shows a ghosted godsword; the same works in reverse for an
item that sits in the shield slot and blocks the weapon slot.

It draws on the worn-equipment tab and, optionally, the Equipment Stats interface.

## Config

| Option | Default | Description |
| --- | --- | --- |
| Opacity | 50% | How strongly the ghosted item is drawn |
| Drop shadow | off | Draw a shadow behind the ghost so it separates from the slot |
| Shadow opacity | 25% | How dark that shadow is |

The empty-slot glyph behind the ghost is always hidden, matching what the game does when a real
item occupies the slot, and the ghost is always drawn on both the worn-equipment tab and the
Equipment Stats interface.

## Building

```
./gradlew run          # launch RuneLite with the plugin loaded
./gradlew build        # compile
```

Requires JDK 11+.
