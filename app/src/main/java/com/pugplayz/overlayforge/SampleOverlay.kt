package com.pugplayz.overlayforge

internal const val SAMPLE_HTML = """<div class="hud">
  <div class="label-row">
    <span class="label">HEALTH</span>
    <span class="value">82 / 100</span>
  </div>
  <div class="bar-shell">
    <div class="bar-fill"></div>
  </div>
</div>"""

internal const val SAMPLE_CSS = """.hud {
  position: absolute;
  left: 4vw;
  top: 5vh;
  width: clamp(150px, 42vw, 360px);
  max-width: calc(100vw - 8vw);
  padding: clamp(8px, 1.8vw, 16px) clamp(10px, 2vw, 18px);
  border: clamp(1px, .25vw, 2px) solid rgba(255,255,255,.24);
  border-radius: clamp(10px, 2vw, 18px);
  background: rgba(7,14,20,.78);
  box-shadow: 0 8px 28px rgba(0,0,0,.38);
  font-family: system-ui, -apple-system, sans-serif;
  color: white;
}
.label-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: clamp(6px, 1.4vw, 14px);
  min-width: 0;
  margin-bottom: clamp(6px, 1.2vw, 10px);
  font-size: clamp(9px, 2.25vw, 17px);
  line-height: 1.1;
  font-weight: 800;
  letter-spacing: .08em;
}
.label,
.value {
  white-space: nowrap;
}
.value {
  color: #66f0b3;
  margin-left: auto;
}
.bar-shell {
  height: clamp(8px, 2vw, 16px);
  overflow: hidden;
  border-radius: 999px;
  background: rgba(255,255,255,.12);
}
.bar-fill {
  width: 82%;
  height: 100%;
  border-radius: inherit;
  background: linear-gradient(90deg, #35d6e8, #66f0b3);
  box-shadow: 0 0 18px rgba(53,214,232,.55);
}

@media (max-aspect-ratio: 3/4) {
  .hud {
    width: min(58vw, 300px);
  }
  .label-row {
    font-size: clamp(8px, 2.6vw, 14px);
    letter-spacing: .05em;
  }
}"""
