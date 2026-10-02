# Wacky Sire

A RuneLite plugin that replaces Abyssal Sire’s tentacle visuals with colorful wacky waving inflatable flailing arm and tube men.

![Offline preview of the procedural mesh, not an in-game screenshot](docs/preview.gif)

## Features

- Original low-poly 3D mesh: hollow tube, two flailing arms, face, fabric streamers, blower base.
- Six colors selected deterministically by the tentacle's location.
- Independent looping animation, unrelated to attacks or the tentacle's state.
- Exactly five Sire tentacle variants (5909–5913); respiratory systems, Sire,
  spawns and other bosses are excluded.
- Optional original tentacle visuals alongside the tube men.
- Cleanup on disable, despawn, loading, logout and hopping.
- Original visuals remain if assets are unavailable or model construction fails.
- No bundled custom cache, network requests, runtime reflection or extra runtime dependencies.

## What this changes

This draws separate client-side `RuneLiteObject`s and uses the public
`RenderCallbackManager` to suppress the matched NPCs' scene geometry.
It does not write NPC definitions, animations, combat state, collision, tiles,
menus or server data. The original NPC UI draw pass is allowed.

**Suppressing an entity also removes its original client clickbox.** Replacement
mode consequently changes local hover/examine targeting and may allow clicking
through to an entity behind it. Tube men are decorative scene objects with no
NPC menus. This is not a transparent swap that preserves original hitboxes.
The original tentacle attack/sleep/stun animations are also replaced with a
constant flail. Enabling **Keep original tentacles visible** preserves the
original geometry and client clickboxes, with tube men drawn alongside.

The project has been visually tested in the live game. It is working on Vanilla, 117 HD, and GPU plugin. Plugin Hub approval is pending.


## Code map

| File | Role |
| --- | --- |
| `WackySirePlugin.java` | Tracks NPCs, registers cosmetic objects, cleans up and filters rendering |
| `TentacleIds.java` | Restricts the effect to five named Sire tentacle IDs |
| `TubeMesh.java` | Generates original tube-man geometry and looping poses |
| `TubeModelFactory.java` | Builds native models without changing cache arrays |
| `WackySireConfig.java` | Optional original-visuals setting |
| `WackySireLauncher.java` | Launches a RuneLite development client |
| `MeshPreview.java` | Exports the real mesh for an offline artwork preview |

The procedural mesh uses cache model 823 only as a single-triangle allocation
primitive, then merges isolated clones and overwrites their vertex coordinates
and face colors. Runtime checks reject an unexpected primitive or merged shape.
Each tentacle owns its native model arrays. The 64 immutable geometry poses are
shared to avoid per-tick mesh allocation. Initial lighting is retained during
the deformation; moving highlights are not relit every frame.

## References

- [Official Plugin Hub submission guide](https://github.com/runelite/plugin-hub)
- [RuneLiteObject API](https://static.runelite.net/runelite-api/apidocs/net/runelite/api/RuneLiteObject.html)
- [RenderCallback API and clickbox behavior](https://static.runelite.net/runelite-client/apidocs/net/runelite/client/callback/RenderCallback.html)
- [RuneLite example plugin](https://github.com/runelite/example-plugin)

The mesh and animation are original procedural artwork. OSRS cache primitives
remain supplied by the game, not redistributed in this repository.
