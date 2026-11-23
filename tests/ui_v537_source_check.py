"""SiraCalc V5.3.7 market source structural gate; not a live API/Android device test."""
from pathlib import Path
root=Path(__file__).resolve().parents[1]
java=root/'app/src/main/java/com/beatoflove/standardcalculator'
p=(java/'PriceService.java').read_text(encoding='utf-8')
m=(java/'MainActivity.java').read_text(encoding='utf-8')
c=(java/'PriceCache.java').read_text(encoding='utf-8')
g=(root/'app/build.gradle').read_text(encoding='utf-8')
policy=(java/'GoldSourcePolicy.java').read_text(encoding='utf-8')
checks={
  'version': "versionCode 16" in g and "versionName '5.3.7'" in g,
  'stable application id': "applicationId 'com.beatoflove.standardcalculator'" in g,
  'ThaiGoldToday attempted': 'primary=parseThaiGold(readThaiGoldToday())' in p,
  'Rakhagold attempted': 'backup=GoldFallbackParser.parse(readRakhagold())' in p,
  'selection uses publisher timestamps': 'GoldSourcePolicy.choose(' in p and 'publisherTime(primary.publishedAt)' in p and 'publisherTime(backup.publishedAt)' in p,
  'no legacy first-return bug': 'try { return GoldFallbackParser.parse(readRakhagold()); }' not in p,
  'both gated at one minute': 'now-primaryAttempt<60_000L' in p and 'now-goldAttempt<60_000L' in p,
  'reject invalid primary timestamp': 'GoldSourcePolicy.validPublisher(ms,now)' in p,
  'reject reversed bids and asks': 'sellBar<buyBar||sellOrn<buyOrn' in p,
  'source credited by API adapter': '"ThaiGoldToday"' in p,
  'existing 60s market polling': 'handler.postDelayed(this,marketIndex()==0?60*1000L:5*60*1000L)' in m,
  'preserve last good cache': 'PriceCache.putThaiGold(this,data)' in m and 'PriceCache.getThaiGold(this)' in m,
  'old quotes marked not current': '"ประกาศย้อนหลัง · ตรวจสอบ"' in m,
  'existing UI navigation kept': 'NAV={1,2,3,7,8,5,6,4}' in m,
  'cache older update refused': 'CacheUpdatePolicy.shouldAccept' in c,
  'reject publisher future timestamps': 'at <= now + 10L * 60L * 1000L' in policy,
}
for n,ok in checks.items():
    if not ok:raise AssertionError(n)
    print('PASS',n)
print(len(checks),'structural checks passed')
