# 🔍 Local Lens — 100% On-Device AI Image Detector

> **Local AI Challenge Submission**  
> A high-performance, privacy-preserving browser extension that automatically detects AI-generated images directly inside the browser runtime using local WebAssembly/SIMD neural inference.  
> **No cloud APIs • No remote servers • No telemetry • 100% Offline**

---

## 🌟 Key Features

- **⚡ Native In-Browser Inference:** Runs an optimized EfficientNetV2-S neural network entirely within the browser via ONNX Runtime WebAssembly SIMD.
- **🚀 Viewport-First Priority Scanning:** Automatically prioritizes images currently visible on screen; off-screen images are queued so badges render near-instantly.
- **🏎️ Instant 0ms RAM Caching:** Previously scanned images render immediately upon scrolling without re-running model inference.
- **🎯 65% Calibrated Confidence Decision:** Evaluated with a strict 65% probability threshold (`P(AI) >= 0.65` = **AI** in Red; `P(AI) < 0.65` = **REAL** in Green).
- **🛡️ Multi-Platform Ecosystem:**
  - **Chrome Desktop:** Native Manifest V3 extension.
  - **Firefox Android & Desktop:** Automatic webpage image scanning add-on.
  - **Android Companion App:** On-device image checking & share-target receiver.
- **🔒 True Privacy:** Never uploads image bytes or URLs to any external server. Works completely offline in airplane mode.
- **📜 MIT Licensed:** Fully open-source under the MIT License with Apache-2.0 model weights.

---

## 🛠️ Quick Installation

### Google Chrome (Manifest V3)
1. Clone or download this repository.
2. Open Chrome and navigate to `chrome://extensions`.
3. Enable **Developer mode** in the top-right corner.
4. Click **Load unpacked** and select the extension folder.
5. Visit any website with images (e.g. Wikipedia, Reddit, Google Images). Images in view will automatically receive detection badges.

### Firefox on Android & Desktop
1. Download the pre-built `.xpi` add-on from releases.
2. In Firefox Android: Go to **Settings** → **About Firefox** → Tap logo 5 times to enable debug menu.
3. Return to Settings → **Install Extension from File** → Select the `.xpi`.
4. Browse any website—badges will appear automatically on images as you scroll.

---

## 📊 Benchmark Protocol & Decision Rules

To evaluate the extension against benchmark datasets:
- **Classification Threshold:** Fixed at **0.65**:
  - $P(\text{AI}) \ge 0.65 \implies$ **AI** (Synthetic)
  - $P(\text{AI}) < 0.65 \implies$ **REAL** (Authentic)

Every analyzed image on a webpage is programmatically annotated with:
- `data-local-ai-score`: Float string $[0.000000, 1.000000]$
- `data-local-ai-verdict`: `"ai"` or `"real"`
- `data-local-ai-confidence`: Selected class confidence score
- `data-local-ai-threshold`: `"0.65"`

---

## 📄 License

- Extension Source Code: **MIT License** (see `LICENSE`)
- Neural Model Weights: **Apache License 2.0** (Aedilic Inc. / Nonescape)
- ONNX Runtime Web: **MIT License** (Microsoft Corporation)
