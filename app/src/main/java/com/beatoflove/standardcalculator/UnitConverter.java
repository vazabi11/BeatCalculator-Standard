package com.beatoflove.standardcalculator;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.LinkedHashMap;
import java.util.Map;

/** Offline unit conversion: exact metric definitions where available. */
public final class UnitConverter {
    private static final MathContext MC = new MathContext(20, RoundingMode.HALF_UP);
    private static final Map<String, LinkedHashMap<String, BigDecimal>> FACTORS = new LinkedHashMap<>();
    private static final String AREA="พื้นที่", LENGTH="ความยาว", TEMP="อุณหภูมิ", VOLUME="ปริมาตร", MASS="น้ำหนัก", SPEED="ความเร็ว", TIME="เวลา", TAX="ภาษี";
    static {
        add(AREA, "ตารางเมตร (m²)", "1", "ตารางกิโลเมตร (km²)", "1000000", "ไร่", "1600", "งาน", "400", "ตารางวา", "4", "เฮกตาร์ (ha)", "10000", "เอเคอร์ (acre)", "4046.8564224", "ตารางฟุต (ft²)", "0.09290304");
        add(LENGTH, "เมตร (m)", "1", "กิโลเมตร (km)", "1000", "เซนติเมตร (cm)", "0.01", "มิลลิเมตร (mm)", "0.001", "นิ้ว (in)", "0.0254", "ฟุต (ft)", "0.3048", "หลา (yd)", "0.9144", "ไมล์ (mi)", "1609.344");
        add(VOLUME, "ลิตร (L)", "1", "มิลลิลิตร (mL)", "0.001", "ลูกบาศก์เมตร (m³)", "1000", "แกลลอนสหรัฐ (US gal)", "3.785411784", "แกลลอนอังกฤษ (UK gal)", "4.54609", "ถ้วยสหรัฐ (cup)", "0.2365882365");
        add(MASS, "กิโลกรัม (kg)", "1", "กรัม (g)", "0.001", "มิลลิกรัม (mg)", "0.000001", "ตันเมตริก (t)", "1000", "ปอนด์ (lb)", "0.45359237", "ออนซ์ (oz)", "0.028349523125");
        add(SPEED, "กิโลเมตร/ชม. (km/h)", "1", "เมตร/วินาที (m/s)", "3.6", "ไมล์/ชม. (mph)", "1.609344", "นอต (kn)", "1.852");
        add(TIME, "วินาที (s)", "1", "นาที (min)", "60", "ชั่วโมง (h)", "3600", "วัน (d)", "86400", "สัปดาห์ (wk)", "604800");
        add(TEMP, "เซลเซียส (°C)", "1", "ฟาเรนไฮต์ (°F)", "1", "เคลวิน (K)", "1");
        add(TAX, "ก่อนภาษี → รวมภาษี", "1", "รวมภาษี → ก่อนภาษี", "1");
    }
    private static void add(String category, String... labelsAndFactors) {
        LinkedHashMap<String, BigDecimal> values=new LinkedHashMap<>();
        for(int i=0;i<labelsAndFactors.length;i+=2) values.put(labelsAndFactors[i],new BigDecimal(labelsAndFactors[i+1]));
        FACTORS.put(category, values);
    }
    public static String[] categories() { return FACTORS.keySet().toArray(new String[0]); }
    public static String[] units(String category) {
        Map<String,BigDecimal> map=FACTORS.get(category);
        return map == null ? new String[0] : map.keySet().toArray(new String[0]);
    }
    public static BigDecimal convert(String category, String from, String to, String raw) {
        if (TAX.equals(category)) throw new IllegalArgumentException("ใช้ calculateTax สำหรับภาษี");
        BigDecimal value = read(raw);
        if (TEMP.equals(category)) {
            BigDecimal c;
            if (from.startsWith("เซลเซียส")) c=value;
            else if (from.startsWith("ฟาเรนไฮต์")) c=value.subtract(new BigDecimal("32"),MC).multiply(new BigDecimal("5"),MC).divide(new BigDecimal("9"),MC);
            else if (from.startsWith("เคลวิน")) c=value.subtract(new BigDecimal("273.15"),MC);
            else throw new IllegalArgumentException("หน่วยอุณหภูมิไม่ถูกต้อง");
            if (to.startsWith("เซลเซียส")) return c;
            if (to.startsWith("ฟาเรนไฮต์")) return c.multiply(new BigDecimal("9"),MC).divide(new BigDecimal("5"),MC).add(new BigDecimal("32"),MC);
            if (to.startsWith("เคลวิน")) return c.add(new BigDecimal("273.15"),MC);
            throw new IllegalArgumentException("หน่วยอุณหภูมิไม่ถูกต้อง");
        }
        Map<String,BigDecimal> values=FACTORS.get(category);
        if(values==null||!values.containsKey(from)||!values.containsKey(to)) throw new IllegalArgumentException("หน่วยไม่ถูกต้อง");
        return value.multiply(values.get(from),MC).divide(values.get(to),MC);
    }
    public static BigDecimal calculateTax(String raw, String taxRate, boolean priceIncludesTax) {
        BigDecimal value=read(raw), rate=read(taxRate);
        if(rate.signum()<0||rate.compareTo(new BigDecimal("100"))>0) throw new IllegalArgumentException("อัตราภาษีต้องอยู่ระหว่าง 0–100%");
        BigDecimal divisor=BigDecimal.ONE.add(rate.divide(new BigDecimal("100"),MC));
        return priceIncludesTax ? value.divide(divisor,MC) : value.multiply(divisor,MC);
    }
    public static BigDecimal read(String raw) {
        try {
            if(raw==null||raw.trim().isEmpty()) return BigDecimal.ZERO;
            return new BigDecimal(raw.trim().replace(",", ""));
        } catch(NumberFormatException e) { throw new IllegalArgumentException("กรุณากรอกตัวเลขที่ถูกต้อง"); }
    }
    public static String display(BigDecimal n) {
        // UI precision is separate from calculation precision.
        BigDecimal x=n.setScale(Math.min(Math.max(n.scale(),0),10), RoundingMode.HALF_UP).stripTrailingZeros();
        return x.toPlainString();
    }
}
