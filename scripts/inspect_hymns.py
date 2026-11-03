"""Inspect current hymns.json to identify issues before cross-check."""
import json

with open('app/src/main/assets/hymns.json', 'r', encoding='utf-8') as f:
    hymns = json.load(f)

# Check which hymns have empty/missing verses
empty_verses = []
for h in hymns:
    verses = h.get('verses', [])
    if not verses:
        empty_verses.append(h['id'])
    else:
        all_empty = all(len(v.get('lines', [])) == 0 for v in verses)
        if all_empty:
            empty_verses.append(h['id'])

print(f"Hymns with empty verses ({len(empty_verses)}): {empty_verses[:80]}")
print()

# Check hymns with suspicious 1-line verses only
thin = []
for h in hymns:
    verses = h.get('verses', [])
    total_lines = sum(len(v.get('lines', [])) for v in verses)
    if 0 < total_lines <= 2 and len(verses) == 1:
        thin.append((h['id'], h.get('title', ''), total_lines))

print(f"Hymns with very few lines ({len(thin)}):")
for hid, title, lc in thin[:30]:
    print(f"  Hymn {hid}: '{title}' - {lc} lines")
print()

# Show totals
titles_empty = sum(1 for h in hymns if not h.get('title', '').strip())
print(f"Hymns missing title: {titles_empty}")

# Show sample of a good hymn to understand structure
h = next((x for x in hymns if x['id'] == 50), None)
if h:
    print("\\nSample hymn 50 structure:")
    print(json.dumps(h, ensure_ascii=False, indent=2)[:800])
