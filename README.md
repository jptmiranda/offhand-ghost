# Offhand Ghost

A [RuneLite](https://runelite.net) plugin. Wield a two-handed weapon and the shield slot it blocks
shows a faded copy of that weapon instead of an empty square. It works the other way round too, for
items that sit in the shield slot and block the weapon slot.

The ghost is drawn on both the worn equipment tab and the Equipment Stats interface. The empty-slot
glyph behind it is hidden, the same as when a real item is equipped.

## Config

| Option | Default | Description |
| --- | --- | --- |
| Opacity | 50% | How strongly the ghost is drawn |
| Drop shadow | off | Draw a shadow behind the ghost so it separates from the slot |
| Shadow opacity | 25% | How dark that shadow is |

## Building

```
./gradlew run          # launch RuneLite with the plugin loaded
./gradlew build        # compile
```

Requires JDK 11+.
