# SiraCalc 5.3.5 — Responsive UI Update
Prepared source, not an APK. Base GitHub main d071160960a0c47d9cf2200436c60801764d9510 (5.3.4/code 13). New version 5.3.5/code 14. Application ID unchanged.

- Currency Convert: removed internal keypad; system signed-decimal keyboard, adaptive From/To and swap/refresh rows; selected rate source and cache behavior retained. Exchange Rates remains a scrollable reference table.
- Converter: compact native form with wrapping unit labels/results, 48dp minimum controls and measured-width adaptive actions; preserves offline conversions, swapping and copying. Tax remains excluded from Converter as in 5.3.4.
- About: no GitHub button or repository URL; retains installed package version, branding, icon and Close.
- Existing six other tabs, market providers, calculator engine, signing configuration and package ID unchanged. Soft input resize is scoped to Currency/Converter; fullscreen/IME behavior needs device QA.
- Build workflow adds PR trigger and structural UI checks; existing verification, artifact naming and explicit manual publication gate retained. Diagnose workflow unchanged.
