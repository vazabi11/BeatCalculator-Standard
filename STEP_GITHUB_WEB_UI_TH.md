# Beat Calculator — Full ZIP + GitHub Web Steps + UI Target

แพ็กนี้ประกอบด้วย:
- โปรเจกต์ Android ฉบับเต็ม (ฐาน V5.0)
- ไฟล์อ้างอิงหน้าจอเป้าหมาย `UI_TARGET_FUEL_SCREEN.png`
- คู่มือนี้สำหรับอัปโหลดผ่าน GitHub Web

## เป้าหมาย
1. ใช้ Repository เดิม `vazabi11/BeatCalculator-Standard`
2. อัปเดตซอร์สโค้ดฉบับเต็มผ่าน GitHub บนเว็บ
3. ใช้หน้าจออ้างอิงเพื่อปรับหน้า “ราคาน้ำมัน” ให้ดูใกล้เคียงต้นแบบ
4. Build APK ผ่าน GitHub Actions

## ไฟล์สำคัญในโปรเจกต์
- `app/src/main/java/com/beatoflove/standardcalculator/MainActivity.java`  
  จัดหน้าจอและโครง UI หลัก
- `app/src/main/java/com/beatoflove/standardcalculator/PriceService.java`  
  เรียกข้อมูลราคาน้ำมัน/ทอง
- `app/src/main/java/com/beatoflove/standardcalculator/PttOilService.java`  
  ดึงข้อมูล PTT OR แบบ SOAP
- `app/src/main/java/com/beatoflove/standardcalculator/OilPriceParser.java`  
  แปลงผลตอบกลับ PTT OR
- `app/src/main/java/com/beatoflove/standardcalculator/GoldFallbackParser.java`  
  ใช้ข้อมูลทองไทยจากแหล่งสำรอง
- `.github/workflows/build-apk.yml`  
  ใช้สร้าง APK
- `.github/workflows/diagnose-market.yml`  
  ใช้ตรวจสถานะ API

## แนวทางหน้าตาตามภาพอ้างอิง
ภาพอ้างอิง: `UI_TARGET_FUEL_SCREEN.png`

องค์ประกอบที่ต้องการให้ใกล้เคียง:
- แถบแท็บด้านบน: Standard / Advance / Science / แปลงหน่วย / ราคาน้ำมัน / ราคาโลหะ
- ส่วนหัว “ราคาน้ำมันหลายแบรนด์” ขนาดใหญ่
- ช่องเลือก “แบรนด์” และ “ชนิดน้ำมัน” แบบ dropdown ขนาดใหญ่
- การ์ดแสดงราคาของแบรนด์ที่เลือก
- ป้ายสถานะอัปเดตล่าสุด
- ปุ่ม “ตรวจสอบราคาใหม่” ขนาดใหญ่
- กล่องข้อมูลออฟไลน์: หากออฟไลน์หรือดึงข้อมูลไม่ได้ จะใช้ราคาล่าสุดที่บันทึกไว้

## ขั้นตอนอัปเดตบน GitHub Web

### 1) แตก ZIP
แตกไฟล์นี้บนคอมให้เห็นโฟลเดอร์ `BeatCalculator-Standard-Android`

### 2) เปิด Repository บน GitHub
เปิด:
`https://github.com/vazabi11/BeatCalculator-Standard`

### 3) อัปโหลดไฟล์ฉบับเต็ม
- กด **Code**
- กด **Add file** → **Upload files**
- เปิดโฟลเดอร์ `BeatCalculator-Standard-Android`
- ลากไฟล์และโฟลเดอร์ทั้งหมดภายในโฟลเดอร์นี้ใส่หน้าเว็บ GitHub
- รอให้รายการไฟล์โหลดครบ
- ในช่อง Commit message ใส่:
  `Upgrade to full package with UI reference`
- กด **Commit changes**

> ถ้าอัปโหลดโฟลเดอร์ `.github` ไม่ได้จากการลากทั้งโฟลเดอร์ ให้เข้าไปสร้างไฟล์ทีละไฟล์บนเว็บแทน:
> - `.github/workflows/build-apk.yml`
> - `.github/workflows/diagnose-market.yml`

### 4) ตรวจเวอร์ชัน
เปิดไฟล์ `app/build.gradle` แล้วตรวจว่าอย่างน้อยมี:
- `versionCode 6`
- `versionName '5.0'`

### 5) Build APK
ไปที่แท็บ **Actions**
- เลือก **Build Calculator APK**
- กด **Run workflow**
- รอจนขึ้น **Success**
- เข้า run ล่าสุดแล้วดาวน์โหลด artifact `BeatCalculator-v5-debug-APK`

### 6) ติดตั้ง APK
- แตกไฟล์ artifact ZIP
- นำ `app-debug.apk` ไปติดตั้งบนมือถือ Android

## หมายเหตุสำคัญ
แพ็กนี้เป็น “ฉบับเต็ม + ภาพอ้างอิง UI” เพื่อใช้เป็นฐานทำงานบน GitHub Web
ถ้าต้องการให้โค้ดหน้าจอถูกปรับให้ตรงภาพอ้างอิงแบบลงมือแก้ในซอร์สจริง
ให้โฟกัสแก้ที่ `MainActivity.java` โดยเฉพาะเมธอด:
- `buildShell()`
- `selectMode()`
- `buildPrice(boolean fuel)`
- `renderFuel()`
- `renderThaiGold()`
- `priceRow(String name, String value)`

## ข้อแนะนำการปรับ UI ในโค้ด
1. ลดข้อความอธิบายยาว ๆ ใต้หัวข้อ
2. ใช้กล่องการ์ดสำหรับผลลัพธ์ราคา
3. รวมส่วนแหล่งข้อมูลและเวลาอัปเดตไว้ในท้ายการ์ด
4. ใช้ข้อความสั้นเมื่อโหลดไม่สำเร็จ เช่น
   - “ยังไม่มีข้อมูลราคาที่ตรวจสอบได้”
   - “ใชัราคาล่าสุดที่บันทึกไว้”
5. เก็บรายละเอียดทางเทคนิคไว้ในปุ่ม “ดูรายละเอียดการเชื่อมต่อ”

