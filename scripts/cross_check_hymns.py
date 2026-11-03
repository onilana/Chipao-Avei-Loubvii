"""
cross_check_hymns.py
--------------------
1. Parses ALL hymns (1-500) from the latest PDF using a broad hymn-detector.
2. Cross-checks against existing hymns.json:
   - Updates verses/chorus where the PDF has more/different content.
   - Adds any truly new hymn entries (shouldn't happen for 1-500 but guards future).
3. Writes a diff report so you can review what changed.
4. Saves updated hymns.json.

Usage:
    python scripts/cross_check_hymns.py
"""
import json
import os
import re
import sys

# ── Paths ─────────────────────────────────────────────────────────────────────
PROJECT_ROOT = os.path.abspath(os.path.join(os.path.dirname(__file__), '..'))
PDF_PATH = (
    r"C:\Users\MEC-Oni\.gemini\antigravity\brain"
    r"\e2e67671-24b4-4903-8fe7-8645ee02122a\.user_uploaded"
    r"\media_1793689295416_353bfee4.pdf"
)
JSON_PATH = os.path.join(PROJECT_ROOT, 'app', 'src', 'main', 'assets', 'hymns.json')
REPORT_PATH = os.path.join(PROJECT_ROOT, 'scripts', 'cross_check_report.txt')

# ── Import original helpers ────────────────────────────────────────────────────
sys.path.insert(0, PROJECT_ROOT)
from scripts.parse_full_hymns import (
    extract_half_pages,
    clean_text_line,
    clean_poula_title,
    is_footer_or_header_line,
    get_categories_for_id,
    parse_key_pitch,
    REF_PATTERN,
    parse_hymn_500 as _parse_hymn_500,
    segment_hymns as _segment_hymns_220_500,
)

# ── Category / section maps (same as parse_full_hymns) ───────────────────────
PRIMARY_SECTIONS = [
    (1, 22, "Praise & Adoration"),
    (23, 53, "Worship"),
    (54, 72, "Prayer & Consecration"),
    (73, 97, "Faith & Trust"),
    (98, 116, "The Gospel"),
    (117, 140, "Gospel Invitation"),
    (141, 158, "Love"),
    (159, 179, "Assurance & Testimony"),
    (180, 193, "Guidance"),
    (194, 237, "Mission & Evangelism"),
    (238, 267, "Heavenly Home"),
    (268, 291, "Repentance, Commitment & Hope"),
    (292, 305, "Second Coming & Rapture"),
    (306, 325, "Funeral, Life to Come"),
    (326, 329, "Baptism"),
    (330, 358, "Christian Endeavour"),
    (359, 375, "Christmas"),
    (376, 376, "Closing Worship"),
    (377, 381, "New Year"),
    (382, 382, "Palm Sunday"),
    (383, 405, "Good Friday & Easter Sunday"),
    (406, 413, "Lord's Supper"),
    (414, 430, "Christian Experience"),
    (431, 433, "Wedding"),
    (434, 439, "Mothers' & Fathers' Day"),
    (440, 445, "Conflict & Courage"),
    (446, 447, "Jubilee"),
    (448, 451, "Offeratory"),
    (452, 500, "Revival, Choruses & Social"),
]


# ─────────────────────────────────────────────────────────────────────────────
# STEP 1 – Extract ALL hymns from PDF (range 1-500)
# ─────────────────────────────────────────────────────────────────────────────

def segment_all_hymns(all_halves):
    """Like segment_hymns but captures IDs 1-500 (not just 220-500)."""
    hymns_raw = {}
    current_id = None
    current_lines = []

    for _sheet, _side, lines in all_halves:
        cleaned = []
        for l in lines:
            s = l.strip()
            # Skip page-number decorators like "-(12)-"
            if re.match(r'^-\s*\(\s*\d*\s*\)\s*-$', s):
                continue
            cleaned.append(l)

        i = 0
        while i < len(cleaned):
            line = cleaned[i]
            s = line.strip()

            # Detect "NNN TITLE" or "NNN. TITLE" at start of line (1..500)
            m = re.match(r'^(\d{1,3})[\.\s]\s*([A-Z\u00dc\u00fc].*)', s)
            if not m:
                # Also try bare "NNN" with next line being title
                m = re.match(r'^(\d{1,3})$', s)
            h_id = None
            if m:
                cand = int(m.group(1))
                if 1 <= cand <= 500:
                    # Reject if the number looks like a verse marker (e.g. "1." at indent)
                    # Only treat as new hymn if it's 3-digit, OR 1-2 digit followed by uppercase
                    raw_num = m.group(1)
                    rest = m.group(2).strip() if len(m.groups()) >= 2 else ""
                    # 3-digit numbers are almost always hymns
                    if len(raw_num) == 3:
                        h_id = cand
                    elif len(raw_num) <= 2:
                        # 1-2 digit: only treat as new hymn if followed by ALL-CAPS word
                        if rest and re.match(r'^[A-Z\u00dc\u00fc]{2,}', rest):
                            # And the line starts at column 0 (not indented)
                            if not line.startswith(' ') and not line.startswith('\t'):
                                h_id = cand

            if h_id is not None:
                if current_id is not None:
                    hymns_raw[current_id] = current_lines
                current_id = h_id
                current_lines = [line]
            else:
                if current_id is not None:
                    current_lines.append(line)
            i += 1

    if current_id is not None:
        hymns_raw[current_id] = current_lines

    return hymns_raw


def parse_hymn_from_raw(h_id, raw_lines):
    """Parse a single hymn from raw lines into structured dict."""
    cleaned = [clean_text_line(l) for l in raw_lines if l.strip()]
    if not cleaned:
        return None

    title = ""
    ref = ""
    eng_title = ""
    scripture = ""
    time_sig = ""
    key_sig = ""
    author = ""
    composer = ""
    verses = []
    chorus = []

    # Line 0: contains "NNN TITLE [Ref?]"
    line0 = cleaned[0]
    line0_stripped = re.sub(r'^\s*' + str(h_id) + r'[\.\s]*', '', line0).strip()
    m_ref = re.search(REF_PATTERN, line0_stripped, re.IGNORECASE)
    if m_ref:
        ref = m_ref.group(0).strip(' ()')
        title = line0_stripped[:m_ref.start()].strip()
    else:
        title = line0_stripped
    title = clean_poula_title(title)

    # Header lines (up to 5 lines after title)
    body_start = 1
    for idx in range(1, min(6, len(cleaned))):
        cur = cleaned[idx]
        if re.match(r'^(1\.|1\s+[A-Z\u00dc\u00fc]|Khoras|Chorus)\b', cur, re.IGNORECASE):
            body_start = idx
            break

        is_header = False

        # English title in parens
        eng_m = re.search(r'\(([^)]+)\)', cur)
        if eng_m:
            cand = eng_m.group(1).strip()
            if not re.search(r'\b(arr|times|repeat|ref)\b', cand, re.IGNORECASE):
                if not eng_title:
                    eng_title = cand
                    is_header = True
            rest = (cur[:eng_m.start()] + ' ' + cur[eng_m.end():]).strip()
            sc_m = re.search(r'\b([I]{0,3}[Vv]?\s*[A-Za-z]+\.?\s*\d+:\d+(?:-\d+)?)\b', rest)
            if sc_m and not scripture:
                scripture = sc_m.group(1).strip()
                is_header = True

        # Key signature
        key_m = re.search(r'\b(?:Doh\s+is|Key)\s+([A-G]\s*(?:b|#)?)', cur, re.IGNORECASE)
        if key_m:
            is_header = True
            raw_k = key_m.group(1).replace(' ', '')
            key_sig = f"Doh is {raw_k}"
            mt = re.search(r'\b(\d/\d|C)\b', cur)
            if mt:
                time_sig = mt.group(1)
            before = cur[:key_m.start()].strip()
            after = cur[key_m.end():].strip()
            if before and not re.match(r'^\d', before):
                author = before.split()[0] if before else ''
            if after and not re.match(r'^\d', after):
                composer = after

        # Scripture alone
        if not scripture:
            sc_alone = re.match(r'^\s*([I]{0,3}[Vv]?\s*[A-Za-z]+\.?\s*\d+:\d+(?:-\d+)?)\s*$', cur)
            if sc_alone:
                scripture = sc_alone.group(1).strip()
                is_header = True

        if not is_header:
            body_start = idx
            break
        else:
            body_start = idx + 1

    # Body
    body_lines = [l for l in cleaned[body_start:] if not is_footer_or_header_line(l)]

    # Parse verses & chorus
    cur_verse_num = None
    cur_verse_lines = []
    in_chorus = False

    for l in body_lines:
        if re.match(r'^(Khoras|Chorus)\b', l, re.IGNORECASE):
            if cur_verse_num is not None and cur_verse_lines:
                verses.append({'verseNumber': cur_verse_num, 'lines': cur_verse_lines})
                cur_verse_num = None
                cur_verse_lines = []
            in_chorus = True
            rest = re.sub(r'^(Khoras|Chorus)\s*:?\s*', '', l, flags=re.IGNORECASE).strip()
            if rest:
                chorus.append(rest)
            continue

        vm = re.match(r'^(\d+)\.\s*(.*)', l) or re.match(r'^(\d+)\s+([A-Z\u00dc\u00fc].*)', l)
        if vm:
            in_chorus = False
            if cur_verse_num is not None and cur_verse_lines:
                verses.append({'verseNumber': cur_verse_num, 'lines': cur_verse_lines})
            cur_verse_num = int(vm.group(1))
            cur_verse_lines = []
            if vm.group(2).strip():
                cur_verse_lines.append(vm.group(2).strip())
            continue

        if in_chorus:
            chorus.append(l)
        elif cur_verse_num is not None:
            cur_verse_lines.append(l)
        else:
            if not cur_verse_lines:
                cur_verse_num = 1
            cur_verse_lines.append(l)

    if cur_verse_num is not None and cur_verse_lines:
        verses.append({'verseNumber': cur_verse_num, 'lines': cur_verse_lines})

    if not verses and body_lines:
        verses.append({'verseNumber': 1, 'lines': body_lines})

    first_line = verses[0]['lines'][0].rstrip(',;.:') if verses and verses[0]['lines'] else ''
    primary_cat, all_cats = get_categories_for_id(h_id)
    pitch_hz, keynote = parse_key_pitch(key_sig)

    return {
        "id": h_id,
        "title": title,
        "englishTitle": eng_title,
        "firstLine": first_line,
        "timeSignature": time_sig,
        "keySignature": key_sig,
        "hymnBookRef": ref,
        "author": author,
        "composer": composer,
        "scripture": scripture,
        "category": primary_cat,
        "verses": verses,
        "chorus": chorus if chorus else None,
        "keynotePitchHz": pitch_hz,
        "keynoteName": keynote,
        "tune": eng_title,
        "meter": time_sig,
        "year": "",
        "categories": all_cats,
    }


def verse_text(verses):
    """Flatten all verse lines to a single comparable string."""
    if not verses:
        return ""
    parts = []
    for v in verses:
        parts.extend(v.get('lines', []))
    return '\n'.join(l.strip().lower() for l in parts if l.strip())


def chorus_text(chorus):
    if not chorus:
        return ""
    return '\n'.join(l.strip().lower() for l in chorus if l.strip())


# ── Main ──────────────────────────────────────────────────────────────────────
print("Extracting text from PDF ...")
all_halves = extract_half_pages(PDF_PATH)
print(f"   {len(all_halves)} half-pages read")

print("Segmenting hymns (range 1-500) ...")
raw_hymns = segment_all_hymns(all_halves)
# Remove 500 from broad parse — it bleeds trailing content; use dedicated parser instead
raw_hymns.pop(500, None)
print(f"   {len(raw_hymns)} hymns detected in PDF: IDs {sorted(raw_hymns.keys())}")

print("Parsing each hymn ...")
pdf_hymns = {}
for h_id, lines in raw_hymns.items():
    parsed = parse_hymn_from_raw(h_id, lines)
    if parsed:
        pdf_hymns[h_id] = parsed

# Hymn 500 – use the dedicated parser from the original script via the 220-500 segmenter
raw_500_map = _segment_hymns_220_500(all_halves)
if 500 in raw_500_map:
    parsed_500 = _parse_hymn_500(raw_500_map[500]['lines'])
    if parsed_500:
        pdf_hymns[500] = parsed_500

print(f"   {len(pdf_hymns)} hymns successfully parsed")

print("Loading existing hymns.json ...")
with open(JSON_PATH, 'r', encoding='utf-8') as f:
    existing_list = json.load(f)
existing = {h['id']: h for h in existing_list}
print(f"   {len(existing)} existing hymns loaded")

# ── Cross-check & merge ───────────────────────────────────────────────────────
report_lines = []
updated_count = 0
added_count = 0
skipped_count = 0

for h_id in sorted(pdf_hymns.keys()):
    pdf_h = pdf_hymns[h_id]
    pdf_v = verse_text(pdf_h.get('verses', []))
    pdf_c = chorus_text(pdf_h.get('chorus') or [])

    if h_id in existing:
        ex_h = existing[h_id]
        ex_v = verse_text(ex_h.get('verses', []))
        ex_c = chorus_text(ex_h.get('chorus') or [])

        pdf_line_count = sum(len(v.get('lines', [])) for v in pdf_h.get('verses', []))
        ex_line_count = sum(len(v.get('lines', [])) for v in ex_h.get('verses', []))

        changed = False

        # Only update verses/chorus if PDF has substantially more content
        # (at least 2 more lines) OR if existing is nearly empty
        if pdf_line_count > ex_line_count + 1 or ex_line_count <= 2:
            existing[h_id]['verses'] = pdf_h['verses']
            if pdf_h.get('chorus'):
                existing[h_id]['chorus'] = pdf_h['chorus']
            changed = True

        # Update missing metadata fields from PDF
        for field in ('title', 'englishTitle', 'timeSignature', 'keySignature',
                      'hymnBookRef', 'author', 'composer', 'scripture',
                      'keynotePitchHz', 'keynoteName', 'tune', 'meter'):
            pdf_val = pdf_h.get(field, '')
            ex_val = ex_h.get(field, '')
            if pdf_val and not ex_val:
                existing[h_id][field] = pdf_val
                changed = True

        if changed:
            updated_count += 1
            report_lines.append(
                f"UPDATED #{h_id} '{ex_h.get('title', '')}' — "
                f"lines: {ex_line_count} → {pdf_line_count}"
            )
        else:
            skipped_count += 1
    else:
        # New hymn not in existing JSON
        existing[h_id] = pdf_h
        added_count += 1
        report_lines.append(f"ADDED   #{h_id} '{pdf_h.get('title', '')}' (new entry)")

# ── Write report ──────────────────────────────────────────────────────────────
report_header = (
    f"Cross-Check Report\n"
    f"==================\n"
    f"PDF hymns found : {len(pdf_hymns)}\n"
    f"Existing hymns  : {len(existing)}\n"
    f"Updated         : {updated_count}\n"
    f"Added           : {added_count}\n"
    f"Unchanged       : {skipped_count}\n\n"
    f"Details:\n"
)
with open(REPORT_PATH, 'w', encoding='utf-8') as f:
    f.write(report_header + '\n'.join(report_lines))
print(f"\nReport written to {REPORT_PATH}")
print(f"   Updated: {updated_count}  |  Added: {added_count}  |  Unchanged: {skipped_count}")

# -- Write updated hymns.json --------------------------------------------------
merged_list = sorted(existing.values(), key=lambda h: h['id'])
with open(JSON_PATH, 'w', encoding='utf-8') as f:
    json.dump(merged_list, f, ensure_ascii=False, indent=2)
print(f"\nDone: hymns.json updated -- {len(merged_list)} total hymns")
