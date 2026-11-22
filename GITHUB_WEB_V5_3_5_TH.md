# SiraCalc V5.3.5 — Termux + GitHub CLI
ใช้มาตรฐาน Hybrid Policy V1.4 / MAS-STD-005-TMX / MAS-STD-009-ID V1.2

1. ดาวน์โหลด Calculator-v5.3.5-Source.zip และ upload_siracalc_v5.3.5.sh ไว้ใน Download เดียวกัน ไม่ต้องแตก ZIP.
2. ถ้ายังไม่ตั้งค่า Termux: pkg install git gh unzip coreutils python -y; termux-setup-storage; gh auth status; gh auth setup-git. ใช้บัญชีที่มีสิทธิ์ repo และ git user.name/email ที่ตั้งไว้ก่อนหน้า; ไม่ส่ง token ใน chat.
3. รัน `bash ~/storage/downloads/upload_siracalc_v5.3.5.sh` จาก Termux. ตรวจ repo, HEAD, checksum และ staged diff; พิมพ์ YES เมื่ออนุมัติ commit/push/PR.
4. Script สร้าง development branch ใหม่และ PR ไป main; ไม่ merge/publish อัตโนมัติ. ตรวจ PR CI ที่ SHA ตรงกันก่อนอนุมัติ merge; ตรวจ main build อีกครั้ง.
5. ดาวน์โหลด artifact Calculator-v5.3.5 (ภายใน Calculator-v5.3.5.apk). ตรวจ package/version/code, checksum/cert continuity ก่อนติดตั้งทับ. Debug APK อาจมีลายเซ็นต่างกัน; ไม่ถอนรุ่นเดิมโดยไม่สำรองข้อมูล.
6. Stable หรือ public pre-release ต้องได้รับอนุมัติแยกต่างหาก. ทดสอบจริงและเก็บ QA ลง Drive.

หาก HEAD ไม่ตรง ให้หยุดและสร้างแพ็กเกจใหม่ ไม่ force push. Script overlay เฉพาะ allowlist; ไม่ลบไฟล์เก่าใน GitHub. Full source มี app/, tests/, root Gradle, workflows ทั้งสอง. ไฟล์ภาพ/คู่มือประวัติที่ไม่ได้อยู่ใน source archive เดิมและไม่จำเป็นต่อ build ไม่ได้รวมซ้ำ แต่ยังอยู่บน GitHub. ถ้า GitHub OAuth ปฏิเสธสิทธิ์ workflow ให้ตรวจสิทธิ์หรือใช้เว็บตามมาตรฐานโดยไม่ปิด tests.
