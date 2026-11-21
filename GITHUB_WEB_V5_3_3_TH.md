# SiraCalc V5.3.3 — Currency Reference & Fuel Brands (Testing)
ฐาน: V5.3.2 prepared source; GitHub main ณ เวลาตรวจยังรายงาน 5.3.1. เก็บ Application ID เดิม, versionName 5.3.3, versionCode 12.
- Currency: Convert + Exchange Rates, ยกเลิก Bank Rates และ Compare ใน UI; อัตราอ้างอิง Frankfurter v2 เป็นเงินบาทต่อหนึ่งหน่วยเงินต่างประเทศ, วันที่แต่ละแถว, cache ต่อสกุลเงิน; กด Refresh หรือเปิดหน้า/กลับจาก background เพื่อโหลดใหม่ ไม่ใช่ราคา bank buy/sell หรือ live quote. 23 foreign currency candidates; ไม่รองรับคู่ใดให้แสดง —. ยังไม่ได้ยืนยัน endpoint จริง.
- Fuel: PTT OR, Bangchak, Shell, PT, Caltex, All Brands; เลิก Esso; PTT province/district เดิม, non-PTT Bangkok reference; Shell/PT/Caltex สถานะ unavailable และลิงก์ทางการ/EPPO เท่านั้น ไม่แสดงราคาปลอม. All Brands แสดงข้อจำกัดด้วย. ย้ายค่าที่เคยเลือก Esso เป็น PTT OR.
- Markets / 4 calculators / Converter / Swipe / SiraCalc identity unchanged from prepared V5.3.2 except shared reference service addition. BankRateService legacy code/test remains inactive (avoid deleting old file through GitHub web), not in live diagnostics.

## GitHub Web
1. แตก Calculator-v5.3.3-Source.zip; อัปโหลด *ภายใน* BeatCalculator-Standard-Android (app/, tests/, root Gradle files, README_TH.md, current guide/test report) ไป https://github.com/vazabi11/BeatCalculator-Standard/upload/main
2. คัดลอกทั้งสอง workflow จาก ZIP (.github/workflows/) ไปแทนไฟล์ที่ตรงชื่อใน GitHub Web (มีทั้ง Build และ Diagnostics): https://github.com/vazabi11/BeatCalculator-Standard/edit/main/.github/workflows/build-apk.yml และ https://github.com/vazabi11/BeatCalculator-Standard/edit/main/.github/workflows/diagnose-market.yml
3. ตรวจ app/build.gradle: 12 / 5.3.3; Actions > Build Calculator APK; ไม่ติ๊ก publish_prerelease. ดาวน์โหลด artifact Calculator-v5.3.3 และ APK Calculator-v5.3.3.apk เมื่อผ่านจริง.
4. Diagnostics manual; ตรวจ Frankfurter batch THB board, PTT provincial, biquote, Binance. ข้อจำกัดผู้ให้บริการไม่เท่ากับ build fail. ไม่ได้เพิ่มตัวดึง PT/Caltex/Shell จริงในรุ่นนี้.
5. ทดสอบบนมือถือ Currency: Convert, Exchange Rates, 1 USD/JPY/IDR เป็น THB, date/cache, refresh, สลับ tab ระหว่างโหลด, offline; Fuel: PTT เลือกจังหวัด/อำเภอ, Bangchak Bangkok reference, Shell/PT/Caltex unavailable, All Brands, old Esso preference migration. ทดสอบ 8 tabs และ swipe จากฐาน V5.3.2.
6. หากลายเซ็นไม่ตรง ห้ามถอนแอปเดิมก่อนสำรองข้อมูล. Pre-release เมื่อได้รับอนุญาตเท่านั้น. ไม่รับรอง stable ก่อน acceptance.
