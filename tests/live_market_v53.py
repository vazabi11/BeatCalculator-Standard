"""Network integration diagnostics. Never use test numbers as live prices."""
import json, urllib.request, math, datetime, sys
failures=[]
try:
    with urllib.request.urlopen('https://api.frankfurter.dev/v2/rate/USD/THB',timeout=15) as r: j=json.load(r)
    assert j['base']=='USD' and j['quote']=='THB' and math.isfinite(float(j['rate'])) and float(j['rate'])>0
    datetime.date.fromisoformat(j['date'])
    print('Frankfurter daily reference: schema and rate validated; date',j['date'])
except Exception as e:
    print('Frankfurter unavailable:',type(e).__name__,str(e)[:100]);failures.append('Frankfurter')
try:
    import websocket
    ws=websocket.create_connection('wss://stream.binance.com:443/stream?streams=btcusdt@ticker',timeout=15)
    try:
        q=json.loads(ws.recv())['data']; assert q['s']=='BTCUSDT' and float(q['c'])>0 and int(q['E'])>0
        print('Binance ticker stream: BTCUSDT schema validated; timestamp',q['E'])
    finally: ws.close()
except Exception as e:
    print('Binance stream unavailable:',type(e).__name__,str(e)[:100]);failures.append('Binance')
print('Twelve Data: requires personally authorized key and entitlement; not tested here.')
print('Reference exchange board and regional fuel feeds: see LiveConnectivityTest report.')
for pair in ('EUR/USD','GBP/USD','USD/JPY','USD/THB','USD/CHF','AUD/USD'):
    try:
        with urllib.request.urlopen('https://api.frankfurter.dev/v2/rate/'+pair,timeout=15) as r: j=json.load(r)
        assert j['base']+'/'+j['quote']==pair and math.isfinite(float(j['rate'])) and float(j['rate'])>0
        datetime.date.fromisoformat(j['date']); print(pair,'reference OK',j['date'])
    except Exception as e: print(pair,'unavailable',str(e)[:120]);failures.append(pair)
try:
    with urllib.request.urlopen('https://data-api.binance.vision/api/v3/ticker/24hr?symbol=BTCUSDT',timeout=15) as r: j=json.load(r)
    assert j['symbol']=='BTCUSDT' and float(j['lastPrice'])>0 and int(j['closeTime'])>0
    print('Binance latest snapshot OK',j['closeTime'])
except Exception as e: print('Binance snapshot unavailable',str(e)[:120]);failures.append('snapshot')
sys.exit(1 if failures else 0)
