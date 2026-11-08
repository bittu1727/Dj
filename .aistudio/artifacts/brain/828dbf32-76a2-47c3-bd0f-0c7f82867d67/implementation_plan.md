# AI DJ Mixer - Android Native Edition

A pro-grade mobile DJ performance console and AI-assisted track studio built natively for Android in Kotlin and Jetpack Compose. Users can load tracks on dual independent decks, preview interactive waveforms, cut and loop cue segments, balance crossfader and 3-band EQ, generate AI music tracks with Gemini, sync BPM automatically, and record live mix sessions.

### User Review & Critical Decisions

> [!IMPORTANT]
> The following architectural and design choices were confirmed during Phase 1 clarification:

- **Target Architecture & Framework**: Native Android with Kotlin and Jetpack Compose, targeting high performance, low audio latency, and seamless integration with Android's audio framework.
- **Visual Design Identity**: Pro DJ dark hardware console aesthetic with high-contrast neon accents—Deck A in electric neon cyan (`#00F0FF`) and Deck B in warm energetic amber/orange (`#FF9500`), tactile rotary EQ knobs, glowing VU peak meters, vinyl platter animations, and responsive sliding faders.
- **AI Music Generation**: In-app Gemini AI music composition engine combined with a deterministic multi-track PCM audio synthesizer. Users enter a genre, mood, tempo, and prompt; Gemini creates musical arrangements, chord progressions, and basslines that are rendered directly into rich playable audio clips loaded directly onto Deck A or Deck B.
- **Local Audio Library**: Access to device audio files via MediaStore / Storage Access Framework with zero broad storage permission overhead, alongside bundled pre-loaded royalty-free demo DJ tracks and loops for immediate out-of-the-box mixing.

---

### 1. Overview & Core Concept

- **What It Does**: AI DJ Mixer turns an Android device into a dual-deck DJ controller. It provides two independent audio playback engines with pitch/tempo control, 3-band frequency equalization (Bass, Mid, Treble), hot cues, looping, crossfader blending, real-time waveform navigation, BPM tap and auto-sync, song trim/cutter studio, Gemini-powered prompt-to-audio track generation, and master output WAV recording.
- **Target Audience / Persona**: Mobile DJs, music producers, party hosts, and audio enthusiasts looking for a tactile, low-latency mixing experience combined with generative AI track creation.
- **Key Value**: Immediate, zero-friction mixing without needing external hardware, featuring smart AI track generation on the fly to fill any set with custom beats and transitions.

---

### 2. User Experience & Visual Design

#### Key User Flows
1. **Launch & Quick Mix**: App opens to the dual-deck console. Deck A and Deck B are pre-primed with energetic demo tracks. Users can tap Play on Deck A, adjust the Crossfader toward Deck B, start Deck B, and hit **Sync** to lock tempos.
2. **Track Loading & Library**: Tapping "Load Track" on either deck opens the Library screen. Users can choose from bundled DJ loops, import device MP3/WAV/AAC files via the system picker, or tap "AI Generator".
3. **AI Song Generation**: In the AI Studio tab, users choose genre (House, Techno, Hip-Hop, Synthwave, Drum & Bass), mood (Energetic, Dark, Chill, Uplifting), and tempo (BPM slider). Gemini crafts the track structure and sound synthesis parameters, which are rendered to high-quality audio and immediately assigned to Deck A or Deck B with calculated BPM and cue points.
4. **Waveform Cue & Song Trimming**: Users can tap "Trim / Editor" to zoom into the waveform, set Loop In / Loop Out points, adjust start and end crop boundaries, test-play the isolated loop, and commit it back to the deck.
5. **Live Performance & Recording**: Tapping "REC" starts streaming the master mix buffer into an audio file. The timer pulses red; tapping stop prompts to name, save, or share the mix.

#### Visual Identity & Theme
- **Color Palette**:
  - Console Chassis: Deep obsidian `#0D0E15` with elevated carbon panels `#161922` and border strokes `#252A38`
  - Deck A Accent: Electric Neon Cyan `#00F0FF` with glowing shadow `#00F0FF33`
  - Deck B Accent: Warm Club Amber `#FF9500` with glowing shadow `#FF950033`
  - Master Accents: Acid Green `#00E676` for active play/record states, Crimson `#FF3B30` for cue/rec badges
  - Meters & Sliders: Dynamic gradient LED bars (`#00E676` -> `#FFEA00` -> `#FF3B30`)
- **Typography & Metering**:
  - Monospace digital display font styling for BPM readouts, pitch percentages, and elapsed time (`03:42.12`).
  - Bold sans-serif labels for controls (`CUE`, `SYNC`, `LOOP 4`, `LOW`, `MID`, `HIGH`).
- **Interactive Controls & Motion**:
  - Spinning vinyl platter / jog wheel with touch-drag pitch bend and scratch response.
  - Smooth 60fps scrolling multi-band waveforms with playhead markers.
  - Interactive rotary EQ dials (drag vertically or rotate) and horizontal crossfader with curve options.
  - Minimum 48dp touch targets on all pads, cue triggers, and transport buttons.

---

### 3. Key Product Decisions & Trade-Offs

- **Dual-Engine Audio Pipeline**:
  - *Chosen Approach*: Multi-instance `MediaPlayer` / `AudioTrack` playback layer coupled with an Android `Equalizer` audio effect session per deck, plus a software mixer recorder.
  - *Why*: Delivers hardware-accelerated, glitch-free audio playback across diverse Android devices while allowing dynamic volume scaling, crossfading, pitch adjustment, and 3-band filtering.
- **In-App AI Music Generation**:
  - *Chosen Approach*: Gemini model generates structured musical compositions (BPM, key, scale, drum patterns, synthesizer note sequences, synth patch parameters), which are rendered into high-fidelity PCM WAV audio via a built-in algorithmic synthesizer.
  - *Why*: Provides instant, completely self-contained AI music generation without relying on external third-party paid song APIs or slow remote uploads, ensuring 100% offline reliability with online AI expansion.
- **Waveform Analysis & BPM Detection**:
  - *Chosen Approach*: Fast Fourier energy peak beat-detection algorithm operating on decoded audio buffers, combined with manual Tap-Tempo fallback.
  - *Why*: Instant BPM calculation and waveform thumbnail generation without freezing the UI thread.
- **Zero-Permission Media Access**:
  - *Chosen Approach*: Jetpack ActivityResult `OpenDocument` / `GetContent` picker for local device music, bundled royalty-free DJ stems in assets, and AI-generated tracks.
  - *Why*: Eliminates dangerous storage permission requests, fully complying with modern Android scoped storage and Play policies.

---

### 4. Technical Architecture & Data Strategy

```
┌────────────────────────────────────────────────────────────────────────┐
│                        Jetpack Compose UI Layer                        │
│                                                                        │
│  ┌───────────────────────┐ ┌──────────────────────┐ ┌───────────────┐  │
│  │     Deck A (Cyan)     │ │  Crossfader & Master │ │ Deck B (Amber)│  │
│  │ Jog Wheel / Waveform  │ │    Volume / Curve    │ │ Jog Wheel / WF│  │
│  │ 3-Band EQ / CUE / Play│ │      VU Meters       │ │ 3-Band EQ /   │  │
│  └───────────┬───────────┘ └──────────┬───────────┘ └───────┬───────┘  │
│              │                        │                     │          │
│  ┌───────────▼────────────────────────▼─────────────────────▼───────┐  │
│  │               Navigation (Mixer / Trim / AI / Library)            │  │
│  └────────────────────────────────────┬─────────────────────────────┘  │
└───────────────────────────────────────┼────────────────────────────────┘
                                        │
┌───────────────────────────────────────▼────────────────────────────────┐
│                       ViewModel & State Store                          │
│  - DeckState (track, isPlaying, positionMs, loop, bpm, pitch, eq)      │
│  - MixerState (crossfader, masterVolume, isRecording, recordedMs)      │
│  - LibraryState (user tracks, AI tracks, bundled tracks)               │
└───────────────────────────────────────┬────────────────────────────────┘
                                        │
        ┌───────────────────────────────┴──────────────────────────────┐
        │                                                              │
┌───────▼──────────────────────────────┐      ┌────────────────────────▼─────────────┐
│       Native DJ Audio Engine         │      │      Gemini AI Music Engine          │
│  - Deck A Audio Player & Equalizer   │      │  - Generative prompt structuring     │
│  - Deck B Audio Player & Equalizer   │      │  - Synth voice & beat generator      │
│  - Software Mixer Buffer & Recorder  │      │  - WAV file export to Deck Library   │
│  - BPM & Beat Grid Analyzer          │      │  - Pre-cached genre loop library     │
└──────────────────────────────────────┘      └──────────────────────────────────────┘
```

#### Core Data Models & State:
- `Track`: `id`, `title`, `artist`, `durationMs`, `bpm`, `key`, `audioUri`, `waveformData`, `genre`, `isAiGenerated`
- `DeckState`: `track`, `isPlaying`, `playbackPositionMs`, `pitchBend`, `volume`, `lowGain`, `midGain`, `highGain`, `cuePositionMs`, `loopStartMs`, `loopEndMs`, `isLoopActive`, `isSynced`
- `MixerState`: `crossfader` (-1.0 to +1.0), `masterVolume`, `isRecording`, `recordingDurationMs`, `curveMode`
- `AiGenerationRequest`: `prompt`, `genre`, `mood`, `bpm`, `durationSeconds`

#### Navigation & Screen Hierarchy:
1. **MixerScreen**: Dual-deck performance console with interactive vinyl platters, responsive vertical EQ knobs, pitch sliders, waveform scrubber, crossfader, and master VU meters.
2. **LibraryScreen**: Tabbed browse view (Bundled DJ Stems, Device Files, Saved AI Creations) with instantaneous "Load to Deck A" / "Load to Deck B" buttons.
3. **TrimScreen**: Detailed waveform audio editor with pinch-to-zoom, start/end draggable flags, loop toggle, and "Apply to Deck" cue setter.
4. **AiStudioScreen**: Text prompt composer, genre chips (House, Techno, Lo-Fi, Hip-Hop, EDM), mood selector, and audio synthesis visualizer.
5. **SettingsScreen**: Audio buffer configuration, crossfader curve selection, audio latency diagnostics, and export history.

---

### 5. Implementation Phases

- **Phase 1: Foundation & Audio Infrastructure**
  - Name app in `metadata.json` and `strings.xml`.
  - Implement `AudioEngine`, `DJPlayer` for Deck A and Deck B with volume, pitch, loop boundaries, and 3-band equalizer filters.
  - Implement bundled demo audio stems and waveform peak extraction.
- **Phase 2: Pro DJ Console UI (Dual Decks & Mixer)**
  - Build tactile Deck composables with vinyl rotation animation, cyan/amber neon theme, EQ dials, pitch sliders, and CUE/PLAY/SYNC pads.
  - Build central mixer with master VU meters, headphone cue toggles, and smooth crossfader.
- **Phase 3: Waveform Trimming & Loop Studio**
  - Interactive multi-color waveform canvas with draggable start/end handles and active loop region highlighting.
  - Real-time cue point preview and commit back to active deck.
- **Phase 4: Gemini AI Music Generator**
  - Implement `AiMusicGenerator` with Gemini prompt-to-music synthesis engine.
  - Create the AI Studio screen with generation progress, waveform generation visualizer, and direct-to-deck loading.
- **Phase 5: BPM Detection, Auto-Sync & Session Recording**
  - Implement BPM beat-grid analyzer and one-tap tempo synchronization.
  - Master output mix recorder saving to cache/music storage with playback and share support.
- **Phase 6: Verification & Polish**
  - Build verification with `compile_applet`, edge-to-edge layout checks, and responsive tablet/phone ergonomics.
