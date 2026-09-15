package com.pugplayz.overlayforge

internal const val SAMPLE_HTML = """<div class="hud">
  <div class="label-row">
    <span>HEALTH</span>
    <span class="value">82 / 100</span>
  </div>
  <div class="bar-shell">
    <div class="bar-fill"></div>
  </div>
</div>"""

internal const val SAMPLE_CSS = """.hud {
  position: absolute;
  left: 5%;
  top: 7%;
  width: 42%;
  padding: 14px 16px;
  border: 2px solid rgba(255,255,255,.24);
  border-radius: 16px;
  background: rgba(7,14,20,.76);
  box-shadow: 0 10px 35px rgba(0,0,0,.38);
  font-family: system-ui, sans-serif;
  color: white;
}
.label-row {
  display: flex;
  justify-content: space-between;
  margin-bottom: 9px;
  font-size: 14px;
  font-weight: 800;
  letter-spacing: .12em;
}
.value { color: #66f0b3; }
.bar-shell {
  height: 15px;
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
}"""
