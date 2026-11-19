package com.beatoflove.standardcalculator;
import java.math.BigDecimal;

public final class AdvancedFeaturesTest {
    private static int tests=0;
    private static void exact(String name, String expected, String... keys){
        CalculatorEngine engine=new CalculatorEngine();for(String k:keys)engine.press(k);
        if(!expected.equals(engine.getDisplay()))throw new AssertionError(name+": "+engine.getDisplay()+" vs "+expected);tests++;
    }
    private static void near(String name,String actual,double expect,double tol){
        if(Math.abs(new BigDecimal(actual.replace(",", "")).doubleValue()-expect)>tol)throw new AssertionError(name+" got "+actual);tests++;
    }
    private static void conv(String category,int a,int b,String input,double expected){
        String[] units=UnitConverter.units(category);
        double n=UnitConverter.convert(category,units[a],units[b],input).doubleValue();
        if(Math.abs(n-expected)>1e-8)throw new AssertionError(category+": "+n+" expected "+expected);tests++;
    }
    public static void main(String[] args){
        exact("factorial 5", "120", "5","n!");
        exact("factorial error", "Factorial: 0–70 only", "5","±","n!");
        exact("sin 90 DEG", "1", "9","0","sin");
        exact("log 100", "2", "1","0","0","log");
        exact("ln 1", "0", "1","ln");
        exact("10^2", "100", "2","10^x");
        exact("modulus", "1", "1","0","mod","3","=");
        exact("pow", "256", "2","^","8","=");
        exact("abs", "7", "7","±","abs");
        exact("cubic", "27", "3","cube");
        exact("tan 0", "0", "0","tan");
        CalculatorEngine rad=new CalculatorEngine();rad.setDegreeMode(false);rad.press("π");rad.press("÷");rad.press("2");rad.press("=");rad.press("sin");
        near("sin PI/2 RAD",rad.getDisplay(),1,1e-10);
        conv("พื้นที่",0,2,"3200",2); // m2 -> rai
        conv("พื้นที่",2,0,"1",1600);
        conv("ความยาว",0,1,"1500",1.5);
        conv("ความยาว",4,0,"10",0.254);
        conv("อุณหภูมิ",0,1,"100",212);
        conv("อุณหภูมิ",1,0,"32",0);
        conv("อุณหภูมิ",0,2,"0",273.15);
        conv("ปริมาตร",0,1,"2",2000);
        conv("น้ำหนัก",0,1,"1",1000);
        conv("ความเร็ว",1,0,"10",36);
        conv("เวลา",2,1,"2",120);
        if(UnitConverter.calculateTax("100","7",false).compareTo(new BigDecimal("107"))!=0)throw new AssertionError("VAT add");tests++;
        if(UnitConverter.calculateTax("107","7",true).compareTo(new BigDecimal("100"))!=0)throw new AssertionError("VAT remove");tests++;
        System.out.println("PASS "+tests+" advanced/unit tests");
    }
}
