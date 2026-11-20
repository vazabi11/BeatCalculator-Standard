Beat Calculator — PTT OR Repair Prototype V4.2

สิ่งที่แก้
1. OilPriceParser.java เพิ่มการตรวจจำนวนรายการ/ฟิลด์ราคาจริง รวม SOAP Fault และ GetOilPriceResult
2. PttOilService.java เรียก CurrentOilPrice; ถ้าไม่มีราคาที่ใช้ได้ จะลอง GetOilPrice ของ 1–3 วันก่อนหน้า
3. ห้ามใช้ข้อมูลย้อนหลังอ้างว่าเป็น 'ราคาสด' ให้ดูวัน PRICE_DATE ที่แสดงในแอปเสมอ
4. ไม่กำหนด/ใส่ราคาเองและไม่ใช้ตัวเลขสมมติ

วิธีติดตั้งในโครงการ V4.1
- แตก ZIP นี้
- นำ app/src/main/java/com/beatoflove/standardcalculator/OilPriceParser.java ไปแทนไฟล์เดิม
- นำ app/src/main/java/com/beatoflove/standardcalculator/PttOilService.java ไปเพิ่ม
- ใน PriceService.java เปลี่ยนเมธอดต่อไปนี้

ของเดิม:
    public static void ptt(Callback<OilPriceParser.Feed> cb) {
        async(() -> OilPriceParser.parse(fetchPttSoap()),cb);
    }

เปลี่ยนเป็น:
    public static void ptt(Callback<OilPriceParser.Feed> cb) {
        async(() -> PttOilService.fetchFeed(),cb);
    }

- ทดสอบ Build ผ่าน GitHub Actions หลัง Commit/Push
- ทดสอบบนโทรศัพท์จริง ขณะออนไลน์และออฟไลน์

หมายเหตุ: ไม่สามารถตรวจคำตอบ SOAP จริงจากโทรศัพท์ในสภาพแวดล้อมนี้ได้ จึงยังไม่อาจรับประกันว่าบริการ PTT OR ให้ราคาจริงในเวลาทดสอบ
โค้ดนี้ยังไม่ถูก Build เป็น APK; ทดสอบเพียง Java parser กับตัวอย่างข้อมูล
