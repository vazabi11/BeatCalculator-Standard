# อัปเดต Calculator by Sirawit Labs 5.1.0 ผ่าน GitHub Web

Repository เดิม: https://github.com/vazabi11/BeatCalculator-Standard
Branch: main. ใช้ ZIP ฉบับเต็ม ไม่ใช้ GitHub Desktop.

1. แตก ZIP เปิดโฟลเดอร์ BeatCalculator-Standard-Android ที่มี app/, tests/, settings.gradle, build.gradle, gradle.properties
2. เปิด https://github.com/vazabi11/BeatCalculator-Standard/upload/main
3. ลาก app/ และ tests/ กับไฟล์ build.gradle, settings.gradle, gradle.properties, README_TH.md, GITHUB_WEB_V5_1_TH.md จากภายในโฟลเดอร์ที่แตกไฟล์ ไม่ลากโฟลเดอร์แม่ขึ้นไปซ้อน ไม่ต้องลบ Repository เดิม. หากเว็บอัปโหลดโฟลเดอร์ไม่ได้ ให้เปิดตำแหน่งปลายทางบน GitHub แล้ว Add file → Upload files ทีละโฟลเดอร์.
4. Commit changes ลง main ด้วยข้อความ `Upgrade Calculator to Sirawit Labs 5.1.0`.
5. เปิด https://github.com/vazabi11/BeatCalculator-Standard/edit/main/.github/workflows/build-apk.yml แล้วแทนทั้งไฟล์ด้วย .github/workflows/build-apk.yml ใน ZIP. Commit ลง main. ถ้าไฟล์ไม่มี ให้ Code → Add file → Create new file ตั้งชื่อ .github/workflows/build-apk.yml.
6. เปิด https://github.com/vazabi11/BeatCalculator-Standard/edit/main/.github/workflows/diagnose-market.yml แล้วแทนทั้งไฟล์ด้วย .github/workflows/diagnose-market.yml ใน ZIP. Commit ลง main. ถ้าไฟล์ไม่มี ให้สร้างใหม่ด้วยชื่อดังกล่าว.
7. ตรวจ app/build.gradle: versionCode 7, versionName '5.1.0'. ตรวจ MarketTime.java, GoldFallbackParser.java, MainActivity.java และ app/src/test/java/com/beatoflove/standardcalculator/GoldFallbackParserTest.java ใน GitHub. ตรวจ Workflow ว่า Artifact ชื่อ SirawitLabs-Calculator-v5.1.0-debug-APK.
8. เปิด https://github.com/vazabi11/BeatCalculator-Standard/actions แล้วเลือก Build Calculator APK. การ Commit อาจรันอัตโนมัติ ให้เลือกรันล่าสุดหลังครบทุกไฟล์ หรือ Run workflow → main. ตรวจ PASS ของ Java tests และ :app:testDebugUnitTest / :app:assembleDebug. ดาวน์โหลด Artifact ชื่อ SirawitLabs-Calculator-v5.1.0-debug-APK เท่านั้น.
9. แตก Artifact ZIP ติดตั้ง app-debug.apk บน Samsung Galaxy S24 Ultra. หากลายเซ็น debug ต่างกันอาจอัปเดตทับไม่ได้: ห้ามถอนก่อนสำรองประวัติ/Memory/ราคาแคชที่ต้องการรักษา (แอปยังไม่มีระบบ export ทั้งหมด). การถอนแอปจะลบข้อมูลเดิม.
10. เปิดแอป: แถบบนแสดง Calculator / by Sirawit Labs / v5.1.0 และไอคอน + − × = ใหม่. แตะเวอร์ชันเพื่อเปิด About ซึ่งอ่าน Version จาก Package Manager จริง. ตรวจหน้า ราคาน้ำมัน / ราคาโลหะ และแคชเมื่อออฟไลน์.
11. เปิด Actions → Diagnose Live Market APIs (manual) → Run workflow → main. อ่านขั้น Inspect public endpoint statuses and field names. HTTP 200 ไม่ได้แปลว่ามีราคาที่ใช้ได้ ให้ดู Valid price rows และข้อมูลที่ API ส่งจริง. ถ้า PTT ยังไม่มีราคา ส่ง URL ของ Run มาเพื่อวิเคราะห์. Rakhagold oil API ยังไม่ได้เปิดใช้เพราะยังไม่ได้ตรวจ payload จริง.

## Tests
Standalone Java ผ่าน 104 กรณี: 42 + 25 + 11 + 10 + 10 + 6. JUnit 3 tests สำหรับ JSON ทองไทยจะรันผ่าน Gradle Actions (ยังไม่ได้รันที่นี่). Android APK และหน้าจอบนเครื่องจริงยังไม่ได้ยืนยัน.

## แหล่งข้อมูล
PTT OR: https://orapiweb.pttor.com/oilservice/OilPrice.asmx
Rakhagold: https://rakhagold.com/api-docs/ (แสดงเครดิต, ไม่เรียกเกิน 1 ครั้ง/นาที)
ไม่มีราคาสมมติในแอป. ราคาตามภาพออกแบบไม่ใช่ข้อมูลจริง. แสดงราคาและวันเวลาจากต้นทางหรือราคาแคชเท่านั้น.
