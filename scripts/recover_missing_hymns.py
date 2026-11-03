"""
recover_missing_hymns.py
-------------------------
Recovers the 56 hymn IDs that the broad detector missed.
Strategy:
  1. Dump ALL raw half-page text into one flat list of lines.
  2. Scan for any line that starts with exactly the missing number
     (with or without ALL-CAPS) and grab the surrounding block.
  3. Parse each recovered block into a structured hymn.
  4. Merge into hymns.json (only if existing entry has empty/skeleton verses).
"""
import json, re, sys, os

sys.path.insert(0, os.path.abspath(os.path.join(os.path.dirname(__file__), '..')))
from scripts.parse_full_hymns import (
    extract_half_pages, clean_text_line, clean_poula_title,
    is_footer_or_header_line, get_categories_for_id, parse_key_pitch, REF_PATTERN,
)

PDF_PATH = (
    r"C:\Users\MEC-Oni\.gemini\antigravity\brain"
    r"\e2e67671-24b4-4903-8fe7-8645ee02122a\.user_uploaded"
    r"\media_1793689295416_353bfee4.pdf"
)
JSON_PATH = os.path.abspath(
    os.path.join(os.path.dirname(__file__), '..', 'app', 'src', 'main', 'assets', 'hymns.json')
)

MISSING_IDS = {51,56,59,60,65,72,74,80,105,107,109,120,122,137,139,146,147,149,
               155,165,169,178,191,204,208,209,224,231,232,233,242,251,256,262,
               263,274,278,280,286,293,297,302,350,361,365,369,395,397,408,410,
               418,423,425,472,496}
# 500 is handled separately

# ── Helpers ───────────────────────────────────────────────────────────────────

def parse_hymn_block(h_id, block_lines):
    """Parse a raw block of lines for a single hymn."""
    cleaned = [clean_text_line(l) for l in block_lines if l.strip()]
    if not cleaned:
        return None

    title = eng_title = ref = scripture = time_sig = key_sig = author = composer = ""
    verses, chorus = [], []

    # Line 0: "NNN[. ]TITLE [REF?]"
    line0 = cleaned[0]
    line0s = re.sub(r'^\s*' + str(h_id) + r'[\.\s]*', '', line0).strip()
    m_ref = re.search(REF_PATTERN, line0s, re.IGNORECASE)
    if m_ref:
        ref = m_ref.group(0).strip(' ()')
        title = line0s[:m_ref.start()].strip()
    else:
        title = line0s
    title = clean_poula_title(title)

    body_start = 1
    for idx in range(1, min(7, len(cleaned))):
        cur = cleaned[idx]
        if re.match(r'^(1\.|1\s+[A-Z\u00dc\u00fc]|Khoras|Chorus)\b', cur, re.IGNORECASE):
            body_start = idx
            break
        is_hdr = False
        eng_m = re.search(r'\(([^)]+)\)', cur)
        if eng_m:
            cand = eng_m.group(1).strip()
            if not re.search(r'\b(arr|times|repeat|ref)\b', cand, re.IGNORECASE) and not eng_title:
                eng_title = cand
                is_hdr = True
        key_m = re.search(r'\b(?:Doh\s+is|Key)\s+([A-G]\s*(?:b|#)?)', cur, re.IGNORECASE)
        if key_m:
            is_hdr = True
            key_sig = f"Doh is {key_m.group(1).replace(' ','')}"
            mt = re.search(r'\b(\d/\d|C)\b', cur)
            if mt:
                time_sig = mt.group(1)
        if not scripture:
            sc = re.match(r'^\s*([I]{0,3}[Vv]?\s*[A-Za-z]+\.?\s*\d+:\d+(?:-\d+)?)\s*$', cur)
            if sc:
                scripture = sc.group(1).strip()
                is_hdr = True
        if not is_hdr:
            body_start = idx
            break
        else:
            body_start = idx + 1

    body = [l for l in cleaned[body_start:] if not is_footer_or_header_line(l)]

    cur_vn, cur_vl, in_chorus = None, [], False
    for l in body:
        if re.match(r'^(Khoras|Chorus)\b', l, re.IGNORECASE):
            if cur_vn is not None and cur_vl:
                verses.append({'verseNumber': cur_vn, 'lines': cur_vl})
                cur_vn, cur_vl = None, []
            in_chorus = True
            rest = re.sub(r'^(Khoras|Chorus)\s*:?\s*', '', l, flags=re.IGNORECASE).strip()
            if rest:
                chorus.append(rest)
            continue
        vm = re.match(r'^(\d+)\.\s*(.*)', l) or re.match(r'^(\d+)\s+([A-Z\u00dc\u00fc].*)', l)
        if vm:
            in_chorus = False
            if cur_vn is not None and cur_vl:
                verses.append({'verseNumber': cur_vn, 'lines': cur_vl})
            cur_vn = int(vm.group(1))
            cur_vl = [vm.group(2).strip()] if vm.group(2).strip() else []
            continue
        if in_chorus:
            chorus.append(l)
        elif cur_vn is not None:
            cur_vl.append(l)
        else:
            if not cur_vl:
                cur_vn = 1
            cur_vl.append(l)

    if cur_vn is not None and cur_vl:
        verses.append({'verseNumber': cur_vn, 'lines': cur_vl})
    if not verses and body:
        verses.append({'verseNumber': 1, 'lines': body})

    first_line = verses[0]['lines'][0].rstrip(',;.:') if verses and verses[0]['lines'] else ''
    pcat, acats = get_categories_for_id(h_id)
    phz, kname = parse_key_pitch(key_sig)

    return {
        "id": h_id, "title": title, "englishTitle": eng_title, "firstLine": first_line,
        "timeSignature": time_sig, "keySignature": key_sig, "hymnBookRef": ref,
        "author": author, "composer": composer, "scripture": scripture,
        "category": pcat, "verses": verses,
        "chorus": chorus if chorus else None,
        "keynotePitchHz": phz, "keynoteName": kname,
        "tune": eng_title, "meter": time_sig, "year": "", "categories": acats,
    }

# ── All known hymn IDs from broad pass (1-499 detected) ──────────────────────
KNOWN_IDS = {1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16,17,18,19,20,21,22,23,24,25,26,27,28,29,30,
 31,32,33,34,35,36,37,38,39,40,41,42,43,44,45,46,47,48,49,50,52,53,54,55,57,58,61,62,63,64,
 66,67,68,69,70,71,73,75,76,77,78,79,81,82,83,84,85,86,87,88,89,90,91,92,93,94,95,96,97,98,
 99,100,101,102,103,104,106,108,110,111,112,113,114,115,116,117,118,119,121,123,124,125,126,
 127,128,129,130,131,132,133,134,135,136,138,140,141,142,143,144,145,148,150,151,152,153,154,
 156,157,158,159,160,161,162,163,164,166,167,168,170,171,172,173,174,175,176,177,179,180,181,
 182,183,184,185,186,187,188,189,190,192,193,194,195,196,197,198,199,200,201,202,203,205,206,
 207,210,211,212,213,214,215,216,217,218,219,220,221,222,223,225,226,227,228,229,230,234,235,
 236,237,238,239,240,241,243,244,245,246,247,248,249,250,252,253,254,255,257,258,259,260,261,
 264,265,266,267,268,269,270,271,272,273,275,276,277,279,281,282,283,284,285,287,288,289,290,
 291,292,294,295,296,298,299,300,301,303,304,305,306,307,308,309,310,311,312,313,314,315,316,
 317,318,319,320,321,322,323,324,325,326,327,328,329,330,331,332,333,334,335,336,337,338,339,
 340,341,342,343,344,345,346,347,348,349,351,352,353,354,355,356,357,358,359,360,362,363,364,
 366,367,368,370,371,372,373,374,375,376,377,378,379,380,381,382,383,384,385,386,387,388,389,
 390,391,392,393,394,396,398,399,400,401,402,403,404,405,406,407,409,411,412,413,414,415,416,
 417,419,420,421,422,424,426,427,428,429,430,431,432,433,434,435,436,437,438,439,440,441,442,
 443,444,445,446,447,448,449,450,451,452,453,454,455,456,457,458,459,460,461,462,463,464,465,
 466,467,468,469,470,471,473,474,475,476,477,478,479,480,481,482,483,484,485,486,487,488,489,
 490,491,492,493,494,495,497,498,499}

# ── Extract all text lines into flat list ────────────────────────────────────
print("Extracting PDF ...")
all_halves = extract_half_pages(PDF_PATH)
flat_lines = []
for _pg, _side, lines in all_halves:
    for l in lines:
        flat_lines.append(l)
print(f"Total raw lines: {len(flat_lines)}")

# ── Find all hymn-start positions (both known and missing) ───────────────────
# Build a map: hymn_id -> start_index in flat_lines
hymn_starts = {}  # id -> line index
for i, line in enumerate(flat_lines):
    s = line.strip()
    # Match "NNN" or "NNN." or "NNN " followed by anything, or just "NNN"
    m = re.match(r'^(\d{1,3})\s*[.\s]?\s*(.*)', s)
    if not m:
        continue
    cand = int(m.group(1))
    if 1 <= cand <= 499:
        if cand not in hymn_starts:
            hymn_starts[cand] = i

print(f"Hymn starts found: {len(hymn_starts)}")
still_missing = MISSING_IDS - set(hymn_starts.keys())
print(f"Still missing after flat scan: {sorted(still_missing)}")

# ── Build hymn blocks from flat_lines using all detected starts ───────────────
# Sort by position
sorted_starts = sorted(hymn_starts.items(), key=lambda x: x[1])

# For each missing hymn, grab lines between its start and the next hymn's start
recovered = {}
all_starts_sorted = [(hid, idx) for hid, idx in sorted_starts]

for pos, (h_id, start_idx) in enumerate(all_starts_sorted):
    if h_id not in MISSING_IDS:
        continue
    # Find end: next known hymn start after this
    end_idx = len(flat_lines)
    for j in range(pos + 1, len(all_starts_sorted)):
        next_id, next_idx = all_starts_sorted[j]
        if next_idx > start_idx:
            end_idx = next_idx
            break
    block = flat_lines[start_idx:end_idx]
    # Limit to reasonable block size (max 200 lines)
    block = block[:200]
    parsed = parse_hymn_block(h_id, block)
    if parsed and parsed.get('verses'):
        total_lines = sum(len(v.get('lines', [])) for v in parsed['verses'])
        if total_lines > 0:
            recovered[h_id] = parsed
            print(f"  Recovered #{h_id}: '{parsed['title']}' ({total_lines} lines)")

print(f"\nRecovered {len(recovered)} missing hymns")

# ── Merge into hymns.json ────────────────────────────────────────────────────
with open(JSON_PATH, 'r', encoding='utf-8') as f:
    hymns_list = json.load(f)
hymns_map = {h['id']: h for h in hymns_list}

merged_count = 0
for h_id, hymn in recovered.items():
    if h_id in hymns_map:
        ex = hymns_map[h_id]
        ex_lines = sum(len(v.get('lines', [])) for v in ex.get('verses', []))
        new_lines = sum(len(v.get('lines', [])) for v in hymn.get('verses', []))
        # Only update if the new data is more complete
        if new_lines > ex_lines:
            hymns_map[h_id] = hymn
            merged_count += 1
    else:
        hymns_map[h_id] = hymn
        merged_count += 1

print(f"Merged {merged_count} hymns into JSON")

final_list = sorted(hymns_map.values(), key=lambda h: h['id'])
with open(JSON_PATH, 'w', encoding='utf-8') as f:
    json.dump(final_list, f, ensure_ascii=False, indent=2)

print(f"Done: {len(final_list)} total hymns in hymns.json")

# Report which are still missing/empty
still_thin = []
for hid in MISSING_IDS:
    h = hymns_map.get(hid)
    if h:
        lc = sum(len(v.get('lines', [])) for v in h.get('verses', []))
        if lc < 3:
            still_thin.append((hid, h.get('title', ''), lc))
if still_thin:
    print(f"\nStill thin (<3 lines) after recovery:")
    for hid, t, lc in still_thin:
        print(f"  #{hid} '{t}': {lc} lines")
