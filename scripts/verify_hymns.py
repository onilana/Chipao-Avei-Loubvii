import json

with open('app/src/main/assets/hymns.json', 'r', encoding='utf-8') as f:
    hymns = json.load(f)

print('JSON valid:', len(hymns), 'hymns')

for hid in [1, 50, 100, 250, 500]:
    h = next((x for x in hymns if x['id'] == hid), None)
    if h:
        vcount = sum(len(v.get('lines', [])) for v in h.get('verses', []))
        title = h['title'][:30]
        nverses = len(h.get('verses', []))
        print(f'Hymn {hid}: {title:30s} | verse blocks: {nverses} | total lines: {vcount}')
