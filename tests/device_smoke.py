"""Optional Android device/emulator smoke check. Requires adb and installed APK.
Not run in Chat sandbox; does not certify every key or live data endpoint.
"""
import subprocess, time, re, xml.etree.ElementTree as ET
PKG='com.beatoflove.standardcalculator'
def adb(*args): return subprocess.check_output(['adb',*args],text=True)
def dump():
    adb('shell','uiautomator','dump','/sdcard/siracalc-ui.xml')
    return ET.fromstring(adb('shell','cat','/sdcard/siracalc-ui.xml'))
def tap_text(text):
    for n in dump().iter('node'):
        if n.get('text')==text and n.get('clickable')=='true':
            x,y,X,Y=map(int,re.findall(r'\d+',n.get('bounds')));adb('shell','input','tap',str((x+X)//2),str((y+Y)//2));return
    raise AssertionError('Clickable key not found: '+text)
def contains(text): return any(text in (n.get('text') or '') for n in dump().iter('node'))
adb('shell','am','force-stop',PKG);adb('shell','am','start','-W','-n',PKG+'/.MainActivity');time.sleep(1)
assert contains('SiraCalc') and contains('v5.3.3') and contains('Standard')
for k in ('C','7','×','8','='):tap_text(k)
assert contains('56'), 'standard arithmetic'
# Swipe within content, away from editable/scrollable result. Portrait emulator assumed.
w,h=map(int,re.findall(r'(\d+)x(\d+)',adb('shell','wm','size'))[-1]);
for expected in ('Scientific','Formula Pro','Financial','Currency','Fuel Prices','Markets','Converter'):
    adb('shell','input','swipe',str(int(w*.86)),str(int(h*.62)),str(int(w*.12)),str(int(h*.62)),'350');time.sleep(.7)
    assert contains(expected), 'navigation: '+expected
    print('Visible:',expected)
print('Device smoke complete (not exhaustive)')
