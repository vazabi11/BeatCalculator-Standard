# Calculator by Sirawit Labs 5.3.1 — Quality & Stability Update

## อัปเดตผ่าน GitHub Web
1. สำรองข้อมูลและ APK เดิมก่อน ไม่ต้องถอนการติดตั้งแอปเดิม
2. แตก ZIP และเปิดโฟลเดอร์ BeatCalculator-Standard-Android ภายใน ZIP
3. เปิด https://github.com/vazabi11/BeatCalculator-Standard/upload/main อัปโหลด app/, tests/, build.gradle, settings.gradle, gradle.properties, README_TH.md, GITHUB_WEB_V5_3_1_TH.md และ TEST_RESULTS_V5_3_1.txt โดยรักษาเส้นทางเดิม ไม่อัปโหลดโฟลเดอร์รากซ้อนอีกชั้น
4. Commit changes: Upgrade Calculator to 5.3.1 Quality and Stability
5. เปิด https://github.com/vazabi11/BeatCalculator-Standard/edit/main/.github/workflows/build-apk.yml และแทนที่ด้วย build-apk.yml ฉบับใหม่ทั้งหมด แล้ว Commit
6. เปิด https://github.com/vazabi11/BeatCalculator-Standard/edit/main/.github/workflows/diagnose-market.yml และแทนที่ด้วย diagnose-market.yml ฉบับใหม่ทั้งหมด แล้ว Commit
7. ตรวจ app/build.gradle: versionCode 10, versionName '5.3.1', applicationId เดิม
8. เปิด https://github.com/vazabi11/BeatCalculator-Standard/actions เลือก Build Calculator APK > Run workflow > main. เมื่อสำเร็จ ดาวน์โหลด Artifact Calculator-v5.3.1 (ZIP) แตกไฟล์แล้วติดตั้ง Calculator-v5.3.1.apk. หากลายเซ็นต่างกัน อย่าถอนแอปก่อนสำรองข้อมูล
9. รัน Diagnose Live Market APIs (manual) > Run workflow > main. ตรวจรายงาน v5.3.1-live-integration-report และ logs ของแต่ละ endpoint; workflow อาจแดงเมื่อแหล่งใดไม่ส่งข้อมูลที่ตรวจสอบได้ ไม่ถือว่าเป็น APK build failure โดยอัตโนมัติ

## ตรวจสอบหลังติดตั้ง
- Standard เปิดเป็นค่าเริ่มต้น; ผังปุ่มเดิมและ Formula Pro ยังอยู่ครบตามซอร์สฐาน
- Fuel: เลือก PTT OR, สุราษฎร์ธานี และชลบุรี สลับกลับไปมา ตรวจจังหวัด/พื้นที่ วันที่และแหล่งข้อมูล; ราคาย้อนหลังต้องแสดงว่าเป็นข้อมูลย้อนหลัง ไม่ใช้ราคา Bangkok Reference แทน
- Currency: Bank Rates เลือกธนาคารและประเภทราคา; Compare เลือกสกุลเงิน ทิศทาง และชนิดธนบัตร ไม่ต้องกรอกราคาเอง; จำนวนเงินใช้คำนวณยอดก่อนค่าธรรมเนียม หากมี
- Markets: ตรวจ Crypto เดิม, Forex และ Metals โดยไม่ใส่ Key; ตรวจแหล่งข้อมูล เวลาราคา สถานะเมื่อปิดเครือข่าย แล้วเปิดใหม่
- Financial: ทดสอบจำ TAX rate, GT, rounding และ tape หลังปิดแอป
- ทดสอบ Standard, Scientific, Formula Pro, Converter และการหมุน/เปลี่ยนหน้าจอบนมือถือ

## ข้อจำกัดและแหล่งข้อมูล
- OR: https://orapiweb.pttor.com/oilservice/OilPrice.asmx ใช้ CurrentOilPriceProvincial / GetOilPriceProvincial. รายการจังหวัดภาษาอังกฤษ/ไทยยังต้องยืนยันด้วย API จริง; ไม่มีข้อมูลอำเภอที่รับรอง; Bangchak รายจังหวัดยังไม่รองรับ
- Forex/Metals: https://biquote.io/ ใช้ข้อมูล indicative MT5 feed, SignalR WebSocket และ REST fallback ทุก 30 วินาที; ยังไม่ยืนยัน live handshake, schema, availability หรือสิทธิ์เผยแพร่; Frankfurter https://frankfurter.dev/ เป็นอัตราอ้างอิงรายวันเท่านั้น; Twelve Data เป็นทางเลือกตามสิทธิ์ API Key; Crypto คง Binance เดิม
- Bank Rates: ตัวอ่านตารางสาธารณะจาก KBank, ttb, Bangkok Bank และ SCB; สองธนาคารหลังอาจต้องใช้ข้อมูลแบบ dynamic; Krungthai/Krungsri ยังไม่มีตัวอ่านที่ยืนยัน; แสดง unavailable หากไม่มีข้อมูลที่ตรวจสอบได้ ห้ามแทนด้วยอัตราตลาดหรือกรอกอัตราจำลอง. ต้องตรวจเงื่อนไขนำข้อมูลไปเผยแพร่ก่อนใช้เชิงพาณิชย์
- ชุด standalone 275 assertions ผ่านในเครื่องพัฒนา. ตรวจ Java syntax 24 ไฟล์; ไม่ใช่ Android compilation. JUnit BankBoardTest และ LiveConnectivityTest ยังต้องรันผ่าน GitHub Actions; ยังไม่ได้ยืนยัน API จริงหรือทดสอบ APK บนมือถือ
- ยังไม่ได้เพิ่มกราฟย้อนหลัง/Favorites หรือยืนยันความครบ 240 ฟังก์ชันของเครื่องคิดเลขต้นแบบ

## มาตรฐานชื่อไฟล์อัตโนมัติ
- Source ZIP: Calculator-v5.3.1-Source.zip
- Artifact: Calculator-v{versionName}; APK: Calculator-v{versionName}.apk
- Workflow ตรวจ versionName, versionCode และ Application ID จริงจาก aapt dump badging เทียบ app/build.gradle หลัง Build; หากไม่ตรงหรือไม่ใช่ Debug APK จะไม่อัปโหลด
- GitHub Actions Summary แสดงเวอร์ชัน, Build type (Debug), commit และ SHA-256; ชื่อไฟล์ไม่ใช่การเปลี่ยนลายเซ็นหรือ Release Build
- ทดสอบ Workflow ด้วย mock aapt 7 กรณีผ่านทั้งหมด ยังไม่ได้รัน Build Android จริง
