# Automated validation

Validated on October 2, 2026 against the released RuneLite **1.13.1** artifacts.

- Gradle 8.10: `test jar` — **BUILD SUCCESSFUL**.
- JDK 17 compiler targeting Java 11 (`--release 11`).
- 13 JUnit 4 tests — **13 passed, 0 failed**.
- Original procedural animation exported from the Java implementation and
  rendered for the offline preview.

The tests exercise finite nondegenerate geometry over many poses, fixed
topology, reverse windings, a seamless loop, blower anchoring, exact NPC scope,
mid-fight enablement, cleanup, NPC transformations, missing-asset retries,
unexpected-cache fallback and isolation between cache/model arrays.

The development-client log exposed a `NullPointerException` in
`cloneTextures()` when the cache model has no texture array. Model creation
now clones textures only when that array exists. The API fake now reproduces
the native null-array failure, and a dedicated untextured-model regression
test passes. The old implementation fails this check; the fixed implementation
passes all 13 tests. The build used `-PruneLiteVersion=1.13.1`.

The rendering integration tests use API fakes. The actual game cache, native
rendering, hitboxes, menus, occlusion and renderer coexistence have not been
verified. [IN_GAME_CHECKS.md](IN_GAME_CHECKS.md) remains pending. The Plugin Hub
has not reviewed or approved this project.
