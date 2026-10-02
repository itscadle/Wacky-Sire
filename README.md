# Wacky Sire

A RuneLite Plugin Hub **submission candidate** that replaces Abyssal Sire's
tentacle visuals with colorful wacky waving inflatable flailing arm tube men.

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

This behavior must be disclosed to RuneLite reviewers. Approval is not assured.
The project has not yet been visually tested in the live game. Do not describe
it as approved or claim GPU/117 HD compatibility before completing the checks below.

## Run locally

1. Extract this project. Install [IntelliJ IDEA](https://www.jetbrains.com/idea/)
   and an [Eclipse Temurin Java 11 JDK](https://adoptium.net/temurin/releases/?version=11).
2. In IntelliJ choose **Open**, select this folder and import the Gradle project.
   Set the project and Gradle JVM to Java 11.
3. In the Gradle panel run **Tasks → verification → test**, then the **run** task.
   In a terminal, use `./gradlew test` followed by `./gradlew run`.
   Windows PowerShell: `./gradlew.bat test` then `./gradlew.bat run`.
4. In the development client, enable **Wacky Sire** and visit a Sire chamber.
   The plugin also handles being enabled while the chamber is already loaded.

Jagex account users should follow RuneLite's official
[development-client login guide](https://github.com/runelite/runelite/wiki/Using-Jagex-Accounts)
if the development client cannot log in normally.

The project defaults to `latest.release`, as recommended by the Plugin Hub.
For the version initially compiled against, use
`./gradlew test -PruneLiteVersion=1.13.1`.

## Before submission

Complete and record [docs/IN_GAME_CHECKS.md](docs/IN_GAME_CHECKS.md). In particular,
check triangle model 823 in the real cache, the actual tentacle placement,
occlusion, click-through behavior and renderer compatibility.

Source compilation and automated tests cover Java/API compatibility, mesh
topology, cache array isolation, NPC scope and lifecycle. These do not validate
native rendering, cache model shape, menus or reviewer acceptance.
Recorded build/test results are in [docs/VALIDATION.md](docs/VALIDATION.md).

If a model fails, inspect the development client's log for **Wacky Sire model
creation failed**. The plugin restores original visuals and stops trying until
it is re-enabled. A pending cache download is retried automatically.

## Submit to the Plugin Hub

1. Create a **public** GitHub repository, e.g. `wacky-sire`, and upload this
   project's files with `build.gradle` at the repository root. Exclude generated
   `build/` and `.gradle/` directories. The root BSD 2-Clause license and
   `runelite-plugin.properties` are included. Edit `author` if desired.
2. Push a commit after in-game checks pass. Copy its full 40-character SHA.
3. Fork [runelite/plugin-hub](https://github.com/runelite/plugin-hub), create a
   branch and add one plain file named `plugins/wacky-sire`:

   ```properties
   repository=https://github.com/YOUR_USERNAME/wacky-sire.git
   commit=YOUR_FULL_40_CHARACTER_COMMIT_SHA
   ```

   Replace both values with the actual repository and commit. Do not submit
   placeholders. Plugin Hub builds from that exact public Git commit.
4. Open a pull request describing the cosmetic behavior and explicitly
   disclosing the removed client clickboxes and native animation replacement.
   A suggested description is in [docs/SUBMISSION.md](docs/SUBMISSION.md).
5. Check the Hub CI and respond to review. Every code change needs a new
   `commit=` SHA in the manifest.

The project uses `build=standard` and only RuneLite's existing runtime
dependencies. The Hub replaces local Gradle build files for this build type.
The Gradle wrapper is from RuneLite's official example plugin.

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
