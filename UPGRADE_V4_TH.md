# อัปเกรด BeatCalculator-Standard จาก V2.0 → V4.0

**ตรวจเมื่อ 19 พ.ย. 2026:** Repository `vazabi11/BeatCalculator-Standard` ยังเป็น V2.0; ไม่มี PriceService.java จึงต้องอัปเดตไฟล์ทั้งหมด ไม่ใช่แค่ไฟล์ราคา

## วิธีที่แนะนำ — GitHub Desktop บน Windows

1. ดาวน์โหลด ZIP V4.0 ฉบับเต็มจาก ChatGPT แล้วแตกไฟล์ จะเห็นโฟลเดอร์ `BeatCalculator-Standard-Android` ภายในมี `settings.gradle`, `app`, `tests`, `.github`
2. ดาวน์โหลดและติดตั้ง GitHub Desktop จาก `https://desktop.github.com/` (หากยังไม่มี)
3. ลงชื่อเข้า GitHub > **File → Clone repository** > เลือก `vazabi11/BeatCalculator-Standard` > **Clone**
4. ใน GitHub Desktop กด **Repository → Show in Explorer** เพื่อเปิดโฟลเดอร์ Repository ใน Windows
5. คัดลอก **เนื้อหาทั้งหมดภายใน** `BeatCalculator-Standard-Android` (ไม่ใช่ตัวโฟลเดอร์ซ้อน) ไปวางทับใน Repository ที่เพิ่ง Clone โดยรวมไฟล์ `.github/workflows/build-apk.yml` ผ่าน File Explorer
6. ตรวจสอบในโฟลเดอร์ GitHub Repository:
   - `settings.gradle` อยู่ระดับบนสุด
   - `app/src/main/java/com/beatoflove/standardcalculator/MainActivity.java`
   - `app/src/main/java/com/beatoflove/standardcalculator/PriceService.java`
   - `app/src/main/java/com/beatoflove/standardcalculator/OilPriceParser.java`
   - `app/src/main/java/com/beatoflove/standardcalculator/FreshnessPolicy.java`
   - `tests/MarketFeedsTest.java`
   - `.github/workflows/build-apk.yml`
7. กลับ GitHub Desktop > แท็บ **Changes** > Commit summary: `Upgrade Beat Calculator to V4.0` > **Commit to main** > **Push origin**
8. เปิด `https://github.com/vazabi11/BeatCalculator-Standard/actions` > **Build Calculator APK** > ดู Run ที่เกิดจาก Push
9. ผ่านทั้ง 3 ชุด Unit Test และ APK Build จะมี Artifact `BeatCalculator-MultiMarket-v4-debug-APK`
10. ดาวน์โหลด Artifact แตก ZIP ได้ `app-debug.apk`, ทดสอบบน S24 Ultra; ตรวจเมนูราคาน้ำมันและทองไทยเมื่อออนไลน์

## ถ้าต้องการอัปเดตผ่าน GitHub เว็บอย่างเดียว

อัปโหลดโฟลเดอร์ `app` และ `tests` พร้อม `build.gradle`, `settings.gradle`, `gradle.properties` จาก ZIP V4 ในแท็บ Code → Add file → Upload files (ตรวจโครงสร้างทุกครั้ง). GitHub เว็บอาจไม่รับโฟลเดอร์ที่ขึ้นต้นด้วยจุด ให้เปิด `.github/workflows/build-apk.yml` > **Edit file** แล้ววางโค้ดจาก ZIP V4 > Commit to main

## การทดสอบหลังติดตั้ง

1. เปิด Basic/Standard/Advance/Science และทดลองคำนวณ
2. เข้า ราคาน้ำมัน > เปรียบเทียบ > E20, เปิดเน็ต ตรวจว่ามี PTT OR/บางจากที่มีค่าจริง และดูเวลาอัปเดต
3. เลือก PTT OR และ บางจากทีละแบรนด์ ตรวจการกรองชนิดน้ำมัน
4. เลือก Shell: ต้องแสดงคำเตือนว่าไม่รองรับ API และลิงก์ EPPO ไม่ควรมีราคาที่แอปสมมติ
5. เข้า ราคาทองไทย 96.5% ตรวจรับซื้อ/ขายออกทั้งทองแท่งและรูปพรรณ; ตรวจเวลาและเครดิต ThaiGoldToday
6. เลือก โลหะตลาดโลก → XAU, XAG, XPT, XPD แสดงราคาเป็น USD/oz ไม่ใช่บาททองไทย
7. ปิดอินเทอร์เน็ต → ต้องแจ้งว่าโหลดไม่ได้ โดยไม่สร้างตัวเลขจำลอง

หากแอปเดิมมีประวัติสำคัญ: อย่า Uninstall ทันที เพราะ APK Debug ที่เซ็นด้วยกุญแจใหม่อาจติดตั้งทับไม่ได้และการถอนแอปลบประวัติ
