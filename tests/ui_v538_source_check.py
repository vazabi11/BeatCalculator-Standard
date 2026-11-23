"""V5.3.8 free source resilience and working source-navigation checks; not live device QA."""
from pathlib import Path
root=Path(__file__).resolve().parents[1]
base=root/'app/src/main/java/com/beatoflove/standardcalculator'
p=(base/'PriceService.java').read_text(encoding='utf-8')
m=(base/'MainActivity.java').read_text(encoding='utf-8')
c=(base/'CommunityGoldParser.java').read_text(encoding='utf-8')
g=(root/'app/build.gradle').read_text(encoding='utf-8')
w=(root/'.github/workflows/build-apk.yml').read_text(encoding='utf-8')
t=(root/'tests/ui_v537_source_check.py').read_text(encoding='utf-8')
checks={
 'version 5.3.8 and code 17':"versionName '5.3.8'" in g and 'versionCode 17' in g,
 'stable app identity':"applicationId 'com.beatoflove.standardcalculator'" in g,
 'third independent source':'https://api.chnwt.dev/thai-gold-api/latest' in p,
 'third feed time limited':'now-communityAttempt<60_000L' in p,
 'three-source newest publisher selection':'GoldSourcePolicy.newest(' in p,
 'third quote included':'community=CommunityGoldParser.parse(readCommunityGold())' in p,
 'third parser has publisher validation':'GoldSourcePolicy.validPublisher(published,now)' in c,
 'third parser checks four price fields':'value(bar,"buy")' in c and 'value(ornaments,"sell")' in c,
 'no accidental fake publisher time':'System.currentTimeMillis()' in c and 'MarketTime.normalize(date,clock.group(1))' in c,
 'official source direct link':'https://sse.goldtraders.or.th/dailyprices' in m,
 'official price button':'ดูราคาประกาศล่าสุดจากสมาคมค้าทองคำ' in m,
 'source navigation only verified content':'https://github.com/max180643/thai-gold-api' in m and 'https://rakhagold.com/api-docs/' in m,
 'stale quote explicit warning':'ข้อมูลจาก API เกิน 24 ชม.' in m,
 'keeps current cache':'PriceCache.getThaiGold(this)' in m,
 'version-gate not pinned to older exact build':'minimum version 5.3.7' in t,
 'run v538 check in GitHub actions':'python3 tests/ui_v538_source_check.py' in w,
 'no signing material in this patch':'KEYSTORE_B64' not in p,
}
for label,ok in checks.items():
    assert ok,label
    print('PASS',label)
print(len(checks),'structural checks passed (not live API or device QA)')
