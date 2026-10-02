# Suggested Plugin Hub PR description

Wacky Sire displays colorful, independently flailing inflatable tube men in
place of Abyssal Sire's tentacle visuals. It targets only NPC IDs 5909–5913 and
uses original procedural geometry built from cloned cache-triangle primitives
through the public ModelData API. It renders cosmetic RuneLiteObjects and
suppresses the matched originals through RenderCallbackManager.

There is no combat automation, new combat-state overlay, external asset
download, reflection or new runtime dependency. Animation phases and colors
are based only on tentacle location, not attack or stun state.

Rendering disclosure: suppressing the original entity removes its native
client clickbox, including hover/examine targeting, and may allow click-through.
The native tentacle animation cues are replaced with a continuous cosmetic
flail. The original UI rendering pass is allowed. A configuration option retains
the native geometry/clickboxes with the tube men alongside. I would appreciate
review of this behavior as part of the cosmetic plugin's approval.

Validation: add actual RuneLite version, renderer, OS and completed in-game
results here before opening the PR. The provided automated tests cover geometry,
NPC scope, cache isolation and lifecycle; they do not verify live rendering.
