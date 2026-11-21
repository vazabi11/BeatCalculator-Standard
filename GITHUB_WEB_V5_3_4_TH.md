# SiraCalc v5.3.4 — Responsive Keypad & Market UI

Version Code 13; Application ID เดิม; Testing source.

## Changes
- Standard/Scientific: weighted page and keypad heights rather than fixed screen-metric key heights; result fit reacts to layout changes.
- Formula Pro: COMP keypad fits available viewport; SD/REG controls remain on one page and long reports open in a dialog.
- Financial: Accounting keypad uses weighted rows; the specialized financial tools open input/results in a separate dialog. Existing calculation functions retained.
- Markets: separate price, signed change, and 10sp muted metadata. Compare only the preceding valid quote of the same symbol and provider. Green/up, red/down, neutral for cached/stale/no comparison. Daily reference displays provider date (not invented time).
- Feed labels shortened to fit; original default and providers remain. Thai gold also shows direction and actual change when the previous comparable cached quote is available.
- No changes to workflow definitions, signing, application ID, or provider endpoints.

## GitHub Web fallback
1. Open https://github.com/vazabi11/BeatCalculator-Standard/upload/main
2. Extract the source ZIP. Upload app/ and tests/ contents at the project root (do not create an extra nested project folder). Optionally upload the new README and this guide/test report. Existing .github/workflows files are unchanged; both are included in this archive.
3. Commit message: SiraCalc v5.3.4 - adaptive calculator layout and market cards.
4. Confirm app/build.gradle versionName 5.3.4 and versionCode 13. GitHub Actions auto-builds on main.
5. Open https://github.com/vazabi11/BeatCalculator-Standard/actions/workflows/build-apk.yml. Verify latest commit and successful build. Expected artifact Calculator-v5.3.4 (not yet generated when this source was prepared).
6. To publish a public testing APK explicitly select Run workflow > publish_prerelease after checking the build/device. Do not reuse old release tags or keys.

## Acceptance checks
- Standard, Scientific, Formula Pro COMP and Financial Accounting: portrait viewport, bottom key visible, no vertical page scrolling; test small phone, large font, split-screen and resize/fullscreen. SD/REG data entry and reports, Financial specialized forms remain accessible through dialogs/internal data scrolling.
- Markets Thai Gold/Metals/Crypto/Forex: provider-consistent comparisons; unchanged/initial/cached quotes neutral, up/down color and signed delta; metadata smaller; date-only for daily rates; no false live claim.
- All 8 tabs, horizontal gestures, numeric input, memory, offline cache and refresh must regress-test on physical device.
- Very small viewport or extreme accessibility settings may still reduce touch target/text size; not a verified guarantee for every phone. Existing portrait orientation restriction retained.

## Deployment status
GitHub write attempted using atomic tree creation; denied HTTP 403 Resource not accessible by integration. No commit/push or new APK verified in this run. Android compilation/device/live API QA not performed locally. Standalone 341 checks pass; Java syntax parse 37 files and XML parse pass. No Stable Release claim.
