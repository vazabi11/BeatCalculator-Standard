# Beat Calculator V5.0 — Full Android Project

## สถานะ
ซอร์สโค้ด V5.0 พร้อมชุดทดสอบ Java และ GitHub Actions สำหรับสร้าง Debug APK แต่ยัง **ไม่ได้** ตรวจยืนยันการ Build APK, การทำงานบน S24 Ultra หรือความสำเร็จของ API สดในสภาพแวดล้อมนี้

## การแก้ไขปัญหาราคาน้ำมัน PTT OR
- ส่ง SOAP ตามเอกสาร `OilPrice.asmx` พร้อม `CurrentOilPrice` และ `SOAPAction`
- ทดสอบข้อมูลภาษา `en` และ `th` กรณีรายการราคาไม่สมบูรณ์
- หากไม่มีราคาที่ใช้ได้ ลอง `GetOilPrice` ย้อนหลังไม่เกิน 3 วัน และแสดงวันประกาศจากต้นทาง
- รองรับชื่อฟิลด์ราคาหลายแบบ (`PRICE`, `RETAIL_PRICE`, `PriceToday`) และรายการ `DataAccess` / `Table`
- รองรับเวลาประกาศไม่มี timezone โดยใช้เขตเวลา Asia/Bangkok (เฉพาะฟิลด์ที่ตีความได้)
- ป้องกันราคา 0/NaN และ SOAP ที่มี entity อันตราย
- รักษาราคาที่บันทึกไว้เมื่อ API ขัดข้อง โดยระบุว่าไม่ใช่ราคาสด

## การแก้ไขปัญหาทองไทย HTTP 402
- ThaiGoldToday เป็นแหล่งเดิม เอกสารยังอ้างว่า API สาธารณะ แต่โทรศัพท์พบ HTTP 402 ซึ่งเป็นการจำกัดการเข้าถึง
- เพิ่ม fallback ของ `https://rakhagold.com/wp-json/priceradar/v1/gold/latest` ซึ่งเผยแพร่เอกสาร API สาธารณะ โดยต้องแสดงเครดิต
- ใช้ตัวอ่านข้อมูลที่ยอมรับเฉพาะราคาทองแท่งและรูปพรรณสี่ช่องและเวลาประกาศจริง ห้ามสร้างราคาจำลอง
- บันทึกชื่อแหล่งข้อมูลลง offline cache และแสดงบนหน้าจอ
- หากทั้งสองแหล่งไม่ตอบข้อมูลตามโครงสร้างที่รองรับ ยังคงแสดงราคาสำรองล่าสุดและ error อย่างโปร่งใส
- ไม่มีการฝัง API key ของสมาชิกสมาคมค้าทองคำ (ต้องได้รับสิทธิ์จากเจ้าของก่อน)

## สร้าง APK
1. แตก ZIP นี้
2. เปิด GitHub Desktop แล้ว Clone `https://github.com/vazabi11/BeatCalculator-Standard.git`
3. คัดลอก **เนื้อหาทั้งหมดภายใน** `BeatCalculator-Standard-Android/` ไปทับในโฟลเดอร์ที่ Clone โดย **ห้ามลบ `.git/`**
4. ตรวจสอบไฟล์ `.github/workflows/build-apk.yml` และ `app/build.gradle` (`versionName '5.0'`)
5. GitHub Desktop → Changes → Commit to main → Push origin
6. GitHub → Actions → `Build Calculator APK` → ดู Build ล่าสุด หรือกด Run workflow
7. เมื่อผ่านทุกขั้นตอน Download Artifact `BeatCalculator-v5-debug-APK`, แตก ZIP แล้วติดตั้ง `app-debug.apk`

## วินิจฉัยข้อมูลสดผ่าน GitHub Actions
ไปที่ Actions → **Diagnose Live Market APIs (manual)** → Run workflow → เปิด Log ขั้นตอน **Inspect public endpoint statuses and field names**
จะรายงาน HTTP status และ *ชื่อฟิลด์* ที่ API ส่งมา โดยไม่พิมพ์ค่าราคาหรือโทเคนใน Log ช่วยยืนยันปัญหาได้โดยไม่เดาข้อมูลต้นทาง

## แหล่งข้อมูล
- PTT OR: https://orapiweb.pttor.com/oilservice/OilPrice.asmx?op=CurrentOilPrice
- ThaiGoldToday: https://www.thaigoldtoday.com/api-docs
- Rakhagold: https://rakhagold.com/api-docs/
- สมาคมค้าทองคำ (API สำหรับสมาชิก ต้องมี Token): https://announce.goldtraders.or.th/api-docs.html

## ข้อจำกัดสำคัญ
- Rakhagold เผยแพร่ endpoint ไว้จริง แต่ยังไม่สามารถยืนยัน JSON สดจากอุปกรณ์จริงในสภาพแวดล้อมนี้ จึงอาจต้องปรับตัวอ่านอีกครั้งจาก Log ของแอป/Workflow
- GitHub API connection ของ ChatGPT อนุญาตให้อ่าน Repository แต่ยังไม่อนุญาตให้เขียน จึงต้อง Push จาก GitHub Desktop
- สำรองข้อมูลแอปก่อนถอนการติดตั้งหรือเปลี่ยน Debug APK ที่ใช้กุญแจลงนามคนละชุด
