# Calculator by Sirawit Labs — V5.2.0 / Formula Pro

## สิ่งที่เปลี่ยน
- ลำดับแท็บ: Basic, Standard, Science, Formula Pro, แปลงหน่วย, ราคาน้ำมัน, ราคาโลหะ
- เอา Advance ออก; ฟังก์ชันเดิม เช่น mod, power, cube, abs อยู่ใน Formula Pro (Science ยังอยู่)
- ตัวประมวลผลสมการแยกจากเครื่องคิดเลข Standard; COMP / SD / REG ทำงานออฟไลน์
- เอกลักษณ์ Modern Engineering: navy / cyan, SHIFT เหลือง, ALPHA ชมพู, หน้าสถิติและ Regression แยกจากแป้นคำนวณ
- Version 5.2.0 / Version code 8; Application ID เดิม com.beatoflove.standardcalculator
- ระบบราคาและไอคอนคงจาก V5.1.0 ไม่ได้รับการแก้การเข้าถึง API เพิ่มเติมในรุ่นนี้

## อ้างอิงความครอบคลุม
คู่มือเครื่องรุ่น fx-350MS 2nd Edition: https://support.casio.com/global/en/calc/manual/fx-82MS_85MS_220PLUS_300MS_350MS_en/

COMP รองรับวงเล็บ ลำดับคำนวณ เลขยกกำลัง ราก ทริโกโนเมตรี/inverse/hyperbolic, factorial/nCr/nPr, ตัวแปร 9 ตัว, Ans, STO/RCL/M+, M−, การสุ่ม, เปอร์เซ็นต์, Pol/Rec, DMS, การปัดเศษ, การแปลงหน่วยมุม, Fix/Sci/Norm 1/Norm 2/ENG, เศษส่วนและจำนวนคละ, สมการหลายชุดด้วย :, ประวัติเรียกกลับและแก้ไขนิพจน์

ตัวอย่าง: 2+3×4 =14; (2+3)×4=20; root(3,-8)=-2; logbase(2,8)=3; mixed(2,1,3)=2⅓; dms(30,15,0)=30.25; pol(3,4) ได้ r=5 (X=r, Y=มุม); rec(5,30) เก็บพิกัดใน X,Y; pctadd(200,10)=220; pctrate(20,80)=25; 2+3:Ans×4 แสดง 5 และ 20

- SHIFT สลับฟังก์ชันรองหนึ่งครั้ง; ALPHA เข้าตัวแปรและ Memory; ƒ(x) รวมฟังก์ชันเพิ่มเติมและค่าสถิติที่เพิ่งคำนวณ
- ลูกศร/การแตะในสมการใช้แก้ไขตำแหน่ง; History เรียกสมการเดิมกลับมาได้; Setup ตั้งรูปแบบตัวเลข
- SD: ป้อน X หรือ X,frequency หนึ่งแถวต่อบรรทัด เช่น 10\n20\n30 หรือ 10,3; แก้ไข/ลบแถวจากกล่องข้อมูล; คำนวณ n, Σx, Σx², mean, population SD, sample SD
- REG: ป้อน X,Y หรือ X,Y,frequency; Linear, Logarithmic, Exponential, Power, Inverse, Quadratic; A/B/C, r (ยกเว้น quadratic), r², ผลรวม x/y, moment ถึง x⁴, SD, covariance, ประมาณ X/Y (quadratic ให้ทั้งสองรากเมื่อมี)
- ค่าสถิติสามารถใช้ต่อใน COMP ผ่าน ƒ(x) ในเซสชันปัจจุบัน; ข้อมูลที่ป้อน ประวัติ และตัวแปรถูกบันทึกในเครื่อง

## ขอบเขตและความแม่นยำ
การคำนวณ Formula Pro ใช้ double; การแสดง Norm ปัดเป็นเลขนัยสำคัญตามการออกแบบของแอป (ไม่รับประกัน bit-for-bit กับเครื่องต้นแบบ), การแสดงเศษส่วนเป็นการประมาณจากเลขทศนิยม ไม่ใช่ symbolic rational engine; syntax ใช้ชื่อฟังก์ชันเช่น mixed(), root(), dms(), pctadd() และอาจแตกต่างจากการกดปุ่มเครื่องจริง; ไม่มีข้ออ้างว่าครบ 240 ฟังก์ชันหรือการทดสอบทุกกรณีในคู่มือ; ยังไม่ทดสอบ UI ด้วย Android SDK หรือโทรศัพท์จริง. ชุดทดสอบ Java standalone ผ่าน 239 assertions (135 ใหม่ + 104 เดิม); JUnit ตัวอ่านทองและ Android compilation ต้องรันใน GitHub. การเข้าถึง PTT OR/ทองไทยยังต้องตรวจแยกผ่าน Diagnostics และมือถือ.

## อัปเดตผ่าน GitHub Web (ไม่ใช้ Desktop)
1. ดาวน์โหลด ZIP เต็ม V5.2.0 และ Extract All. เปิดโฟลเดอร์ BeatCalculator-Standard-Android ที่มี app/, tests/, build.gradle, settings.gradle, gradle.properties
2. ไปที่ https://github.com/vazabi11/BeatCalculator-Standard/upload/main
3. ลาก app/, tests/, build.gradle, settings.gradle, gradle.properties, GITHUB_WEB_V5_2_TH.md เข้าเว็บทีเดียว (อย่าลากโฟลเดอร์ครอบ BeatCalculator-Standard-Android หรือไฟล์ ZIP ขึ้นทั้งก้อน). ตรวจเส้นทางในรายการอัปโหลดว่าตรงกับไฟล์เดิม. Commit message: Upgrade Sirawit Labs Calculator to 5.2.0 Formula Pro. Commit ลง main.
4. เปิด https://github.com/vazabi11/BeatCalculator-Standard/edit/main/.github/workflows/build-apk.yml แล้วแทนที่ทั้งหมดด้วยไฟล์ SirawitLabs-v5.2-build-apk.yml ที่ให้แยก หรือ .github/workflows/build-apk.yml ใน ZIP. หากยังไม่มี สร้างใหม่ที่ Code → Add file → Create new file แล้วใส่เส้นทาง .github/workflows/build-apk.yml. Commit ลง main. Workflow diagnose-market.yml ไม่เปลี่ยนในรุ่นนี้ ให้เก็บไว้หรือใช้ไฟล์ใน ZIP หากยังไม่มี.
5. ตรวจ app/build.gradle: versionCode 8, versionName '5.2.0'. ตรวจ MainActivity.java มี Formula Pro, ไม่มีแท็บ Advance และมี FormulaEngine.java, FormulaPanel.java, FormulaStats.java พร้อม tests/FormulaProTest.java
6. เปิด https://github.com/vazabi11/BeatCalculator-Standard/actions เลือก Build Calculator APK. Commit อาจสร้าง Build ใหม่อัตโนมัติ; ใช้ Run workflow บน main หลังอัปโหลดครบหากต้องการ. เลือก run ที่เป็น commit ล่าสุดที่มี Workflow ใหม่และรอ Success. ตรวจ log มี PASS 135 Formula Pro assertions และ Build successful
7. ดาวน์โหลด Artifact SirawitLabs-Calculator-v5.2.0-debug-APK แตก ZIP แล้วติดตั้ง app-debug.apk บนมือถือ. ไม่ควรติดตั้ง APK รุ่นก่อนโดยผิดไฟล์. ถ้าลายเซ็นต่างกัน จะติดตั้งทับไม่ได้; การถอนแอปลบ Memory/History/Cache ให้ตัดสินใจก่อนถอน. Version บนแถบด้านบนต้องแสดง v5.2.0 (อ่านจาก Android Package)
8. ทดสอบ Basic/Standard/Science และ Formula Pro. COMP: 2+3×4=14, SHIFT sin⁻¹(0.5)=30 ใน DEG, ncr(5,2)=10; SD 10,20,30,40,50 แยกบรรทัด: mean=30, σ=14.142135..., s=15.811388...; REG Linear: (1,3),(2,5),(3,7) แยกบรรทัด → A=1,B=2,r=1. ทดสอบแอร์เพลนโหมด. ตรวจหน้าราคาและ Offline Cache เดิมด้วย

หาก Build ล้มเหลว โปรดส่ง URL ของ Actions Run ให้ตรวจ Log โดยตรง. หน้าจอและ APK ยังไม่ได้ผ่านการทดสอบจริงจากผู้จัดทำ ZIP.
