package com.beatoflove.standardcalculator;

import java.util.Arrays;

public final class CalculatorEngineTest {
    private static int tests = 0;
    private static void check(String name, String expected, String... steps) {
        CalculatorEngine e = new CalculatorEngine();
        for (String step : steps) e.press(step);
        if (!expected.equals(e.getDisplay())) throw new AssertionError(name+": expected "+expected+" got "+e.getDisplay());
        tests++;
    }
    private static void ok(boolean value, String message) {
        if (!value) throw new AssertionError(message);
        tests++;
    }
    public static void main(String[] args) {
        check("add", "500", "1","2","5","×","4","=");
        check("divide", "25", "1","0","0","÷","4","=");
        check("percent add", "220", "2","0","0","+","1","0","%","=");
        check("percent mult", "20", "2","0","0","×","1","0","%","=");
        check("subtract", "30", "5","0","−","2","0","=");
        check("sequential", "30", "8","+","2","×","3","=");
        check("negative", "-42", "4","2","±");
        check("square", "81", "9","x²");
        check("root", "9", "8","1","√x");
        check("reciprocal", "0.25", "4","1/x");
        check("division error", "Cannot divide by zero", "1","÷","0","=");
        check("root error", "Invalid input", "4","±","√x");
        check("recover after error", "9", "1","÷","0","=","9");
        check("CE", "13", "5","+","9","CE","8","="); // CE clears 9, then 5 + 8
        check("backspace", "12", "1","2","3","⌫");
        check("decimal", "0.5", "0",".","5");
        check("repeat equals", "8", "2","+","3","=","=");
        check("switch operator", "6", "2","+","×","3","=");
        check("memory recall", "42", "4","2","MS","C","MR");
        check("memory add", "15", "1","0","MS","C","5","M+","C","MR");
        check("memory subtract", "8", "1","0","MS","C","2","M−","C","MR");
        check("memory clear", "0", "8","MS","MC","C","MR");
        check("empty equals", "0", "=");
        check("no double decimal", "1.5", "1",".",".","5");
        check("operator right missing", "10", "5","+","=");
        check("clear", "0", "4","+","5","C");
        check("new after result", "7", "2","+","3","=","7");
        check("repeating single digit", "11", "1","+","1","=","=","=","=","=","=","=","=","=","=");
        check("zero negative", "0", "0","±");
        check("large grouping", "1,234,567", "1","2","3","4","5","6","7");
        CalculatorEngine e = new CalculatorEngine();
        e.press("1");e.press("+");e.press("2");e.press("=");
        ok(e.getHistory().size()==1, "history added");
        ok(e.getHistory().get(0).contains("1 + 2 ="), "history content");
        e.press("9");e.press("MS");
        ok(e.hasMemory(), "memory set");
        e.clearSavedData();
        ok(!e.hasMemory() && e.getHistory().isEmpty() && "0".equals(e.getDisplay()), "clear saved");
        e.restoreMemory("123.5");
        ok("123.5".equals(e.getMemory()), "restore memory");
        e.restoreHistory(Arrays.asList("7 + 2 =\n9"));
        ok(e.getHistory().size()==1, "restore history");
        System.out.println("PASS " + tests + " calculator engine tests");
    }
}
