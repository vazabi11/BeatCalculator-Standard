"""Structural regression checks. Not an Android rendering/interaction test."""
from pathlib import Path
import re
root=Path(__file__).resolve().parents[1]
base=root/'app/src/main/java/com/beatoflove/standardcalculator'
m=(base/'MainActivity.java').read_text()
c=(base/'CurrencyPanel.java').read_text()
r=(base/'ResponsiveForm.java').read_text()
cu=c.split(' void convert(',1)[1].split(' void exchangeRates(',1)[0]
unit=m.split('    private void buildUnits(){',1)[1].split('    private void updateUnits(){',1)[0]
about=m.split('    private void showAbout(){',1)[1].split('    private GradientDrawable',1)[0]
checks={
 'currency no custom keypad': not re.search(r'\bkeys\s*\(',cu),
 'currency numeric input': 'input(l,"Amount","1",false)' in cu and 'TYPE_CLASS_NUMBER' in r,
 'currency two modes': all(x in c for x in ('"Convert"','"Exchange Rates"','referenceBoard(')),
 'currency keeps swap and refresh': 'Swap currencies' in cu and 'Refresh rate' in cu,
 'currency keeps reference/cache': all(x in cu for x in ('PriceService.reference(', 'prefs.edit()', 'live_markets53')),
 'converter uses scoped form': 'new ResponsiveForm(this)' in unit and 'ui.result(container)' in unit,
 'converter preserves swap/copy': 'unitFrom.setSelection(b)' in unit and 'log.copyRecord(unitHistoryLabel(),unitOutput.getText().toString())' in unit,
 'converter excludes tax category': '.filter(c->!c.equals("ภาษี"))' in unit,
 'measured adaptive width': 'View.MeasureSpec.getSize(w)' in r and 'UiPolicy.sideBySide' in r,
 'scroll fallback': 'setFillViewport(true)' in r and 'ScrollView' in r,
 'tap targets min 48dp': 'setMinimumHeight(dp(48))' in r,
 'selectable wrap output': 'setSingleLine(false); t.setTextIsSelectable(true)' in r,
 'about no repo link': 'GitHub' not in about and 'github.com' not in about and 'setNeutralButton' not in about,
 'about preserves metadata': all(x in about for x in ('versionName()', 'versionCode', 'Sirawit Dh.', 'Sirawit Labs', 'setIcon', 'setPositiveButton')),
 'eight tabs retained': 'NAV={1,2,3,7,8,5,6,4}' in m,
 'application ID retained': "applicationId 'com.beatoflove.standardcalculator'" in (root/'app/build.gradle').read_text(),
}
for name,ok in checks.items():
    if not ok: raise AssertionError(name)
    print('PASS',name)
print(f'{len(checks)} structural checks passed (not device QA)')
