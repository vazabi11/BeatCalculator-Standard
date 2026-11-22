"""Structural source checks for V5.3.6. Not Android UI/interaction tests."""
from pathlib import Path
import xml.etree.ElementTree as ET
root=Path(__file__).resolve().parents[1]
base=root/'app/src/main/java/com/beatoflove/standardcalculator'
m=(base/'MainActivity.java').read_text();f=(base/'FormulaPanel.java').read_text();fin=(base/'FinancialPanel.java').read_text();c=(base/'CurrencyPanel.java').read_text();a=(base/'CalcActions.java').read_text();g=(root/'app/build.gradle').read_text()
std=m.split('private void buildCalculator(int mode){',1)[1].split('LinearLayout display=vertical();',1)[0]
conv=m.split('private void buildUnits(){',1)[1].split('private void updateUnits(){',1)[0]
cur=c.split(' void convert(',1)[1].split(' void exchangeRates(',1)[0]
checks={
 'version 5.3.6 / code 15':"versionName '5.3.6'" in g and 'versionCode 15' in g,
 'package stable':"applicationId 'com.beatoflove.standardcalculator'" in g,
 'no obsolete calculator header references':all(x not in m for x in ('modeBar','modeTitle','showModeDialog')),
 'standard/scientific action strip':'CalcActions.add(this,display,this::copyResult,this::showHistory)' in m,
 'existing calculator history recall': 'engine.useHistoryResult(record)' in m and 'engine.clearHistory()' in m,
 'eight tabs unchanged':'NAV={1,2,3,7,8,5,6,4}' in m,
 'converter history and copy':'log.copyRecord(unitHistoryLabel()' in conv and 'Converter · History' in conv,
 'converter auto record': 'unitAutoRecord.changed()' in m,
 'currency history and copy':'log.copyRecord(context.get()' in cur and 'Currency · History' in cur,
 'currency automatic debounced record':'finally{auto.changed();}' in cur,
 'currency no internal keypad':'keys(' not in cur,
 'formula current copy and setup':'this::copyCurrent,this::showHistory' in f and 'small("Setup",this::setup)' in f,
 'formula replay preserved':'input.setText(record.substring(0,end))' in f,
 'formula SD REG log':'"formula_reg":"formula_sd"' in f and 'log.record((paired?' in f,
 'formula prediction log':'log.record((inverse?' in f,
 'financial accounting history':'this::accountHistory' in fin and 'tape.get(i)' in fin,
 'financial tape retained':'Tape / Grand Total' in fin,
 'financial tools record':'new CalcActions.Log(a,"financial_"+n)' in fin and 'new CalcActions.Log(a,"financial_"+k)' in fin,
 'financial invalidates old results':'watch(field,()->o.setText("—"))' in fin,
 'shared icon sizes':'dp(a,48)' in a and 'dp(a,12)' in a,
 'accessibility and ripple':all(x in a for x in ['setContentDescription','setTooltipText','RippleDrawable']),
 'local logs and bounded retention': 'siracalc_history_v536' in a and 'i<99' in a,
 'auto record guards': all(x in a for x in ['1200','isAttachedToWindow()','revision==log.revision','context.equals(label.get())']),
 'clear confirmation and valid result checks':'ล้างประวัติ?' in a and 'if(!valid(value))' in a,
}
for name in ('ic_calc_copy','ic_calc_history'):
    node=ET.parse(root/f'app/src/main/res/drawable/{name}.xml').getroot();ns='{http://schemas.android.com/apk/res/android}'
    checks[name+' 24dp vector']=node.tag=='vector' and node.get(ns+'width')=='24dp' and node.get(ns+'height')=='24dp'
for n,ok in checks.items():
    if not ok:raise AssertionError(n)
    print('PASS',n)
print(len(checks),'structural checks passed (not device QA)')
