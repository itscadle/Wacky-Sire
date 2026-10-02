# Live-client checks — pending

Automated tests do not replace these checks. Run in the development client
before submitting. Record your RuneLite version, OS, renderer, results and a
short screenshot/video clip. The offline preview is not evidence of in-game rendering.

- [ ] Enable while a Sire chamber is already loaded: all five tentacle variants
      create tube men, with no original geometry left in replacement mode.
- [ ] Verify cache model 823 still has 3 vertices / 1 face. No model warning in logs.
- [ ] Check actual NPC origins, orientation, scale and floor height in all chambers;
      adjust geometry if any tube floats, clips through the arena or hides a lung.
- [ ] Compare rendering under software, built-in GPU and 117 HD. Record any
      unsupported renderer in the README instead of claiming universal compatibility.
- [ ] Turn camera through a full rotation: correct triangle winding, no missing
      faces, no stuck GPU geometry or incorrect drawing over walls/actors.
- [ ] Observe sleep, awake, attacks and stun: constant cosmetic flail, no
      predictive combat signal. Native attack animations are intentionally not reproduced.
- [ ] Verify all four respiratory systems remain visible and can be targeted.
- [ ] Compare hover/examine and entities behind a tentacle in both setting modes.
      Replacement mode removes the native clickbox. Record practical effects.
- [ ] Verify hitsplats/health bars still draw when relevant.
- [ ] Toggle Keep original tentacles visible: original geometry returns immediately.
- [ ] Disable and re-enable: no ghost objects, duplicate models or hidden NPCs.
- [ ] Exit, hop worlds, log out and back in: no scene leaks or hidden originals.
- [ ] Confirm Kraken, Whisperer, Sire, lungs, spawn and scion models are untouched.
- [ ] Test alongside Entity Hider and NPC model/animation plugins; disclose conflicts.
- [ ] Check CPU/FPS with all nearby tentacles loaded and with the plugin disabled.
- [ ] Review clickbox/cue changes with RuneLite maintainers; adjust approach if requested.

## Results

Not yet run in a live game session.
