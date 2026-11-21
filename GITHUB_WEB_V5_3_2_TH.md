# SiraCalc / Calculator V5.3.2 — testing build

ชื่อบนมือถือและ Header ทดลองใช้ SiraCalc; Application ID เดิม, versionName 5.3.2, versionCode 11.
ชื่อไฟล์ Artifact คงมาตรฐานเดิม Calculator-v5.3.2.apk; ยังไม่ตรวจชื่อทางการค้า.

## อัปเดตผ่าน GitHub Web
1. แตก ZIP; เปิด https://github.com/vazabi11/BeatCalculator-Standard/upload/main
2. อัปโหลด app/, tests/, build.gradle, settings.gradle, gradle.properties, GITHUB_WEB_V5_3_2_TH.md และ TEST_RESULTS_V5_3_2.txt จากภายในโฟลเดอร์หลัก (ไม่ซ้อนโฟลเดอร์)
3. Commit: Upgrade Calculator to v5.3.2 SiraCalc navigation and market fixes
4. แก้ .github/workflows/build-apk.yml และ diagnose-market.yml ด้วยไฟล์ใน ZIP ผ่านหน้า Edit ของ GitHub; คง opt-in public prerelease.
5. ตรวจ versionName 5.3.2 / code 11. Actions > Build Calculator APK > Run workflow > main; ไม่ติ๊ก publish_prerelease ก่อนทดสอบ.
6. ดาวน์โหลด Artifact Calculator-v5.3.2 แล้วติดตั้ง Calculator-v5.3.2.apk; หากลายเซ็นไม่ตรง ห้ามถอนตัวเดิมก่อนสำรองข้อมูล.
7. Actions > Diagnose Live Market APIs (manual) > Run workflow และตรวจรายงานจริง.

## ทดสอบบนเครื่อง
- Swipe 8 Tabs ทั้งสองทิศ ไม่ wrap; ช่องแก้ข้อความ/ผลลัพธ์ที่เลื่อนแนวนอน และ dropdown ไม่แย่ง gesture.
- Formula Pro: 785^3 = 483,736,625; Ans, SHIFT/ALPHA, SD, REG; สำรวจผลลัพธ์ยาวและเศษส่วน.
- Fuel: เลือก PTT OR สุราษฎร์ธานี; default อำเภอเมืองหากพบชื่อจาก API; เลือกอำเภออื่น/All districts/สลับจังหวัด. Bangchak บังคับ Bangkok reference. Shell/Esso ไม่มีราคาสมมติ.
- Markets: Metals/Crypto/Forex มี Feed/Symbol; ค่าเริ่มต้น Reference/latest available, fetch on open/manual; auto streaming เฉพาะหน้าที่เปิด. แยก daily reference / latest snapshot / cached จาก live. ตรวจเวลาและแหล่งข้อมูลจริง.
- Currency: Bank Rates/Compare ดึงจาก public boards. เพิ่ม adapter Krungsri/Krungthai ตามโครงสร้างตารางทางการ; บางธนาคารยังอาจไม่ตอบหรือไม่ส่งโครงสร้างตาราง. ไม่มี rate สมมติ.
- Financial, Standard, Scientific, Converter: ตรวจครบคำสั่งบนเครื่อง; standalone tests ไม่ใช่ UI tests.
- tests/device_smoke.py ใช้กับ adb และเครื่องที่ติดตั้ง APK แล้ว (smoke เท่านั้น ยังไม่รันใน sandbox)

## ข้อจำกัด
ไม่มี Android SDK/Gradle/Emulator และ DNS ภายนอกใน sandbox นี้. Android compilation, JUnit fixture tests, actual API, UI smoke, battery measurement และ acceptance tests ยังไม่รัน. ไม่มีการ Push/Release อัตโนมัติจากรอบนี้.
