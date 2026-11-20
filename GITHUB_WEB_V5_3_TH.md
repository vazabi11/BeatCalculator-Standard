# Calculator by Sirawit Labs — V5.3.0
Developer: Sirawit Dh. · Unified Calculator & Live Markets Edition

## Source package
- applicationId เดิม com.beatoflove.standardcalculator; versionCode 9; versionName 5.3.0.
- Navigation: Standard (default), Scientific, Formula Pro, Financial, Currency, Fuel, Markets, Converter. Basic ไม่แสดงใน navigation; Advance ไม่มีตั้งแต่ V5.2.
- ผังปุ่มและ engine Standard / Scientific และ FormulaPanel จาก V5.2 คงเดิม; เปลี่ยน palette ตาม Unified Dark Cyan. ไอคอน Adaptive Vector แบบเครื่องหมายสี่ตัว Cyan (ไม่ใช่การฝังภาพ PNG จากต้นแบบ).
- Financial: Accounting keypad, configurable tax, GT/tape, COST/SELL/MARGIN/MU, rounding; Business, Finance, Investment forms. VAT อยู่ใน Financial (Converter แสดงเฉพาะหน่วย).
- Financial calculations: BigDecimal สำหรับเงินและการปัดเศษ; ดอกเบี้ยทบต้น/TVM/IRR เป็นต้นใช้ numerical double. ค่างวดเป็นแบบลดต้นลดดอก จ่ายปลายงวด; ไม่รวมค่าธรรมเนียม/ประกัน. IRR รองรับกระแสเงินสดเปลี่ยนเครื่องหมายครั้งเดียว และอัตราที่หาได้ในช่วงจำกัด. TVM ใช้ sign convention (จ่ายลบ รับบวก); กรอก 0 ในช่องค่าที่ต้องการหา. Tax rate ค่าเริ่มต้น 7% เป็นค่าที่ปรับได้ ไม่ใช่คำรับรองภาษีปัจจุบัน. Tape/GT อยู่ใน session.
- Currency: Frankfurter daily reference ไม่มี key; เลือก From/To, amount, swap, keypad; cache คู่เงินพร้อมวันที่; เลือก Forex quote ล่าสุดที่เคยรับจาก Markets ได้เฉพาะคู่ที่มี cache. Bank Rates/Compare: กรอกอัตราที่ตรวจจากเว็บไซต์ธนาคาร, ทิศทาง, ประเภทเงินสด/โอน, ค่าธรรมเนียม; บันทึกแยกธนาคาร และมีลิงก์ต้นทาง. ยังไม่มี automatic bank-specific feed และ BOT API ไม่ได้เชื่อม (มีลิงก์อ้างอิง).
- Fuel: 77 จังหวัด + Bangkok reference; ขอบเขตอำเภอหรือกรอกชื่ออำเภอเอง; จำจังหวัดและชื่ออำเภอ. ราคาอัตโนมัติเดิม PTT OR/Bangchak เป็นราคากลาง ไม่ใช้แทนจังหวัด. Regional API ยังไม่ยืนยัน จึงแสดง unavailable และลิงก์ OR. ไม่มีอัตรารายสถานีหรือทุกแบรนด์.
- Markets: Thai Gold (เดิม), Metals (stream หรือ reference), Crypto (Binance ticker USDT), Forex (Twelve Data). Streaming เริ่มเมื่อเปิดหมวด หยุดเมื่อปิดหน้าจอ/ออกจาก Tab, retry แบบ backoff, แสดง timestamp และ cache, highlight เมื่อราคาขยับ. Thai Gold ตรวจทุก 1 นาที, metals reference ทุก 5 นาที, Fuel ทุก 15 นาที. ราคารับจากผู้ให้บริการจริงเท่านั้น.
- Streaming Metals/Forex: เปิด Markets > Metals/Forex > Set personal market feed key, ใช้ Twelve Data key ของตนเองที่มีสิทธิ์ symbol/stream. ไม่ฝังหรือบันทึก key ลง APK; เก็บใน RAM session เท่านั้น. สำหรับเผยแพร่แอปควรใช้ backend ที่รักษา key และสิทธิ์การกระจายข้อมูล. ผู้ให้บริการกำหนดความถี่จริงและสิทธิ์ตามแพ็กเกจ; ไม่รับรอง tick-by-tick. Crypto ต้องเข้าถึง endpoint Binance ได้จากเครือข่ายผู้ใช้. USDT ไม่ใช่ USD.
- ไม่รวมกราฟย้อนหลัง/Favorites/price alerts/portfolio ในรุ่นนี้; ไม่มีการแสดงราคาสมมติ.

## GitHub Web — อัปเดต
1. แตก ZIP เปิดโฟลเดอร์ BeatCalculator-Standard-Android ภายใน ไม่อัปโหลดโฟลเดอร์ซ้อนทั้งก้อน.
2. เปิด https://github.com/vazabi11/BeatCalculator-Standard/upload/main แล้วอัปโหลด app/, tests/, build.gradle, settings.gradle, gradle.properties, README_TH.md, GITHUB_WEB_V5_3_TH.md, TEST_RESULTS_V5_3.txt จากภายในโฟลเดอร์. Commit: `Upgrade Sirawit Labs Calculator to 5.3.0`.
3. แก้ `.github/workflows/build-apk.yml` ผ่าน https://github.com/vazabi11/BeatCalculator-Standard/edit/main/.github/workflows/build-apk.yml โดยใช้ไฟล์ workflow ใน ZIP หรือไฟล์แยกที่ส่งให้. ทำเช่นเดียวกับ `.github/workflows/diagnose-market.yml`: https://github.com/vazabi11/BeatCalculator-Standard/edit/main/.github/workflows/diagnose-market.yml . Commit แต่ละไฟล์.
4. ตรวจ app/build.gradle versionCode 9, versionName '5.3.0'. ไป Actions: https://github.com/vazabi11/BeatCalculator-Standard/actions > Build Calculator APK > Run workflow > main. เลือก run หลัง commit ทั้งหมดแล้ว. ดาวน์โหลด artifact `SirawitLabs-Calculator-v5.3.0-debug-APK` เฉพาะเมื่อ build สำเร็จ.
5. แตก artifact ติดตั้ง app-debug.apk. หากลายเซ็น APK ไม่ตรง Android จะไม่ให้ติดตั้งทับ; อย่าถอนแอปเก่าก่อนสำรองข้อมูล เพราะข้อมูลประวัติ/แคชอาจหาย. ตรวจ header version v5.3.0 และ tab order.
6. Run workflow Diagnose Live Market APIs (manual) แยกจาก Build. Probe Frankfurter/Binance ใช้การเชื่อมต่อจริงจาก runner; หาก fail ให้ดู log. ผลผ่านของ runner ไม่ยืนยันว่าโทรศัพท์ของผู้ใช้เข้าถึง endpoint ได้. Twelve Data ต้องทดสอบด้วย key/plan ของผู้ใช้บนเครื่องจริง.

## Acceptance checks
- Standard default; 8 tabs; ตรวจ layout/key count unchanged จาก V5.2; Formula Pro SHIFT/ALPHA, COMP/SD/REG.
- Accounting TAX+ 100 ที่ 7% = 107, TAX- 107 = 100; Margin cost100/sell125 =20%; Markup=25%; test zero interest loan, NPV, IRR.
- Currency daily: USD → THB, ตรวจ source/date; swap; airplane mode ใช้คู่เงินที่มี cache; bank manual THB per 1 foreign unit (JPY ถ้าแหล่ง quote ต่อ 100 ให้หาร 100 ก่อนกรอก); ไม่ใช้ daily reference เป็น bank buy/sell.
- Fuel: Bangkok reference vs province unknown; จังหวัดอื่นไม่มีราคาเมื่อไม่พบ feed; ไม่แสดงราคากรุงเทพฯ ในนามจังหวัด.
- Markets: Crypto: ticker + timestamp, airplane mode cached, resume; Forex/Metals no key = unavailable/cached; with own authorized key test symbols and source cadence. ตรวจ USDT ไม่แสดงเป็น USD. Thai Gold/Metal REST ตรวจเวลาและชื่อแหล่งเดิม.
- Android build และการทดสอบหน้าจอ/การเชื่อมต่อจริงยังไม่ได้ทำใน environment นี้ (ไม่มี SDK, DNS network lookup blocked). Standalone 269 tests ผ่าน และ Java syntax parse ผ่าน; ไม่ใช่หลักฐานว่า Android build หรือ API จริงผ่าน.

## Provider docs
- https://frankfurter.dev/javascript/
- https://twelvedata.com/docs/websocket/websocket-getting-started
- https://support.twelvedata.com/en/articles/12520817-forex-api-v2
- https://developers.binance.info/en/docs/catalog/core-trading-spot-trading/api/ws-streams/~
- https://www.pttor.com/news/oil-price
- https://portal.api.bot.or.th/portal/catalogue-products/exchange-rates-1
