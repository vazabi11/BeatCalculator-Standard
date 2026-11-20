# Beat Calculator — Android V4.3

**Native Android / Java 17 / AGP 8.7.3 / Gradle 8.9 / Compile SDK 35**

รุ่น V4.3 ยึดภาพออกแบบหน้าราคาน้ำมันหลายแบรนด์ของผู้ใช้ และพัฒนาต่อจาก V4.1 ที่ยังคงมีครบทั้งเครื่องคิดเลข, ตัวแปลงหน่วย, ราคาทองไทย/Spot และ Offline Price Cache.

## หน้าจอราคาน้ำมันใหม่
- แสดงหัวข้อ + คำอธิบายสั้น ไม่ท่วมหน้าจอ
- เลือกแบรนด์ PTT OR, บางจาก, เปรียบเทียบ และ Shell (Shell ไม่แสดงราคาอัตโนมัติหากไม่มี API ที่อนุญาต)
- เลือกชนิดน้ำมัน: ทั้งหมด, Gasohol 91, Gasohol 95, E20, E85, Diesel
- ราคาจริงที่ตรวจสอบแล้วจะแสดงในตาราง แยกสีป้ายเกรด พร้อมวันประกาศและแหล่งข้อมูล
- ราคาที่เคยดึงสำเร็จบันทึกในเครื่อง; ออฟไลน์แสดงราคาสำรองพร้อมคำเตือนว่าไม่ใช่ราคาสด
- หากไม่มีราคา ทั้ง API และ Cache แสดงสถานะไม่พบราคา (ไม่ใช้ตัวเลขสมมติหรือค่า 0)
- รายละเอียดข้อผิดพลาดทางเทคนิคอยู่หลังการแตะข้อความสถานะ ไม่อยู่ในหน้าหลัก

## PTT OR
`PttOilService.java` เรียก `CurrentOilPrice` ตามคู่มือ SOAP ของ OR; หาก parser ไม่ได้ราคาที่ใช้ได้ ลอง `GetOilPrice` ย้อนหลัง 1–3 วัน แล้วใส่วันที่ประกาศจริงอย่างชัดเจน.

อ้างอิง:
- https://orapiweb.pttor.com/oilservice/OilPrice.asmx?op=CurrentOilPrice
- https://orapiweb.pttor.com/oilservice/OilPrice.asmx?op=GetOilPrice

ยังไม่สามารถรับรองผลดึงราคาออนไลน์จริงได้ จนกว่าจะติดตั้งและตรวจบนอุปกรณ์.

## สร้าง APK
อ่านคู่มือฉบับเต็ม `UPGRADE_V4_3_TH.md` หรือเปิด Repository GitHub Desktop แล้ว Commit/Push ไปที่ `main` เพื่อให้ GitHub Actions เรียก Build.

## ตรวจสอบ version
`app/build.gradle`: `versionCode 7`, `versionName '4.3'`.

## Preview
ไฟล์ `Preview-V4.3.html` ภายใน ZIP เป็นเพียงตัวอย่างจัดวางหน้าจอซึ่งแสดงเครื่องหมาย `—` แทนราคาที่ไม่มีอยู่ ไม่ทำ API calls และไม่ใช่ตัวแอปจริง.
