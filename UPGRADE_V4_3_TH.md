# Beat Calculator — อัปเกรด V4.1 → V4.3 (GitHub Desktop)

## สิ่งที่เปลี่ยน
- หน้าราคาน้ำมันปรับตาม UI Mockup ที่ยืนยัน: ธีมมืด, หัวข้อสั้น, dropdown มีขอบและลูกศร, การ์ดราคาแบบตาราง, สีแยกเกรด, ปุ่มตรวจสอบราคาใหม่สีฟ้า
- ค่าเริ่มต้น: แบรนด์ `PTT OR` / ชนิด `ทั้งหมด` เพื่อแสดงตารางเมื่อมีราคาใน API หรือแคชจริง
- หัวข้อคำอธิบายยาว ถูกลดเป็น 1 บรรทัด รายละเอียด Error ดูได้โดยแตะข้อความสถานะ
- การ์ดแสดงวันประกาศจากต้นทาง; ราคาสำรองแสดงเวลาที่บันทึกในเครื่องแยกต่างหาก
- ไม่สร้างราคา/วันเวลาจำลองในแอป: หากไม่มีค่าที่ผ่านการตรวจสอบ จะแสดง “ยังไม่มีราคาที่ตรวจสอบได้”
- Shell ยังไม่มี API ที่ยืนยันสิทธิ์ จึงแสดงสถานะสั้นและลิงก์แหล่งข้อมูลเท่านั้น
- รวม PTT OR parser diagnostics และ GetOilPrice วันย้อนหลัง 1–3 วันจากชุดซ่อม V4.2 โดยผล API จริงยังต้องทดสอบ
- แก้การเรียกข้อมูลซ้ำช่วง onCreate/onResume; คงประวัติ / Memory และราคาสำรองจาก V4.1

## ข้อสำคัญ
- สถานะปัจจุบัน GitHub Repository ของคุณใช้ `versionName '4.1'`. ให้ใช้ **FULL ZIP V4.3** ที่ให้มา ไม่ใช่อัปเดต V4.0 เก่า.
- ยังไม่ผ่านการ Build APK บน GitHub หรือทดสอบราคา API สดบน S24 Ultra ในรอบนี้.
- ภาพ preview HTML/PNG เป็น **ภาพวางหน้าจอ** ไม่ใช่ screenshot จาก APK จริง. เครื่องหมาย `—` ใน preview คือช่องราคาแบบไม่มีข้อมูล ไม่ใช่ราคาตลาด.
- แอปเวอร์ชันใหม่มี `applicationId` เดิม แต่ใช้ `versionCode 7`, `versionName 4.3`; หากเซ็นด้วยคีย์ debug ต่างเครื่อง อาจติดตั้งทับไม่ได้ และการถอนแอปทำให้ cache / ประวัติที่บันทึกในเครื่องหาย

## อัปเดตบน GitHub Desktop
1. เปิด GitHub Desktop → `BeatCalculator-Standard` บน branch `main` → **Fetch origin** / **Pull origin** หากมี
2. เปิดโฟลเดอร์โปรเจกต์ด้วย **Repository → Show in Explorer**
3. แตก **BeatCalculator-MultiMarket-Android-v4.3.zip** (ZIP ฉบับเต็ม)
4. เข้าไปด้านในโฟลเดอร์ `BeatCalculator-Standard-Android` ของ ZIP ที่แตกแล้ว แล้วกด `Ctrl+A` และ `Ctrl+C`
5. วางด้วย `Ctrl+V` **ภายใน** โฟลเดอร์ที่ Clone จาก GitHub โดยตอบ Replace files in destination
6. อย่าลบโฟลเดอร์ `.git` ภายใน Repository ปลายทาง; ปรับ Explorer → View → Show → Hidden items เพื่อให้มองเห็น `.github`
7. กลับไป GitHub Desktop → Changes ตรวจว่ามี `MainActivity.java`, `OilPriceParser.java`, `PttOilService.java`, `PriceService.java`, `app/build.gradle`, workflow และ tests ใหม่
8. Summary: `Implement Beat Calculator V4.3 fuel price UI and PTT fallback`
9. กด **Commit to main** → **Push origin**
10. ไปที่ `https://github.com/vazabi11/BeatCalculator-Standard/actions` → ดู Workflow `Build Calculator APK` ที่รันหลัง Push
11. เมื่อได้สถานะเขียว ดาวน์โหลด Artifact `BeatCalculator-MultiMarket-v4_3-debug-APK`, แตก ZIP แล้วติดตั้ง `app-debug.apk`

## ตรวจสอบหลังติดตั้ง
1. หน้า **ราคาน้ำมัน** ควรเริ่มแบรนด์ PTT OR และตัวเลือกชนิด `ทั้งหมด`
2. เห็นการ์ดสีเทาเข้ม, ชื่อแบรนด์, ป้ายสถานะ, ตารางราคาเฉพาะเมื่อต้นทาง/แคชมีข้อมูล และปุ่มฟ้า **ตรวจสอบราคาใหม่**
3. ปิดอินเทอร์เน็ต: หากเคยดึงราคาสำเร็จมาก่อน ให้แสดงราคาสำรองพร้อมเวลาและสถานะ (ไม่ใช่ราคาสด)
4. หากไม่เคยมีราคาที่ใช้ได้ ให้แสดง **ยังไม่มีราคาที่ตรวจสอบได้** ห้ามแสดง 0 บาทเป็นราคาน้ำมัน
5. หาก PTT OR ล้มเหลว แตะข้อความสถานะใต้ปุ่มเพื่อดูรายละเอียดทางเทคนิค
6. ตรวจเครื่องคิดเลข Basic/Standard/Advance/Science, แปลงหน่วย, ราคาทองว่าไม่เกิด regression

## การทดสอบที่ทำในสภาพแวดล้อมพัฒนา
`javac` และ `java` ผ่าน 94 assertions: CalculatorEngine 42, Advanced/Unit 25, Market Feed 11, PTT diagnostic 6, Cache Policy 10.

ยังไม่มี Android SDK/Gradle ภายในสภาพแวดล้อมที่ใช้สร้างแพ็กเกจนี้ จึงไม่ได้รับรองว่า APK คอมไพล์ผ่านจนกว่า GitHub Actions จะตรวจสอบจริง.
