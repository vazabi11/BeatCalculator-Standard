# Beat Calculator — Standard V2.0

Android Native (Java 17) · Offline · ปรับหน้าตาตามภาพอ้างอิง Windows Calculator / Standard / Dark Theme

## สิ่งที่มีในโปรเจกต์

- หน้าจอธีมมืดแบบ Standard: 4 คอลัมน์ × 6 แถว + แถบ Memory
- บวก ลบ คูณ หาร / ทศนิยม / เปอร์เซ็นต์ / เปลี่ยนเครื่องหมาย ±
- CE / C / ลบทีละหลัก / 1/x / x² / √x
- การกดเท่ากับซ้ำ, คำนวณต่อเนื่องจากซ้ายไปขวา
- History สูงสุด 50 รายการ (แตะผลเก่าเพื่อเรียกใช้)
- Memory: MC, MR, M+, M−, MS, M▾
- ปุ่มคัดลอกผลลัพธ์, เปิดเต็มจอ, ย่อ และปิดแอป
- บันทึกประวัติและหน่วยความจำในเครื่อง (SharedPreferences)
- ไม่มี Internet permission, ไม่มีโฆษณาหรือ Analytics
- เมนู "ล้างข้อมูลทั้งหมด" ล้างการคำนวณ ประวัติ และหน่วยความจำ
- ใช้ Application ID ใหม่ `com.beatoflove.standardcalculator` เพื่อไม่ใช้ข้อมูลของรุ่นเก่า

## แนะนำ: สร้าง Repository ใหม่บน GitHub เพื่อทดสอบ APK

1. ดาวน์โหลด `BeatCalculator-Standard-Android-v2.zip` แล้ว Extract All บน Windows 11
2. เปิด GitHub → **New repository** ตั้งชื่อ `BeatCalculator-Standard` เลือก Private หรือ Public ตามต้องการ และ **อย่าติ๊ก Add README**
3. ในหน้า repository ใหม่ เลือก **uploading an existing file** หรือ **Add file → Upload files**
4. เปิดโฟลเดอร์ที่แตก ZIP และเลือก **เนื้อหาทั้งหมดภายในโฟลเดอร์** (ไม่ใช่ลากโฟลเดอร์หลักครอบซ้ำ) ให้รวม `.github` ด้วย
5. กด **Commit changes** ไปยัง `main`
6. ตรวจสอบไฟล์ต่อไปนี้ที่ root ของ Repository:
   - `.github/workflows/build-apk.yml`
   - `settings.gradle`
   - `build.gradle`
   - `app/build.gradle`
   - `app/src/main/AndroidManifest.xml`
7. เปิดแท็บ **Actions** → เลือก **Build Calculator APK** → **Run workflow** → เลือก `main` → **Run workflow**
8. เมื่อ job สำเร็จ (เขียว) → เปิด workflow run → ส่วน **Artifacts** → ดาวน์โหลด `BeatCalculator-Standard-debug-APK`
9. แตก ZIP จาก Artifact จะได้ `app-debug.apk`
10. ส่ง APK ไป Samsung Galaxy S24 Ultra และติดตั้งจาก My Files โดยยอมรับเฉพาะไฟล์ที่คุณเพิ่ง Build เอง

**สำคัญ:** อย่าใช้ Workflow รุ่นเก่าที่มี `android-actions/setup-android@v3` ซึ่งเคยล้มเหลวจากการพยายามติดตั้งแพ็กเกจ SDK ชื่อ `tools`  Workflow ใหม่นี้ใช้ Android SDK ที่ติดตั้งอยู่บน GitHub runner และเรียก `sdkmanager` โดยตรง

## ถ้าต้องใช้ Repository เก่า `vazabi11/BeatCalculator`

แนะนำสร้าง Repository ใหม่ก่อน เพื่อกันไฟล์เก่า `main.yml` รบกวน และป้องกันลบข้อมูลย้อนหลังโดยไม่ได้ตั้งใจ เมื่อโปรเจกต์ใหม่ Build สำเร็จค่อย Archive หรือ Delete ของเก่าด้วยตนเอง หากไม่ต้องการเก็บโค้ดรุ่นเก่า

## เปิดโปรเจกต์ใน Android Studio

1. เปิด Android Studio → **Open** → เลือกโฟลเดอร์ที่มี `settings.gradle`
2. โปรเจกต์ใช้ Android Gradle Plugin 8.7.3 / Gradle 8.9 / JDK 17 / Compile SDK 35
3. โปรเจกต์ ZIP นี้ **ไม่มี Gradle Wrapper binaries** (`gradlew`, `gradle-wrapper.jar`) เพราะไม่สามารถดาวน์โหลดไฟล์ทางการภายในสภาพแวดล้อมที่สร้าง ZIP นี้ได้ ดังนั้นแนะนำ Build ผ่าน GitHub Actions ตามขั้นตอนด้านบน
4. หากจะใช้ Android Studio ต้องมี Gradle 8.9 ในเครื่อง หรือเพิ่ม Gradle Wrapper ด้วย Gradle ที่ติดตั้งแล้ว (`gradle wrapper --gradle-version 8.9`) ก่อน Sync
5. เมื่อ Sync สำเร็จ ใช้ **Build → Build APK(s)** หรือชื่อเมนูใหม่ **Generate APK(s)** แล้วอ่านผลใน Build output
6. APK จะอยู่ใน `app/build/outputs/apk/debug/app-debug.apk`

## ทดสอบ Logic บนคอมพิวเตอร์ (ไม่ต้องมี Android SDK)

จาก root ของโปรเจกต์:

```bash
mkdir -p tests/out
javac -encoding UTF-8 -d tests/out app/src/main/java/com/beatoflove/standardcalculator/CalculatorEngine.java tests/CalculatorEngineTest.java
java -cp tests/out com.beatoflove.standardcalculator.CalculatorEngineTest
```

ผลที่ตรวจสอบได้ในขั้นสร้างโปรเจกต์: `PASS 36 calculator engine tests` (การคำนวณระดับ Java)

## วิธีทดสอบบนมือถือ

- `125 × 4 =` → `500`
- `200 + 10 % =` → `220`
- `8 + 2 × 3 =` → `30` (เครื่องคิดเลข Standard ประมวลผลแบบต่อเนื่องซ้ายไปขวา)
- `9 x²` → `81`
- `81 √x` → `9`
- `1 ÷ 0 =` → `Cannot divide by zero`
- `15 MS` → `C` → `MR` → `15`
- เปิด History และเลือกผลเก่าเพื่อเรียกกลับมาใช้งาน
- เปิดเมนู `☰` → ล้างข้อมูลทั้งหมด เพื่อเริ่มต้นจากศูนย์

## ข้อจำกัดการยืนยัน

- โค้ดแกนคำนวณทดสอบด้วย `javac/java` แล้ว
- ไฟล์ HTML ตัวอย่างเป็นตัวทดลองแยกจาก Native Android และตรวจด้วย Chromium แล้ว
- ยังไม่มีการคอมไพล์ APK จริงในสภาพแวดล้อมที่สร้างไฟล์นี้ (ไม่มี Android SDK / Gradle)
- ต้องดูผล GitHub Actions ครั้งแรกก่อนจึงจะยืนยันว่า APK สร้างได้สำเร็จ
