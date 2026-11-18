package com.beatoflove.standardcalculator;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

/** Pure-Java, offline, deterministic standard calculator state machine. */
public final class CalculatorEngine {
    private static final MathContext MC = new MathContext(16, RoundingMode.HALF_UP);
    private static final int DIGIT_LIMIT = 16;
    private static final int HISTORY_LIMIT = 50;

    private String entry = "0";
    private BigDecimal accumulator;
    private String pendingOp;
    private BigDecimal lastOperand;
    private String lastOp;
    private boolean startNewEntry;
    private boolean afterEquals;
    private boolean error;
    private String expression = "";
    private BigDecimal memory;
    private final List<String> history = new ArrayList<>();

    public void press(String key) {
        if (key == null) return;
        if (key.length() == 1 && key.charAt(0) >= '0' && key.charAt(0) <= '9') {
            digit(key); return;
        }
        switch (key) {
            case ".": decimal(); break;
            case "+": case "−": case "×": case "÷": binary(key); break;
            case "=": equalsKey(); break;
            case "AC": case "C": clearAll(); break;
            case "CE": clearEntry(); break;
            case "⌫": backspace(); break;
            case "±": negate(); break;
            case "%": percent(); break;
            case "1/x": reciprocal(); break;
            case "x²": square(); break;
            case "√x": root(); break;
            case "MC": memory = null; break;
            case "MR": recall(); break;
            case "M+": memoryAdd(false); break;
            case "M−": memoryAdd(true); break;
            case "MS": memoryStore(); break;
        }
    }

    private void digit(String d) {
        if (error) clearAll();
        if (afterEquals && pendingOp == null) resetAfterResult();
        if (startNewEntry) {
            entry = d;
            startNewEntry = false;
            return;
        }
        if (digitCount(entry) >= DIGIT_LIMIT) return;
        if (entry.equals("0")) entry = d;
        else if (entry.equals("-0")) entry = d.equals("0") ? "-0" : "-" + d;
        else entry += d;
    }

    private void decimal() {
        if (error) clearAll();
        if (afterEquals && pendingOp == null) resetAfterResult();
        if (startNewEntry) { entry = "0."; startNewEntry = false; }
        else if (!entry.contains(".")) entry += ".";
    }

    private void resetAfterResult() {
        accumulator = null;
        pendingOp = null;
        lastOperand = null;
        lastOp = null;
        afterEquals = false;
        expression = "";
        entry = "0";
    }

    private void binary(String op) {
        if (error) return;
        BigDecimal value = current();
        if (value == null) return;
        if (pendingOp != null && !startNewEntry && accumulator != null) {
            BigDecimal result = apply(accumulator, value, pendingOp);
            if (result == null) return;
            accumulator = result;
            entry = plain(result);
        } else if (pendingOp == null || afterEquals) {
            accumulator = value;
        }
        pendingOp = op;
        expression = format(accumulator) + " " + op;
        startNewEntry = true;
        afterEquals = false;
        lastOperand = null;
        lastOp = null;
    }

    private void equalsKey() {
        if (error) return;
        if (pendingOp != null && accumulator != null) {
            BigDecimal right = startNewEntry ? accumulator : current();
            if (right == null) return;
            BigDecimal left = accumulator;
            BigDecimal result = apply(left, right, pendingOp);
            if (result == null) return;
            String line = format(left) + " " + pendingOp + " " + format(right) + " =";
            record(line, result);
            lastOp = pendingOp;
            lastOperand = right;
            pendingOp = null;
            accumulator = null;
            expression = line;
            entry = plain(result);
            afterEquals = true;
            startNewEntry = false;
        } else if (afterEquals && lastOp != null && lastOperand != null) {
            BigDecimal left = current();
            if (left == null) return;
            BigDecimal result = apply(left, lastOperand, lastOp);
            if (result == null) return;
            String line = format(left) + " " + lastOp + " " + format(lastOperand) + " =";
            record(line, result);
            expression = line;
            entry = plain(result);
        }
    }

    private void record(String line, BigDecimal result) {
        history.add(0, line + "\n" + format(result));
        if (history.size() > HISTORY_LIMIT) history.remove(history.size() - 1);
    }

    private BigDecimal apply(BigDecimal a, BigDecimal b, String op) {
        try {
            switch (op) {
                case "+": return safe(a.add(b, MC));
                case "−": return safe(a.subtract(b, MC));
                case "×": return safe(a.multiply(b, MC));
                case "÷":
                    if (b.signum() == 0) { fail("Cannot divide by zero"); return null; }
                    return safe(a.divide(b, MC));
                default: return null;
            }
        } catch (ArithmeticException ex) {
            fail("Invalid calculation"); return null;
        }
    }

    private BigDecimal safe(BigDecimal result) {
        if (result == null || result.precision() > 120 || Math.abs(result.scale()) > 150) {
            fail("Overflow"); return null;
        }
        return result;
    }

    private void clearAll() {
        entry = "0";
        accumulator = null;
        pendingOp = null;
        lastOperand = null;
        lastOp = null;
        startNewEntry = false;
        afterEquals = false;
        error = false;
        expression = "";
        // Calculator C deliberately leaves saved memory and history untouched.
    }

    private void clearEntry() {
        if (error) { clearAll(); return; }
        entry = "0";
        startNewEntry = false;
        if (afterEquals) clearAll();
    }

    private void backspace() {
        if (error) { clearAll(); return; }
        if (afterEquals || startNewEntry) return;
        if (entry.length() <= 1 || (entry.length() == 2 && entry.charAt(0) == '-')) entry = "0";
        else entry = entry.substring(0, entry.length() - 1);
    }

    private void negate() {
        if (error) return;
        if (startNewEntry) { entry = "0"; startNewEntry = false; }
        if (entry.startsWith("-")) entry = entry.substring(1);
        else if (!entry.equals("0")) entry = "-" + entry;
        afterEquals = false;
    }

    private void percent() {
        if (error) return;
        BigDecimal val = startNewEntry ? BigDecimal.ZERO : current();
        if (val == null) return;
        BigDecimal result;
        if (accumulator != null && ("+".equals(pendingOp) || "−".equals(pendingOp))) {
            result = accumulator.multiply(val, MC).divide(BigDecimal.valueOf(100), MC);
        } else {
            result = val.divide(BigDecimal.valueOf(100), MC);
        }
        entry = plain(result);
        startNewEntry = false;
        afterEquals = false;
    }

    private void reciprocal() {
        BigDecimal v = current(); if (v == null) return;
        if (v.signum() == 0) { fail("Cannot divide by zero"); return; }
        applyUnary(BigDecimal.ONE.divide(v, MC), "1/(" + format(v) + ")");
    }
    private void square() {
        BigDecimal v = current(); if (v == null) return;
        applyUnary(v.multiply(v, MC), "sqr(" + format(v) + ")");
    }
    private void root() {
        BigDecimal v = current(); if (v == null) return;
        if (v.signum() < 0) { fail("Invalid input"); return; }
        // Math.sqrt avoids reliance on BigDecimal.sqrt on older Android API levels.
        applyUnary(BigDecimal.valueOf(Math.sqrt(v.doubleValue())), "√(" + format(v) + ")");
    }
    private void applyUnary(BigDecimal result, String text) {
        if (safe(result) == null) return;
        entry = plain(result);
        expression = text;
        startNewEntry = false;
        afterEquals = false;
    }

    private void memoryStore() { BigDecimal v = current(); if (v != null) memory = v; }
    private void memoryAdd(boolean subtract) {
        BigDecimal v = current(); if (v == null) return;
        if (memory == null) memory = BigDecimal.ZERO;
        memory = subtract ? memory.subtract(v, MC) : memory.add(v, MC);
    }
    private void recall() {
        if (memory == null) return;
        if (error) clearAll();
        if (afterEquals && pendingOp == null) resetAfterResult();
        entry = plain(memory);
        startNewEntry = false;
    }
    private BigDecimal current() {
        if (error) return null;
        try { return new BigDecimal(entry.endsWith(".") ? entry.substring(0, entry.length() - 1) : entry); }
        catch (NumberFormatException e) { fail("Invalid input"); return null; }
    }
    private static int digitCount(String str) {
        int n = 0; for (int i = 0; i < str.length(); i++) if (Character.isDigit(str.charAt(i))) n++; return n;
    }
    private void fail(String message) {
        error = true;
        entry = message;
        pendingOp = null;
        accumulator = null;
        lastOp = null;
        lastOperand = null;
        expression = "";
        startNewEntry = false;
        afterEquals = false;
    }
    /** Loads a saved calculation result into the current input. */
    public boolean useHistoryResult(String record) {
        if (record == null || !record.contains("\n")) return false;
        String value = record.substring(record.lastIndexOf('\n') + 1).replace(",", "").trim();
        try {
            BigDecimal v = new BigDecimal(value);
            clearAll();
            entry = plain(v);
            return true;
        } catch (NumberFormatException ignored) {
            return false;
        }
    }
    public void clearHistory() { history.clear(); }
    public void clearMemory() { memory = null; }
    public void clearSavedData() { clearAll(); clearHistory(); clearMemory(); }
    public boolean hasMemory() { return memory != null; }
    public String getMemory() { return memory == null ? null : plain(memory); }
    public void restoreMemory(String value) {
        try { memory = value == null || value.isEmpty() ? null : new BigDecimal(value); }
        catch (NumberFormatException e) { memory = null; }
    }
    public List<String> getHistory() { return Collections.unmodifiableList(history); }
    public void restoreHistory(List<String> saved) {
        history.clear(); if (saved == null) return;
        for (String line : saved) {
            if (line != null && line.length() <= 300 && history.size() < HISTORY_LIMIT) history.add(line);
        }
    }
    public String getDisplay() {
        if (error) return entry;
        if (entry.endsWith(".")) return format(new BigDecimal(entry.substring(0, entry.length() - 1))) + ".";
        if (entry.equals("-0")) return "-0";
        return format(new BigDecimal(entry));
    }
    public String getExpression() { return expression; }
    public boolean hasError() { return error; }
    private static String plain(BigDecimal number) {
        BigDecimal clean = number.stripTrailingZeros();
        if (clean.compareTo(BigDecimal.ZERO) == 0) return "0";
        return clean.toPlainString();
    }
    private static String format(BigDecimal number) {
        DecimalFormat f = new DecimalFormat("#,##0.################", DecimalFormatSymbols.getInstance(Locale.US));
        f.setRoundingMode(RoundingMode.HALF_UP);
        f.setGroupingUsed(true);
        f.setMaximumFractionDigits(16);
        return f.format(number);
    }
}
