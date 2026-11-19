# อัปเดต Beat Calculator V4.0 → V4.1 (Offline Price Fallback)

อัปเดตจากซอร์ส V4.0 ที่ได้มาก่อนหน้านี้โดย **ไม่จำเป็นต้องล้างแอปหรือสร้าง Repository ใหม่**

## ไฟล์ที่ต้องแทนที่หรือเพิ่ม

1. `app/src/main/java/com/beatoflove/standardcalculator/MainActivity.java` (แทนที่)
2. `app/src/main/java/com/beatoflove/standardcalculator/PriceCache.java` (**เพิ่มใหม่**)
3. `app/src/main/java/com/beatoflove/standardcalculator/CacheUpdatePolicy.java` (**เพิ่มใหม่**)
4. `app/build.gradle` (แทนที่เพื่อเปลี่ยน versionName เป็น 4.1)
5. `.github/workflows/build-apk.yml` (แทนที่เพื่อเพิ่ม Unit Test)
6. `tests/CacheUpdatePolicyTest.java` (**เพิ่มใหม่**)

ให้ทำผ่าน GitHub Desktop หรือ Android Studio/โปรแกรม Git เพื่อรักษาโครงสร้าง `.github/workflows` และโฟลเดอร์ซ้อนทั้งหมด จากนั้น Commit และ Push ไปที่ `main` ของ Repository `BeatCalculator-Standard` (ถ้าอัปเดต V4.0 ลง Repository นี้แล้ว)

**ถ้า Repository ยังเป็น V2.0:** ใช้ ZIP โปรเจกต์ V4.1 ฉบับเต็มแทนไฟล์โปรเจกต์เดิมทั้งหมด ไม่ใช่ Update Only เพราะรุ่นก่อนขาดโค้ดจาก V3/V4

## สร้างและติดตั้ง

1. เปิด GitHub → Repository → Actions → Build Calculator APK
2. GitHub จะสร้างงานใหม่โดยอัตโนมัติเมื่อ Push ลง main หรือกด Run workflow ด้วยตนเอง
3. ตรวจให้ผ่านทั้งหมด: `CalculatorEngineTest`, `AdvancedFeaturesTest`, `MarketFeedsTest`, `CacheUpdatePolicyTest` และ `Build debug APK`
4. เมื่อผ่าน ดาวน์โหลด Artifact ชื่อ `BeatCalculator-MultiMarket-v4_1-debug-APK` แล้วแตก ZIP เพื่อรับ `app-debug.apk`
5. ติดตั้งบน Android โดยใช้คีย์ลายเซ็นเดียวกับรุ่นก่อน หากคนละคีย์ การติดตั้งทับอาจล้มเหลว อย่าถอนรุ่นเก่าก่อนสำรองข้อมูลสำคัญ

## ทดสอบ Offline บน S24 Ultra

1. เปิดอินเทอร์เน็ต เลือกแท็บราคาน้ำมัน แล้วกดอัปเดตจนได้ราคา PTT OR/บางจากและเวลาประกาศ
2. เลือกแท็บทองไทยและโลหะ Spot แต่ละชนิดที่ต้องการให้มีข้อมูลสำรอง ต้องดึงได้สำเร็จแต่ละชนิดก่อน
3. เปิด Airplane mode แล้วกลับเข้าแท็บราคา ควรเห็นราคาล่าสุดที่บันทึกพร้อมข้อความ `ราคาล่าสุดที่บันทึกไว้ (ไม่ใช่ราคาสด)` และวันเวลาบันทึก
4. ปิดแอป เปิดใหม่ขณะไม่มีอินเทอร์เน็ต ราคาสำรองต้องยังแสดง
5. เปิดอินเทอร์เน็ตอีกครั้งและกดอัปเดต หากบริการส่งค่าที่ใหม่กว่า แอปต้องแทนที่แคช
6. หากไม่มีข้อมูลสำรองตั้งแต่แรก ให้แสดง `ยังไม่มีราคาที่บันทึกไว้ในเครื่อง` ไม่แสดง 0 บาทหรือค่าที่สมมติเอง

### ข้อกำหนดข้อมูล

ราคาสำรองไม่หมดอายุโดยการลบอัตโนมัติ แต่จะไม่ถูกนำเสนอว่าเป็นราคาปัจจุบันจนกว่าจะได้รับการยืนยันจากต้นทาง ราคาของ Shell จะไม่แสดงอัตโนมัติจนกว่าจะได้สิทธิ์เชื่อมต่อแหล่งข้อมูลอย่างเหมาะสม
