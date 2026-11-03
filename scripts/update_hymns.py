"""Script to update the hymns JSON asset using the latest hymnal PDF.
It parses the PDF, extracts hymn data, merges with the existing `hymns.json`
and writes the updated file.
"""
import json
import os
import sys

# Add project root to sys.path to import parse_full_hymns
PROJECT_ROOT = os.path.abspath(os.path.join(os.path.dirname(__file__), '..'))
if PROJECT_ROOT not in sys.path:
    sys.path.insert(0, PROJECT_ROOT)

try:
    from scripts.parse_full_hymns import extract_half_pages, segment_hymns, parse_full_hymn
except Exception as e:
    print(f"Failed to import parsing helpers: {e}")
    sys.exit(1)

# Path to the latest hymnal PDF (uploaded by the user)
PDF_PATH = r"C:\\Users\\MEC-Oni\\.gemini\\antigravity\\brain\\e2e67671-24b4-4903-8fe7-8645ee02122a\\.user_uploaded\\media_1793688912458_afa5d02a.pdf"

# Path to existing hymns JSON
JSON_PATH = os.path.abspath(os.path.join(PROJECT_ROOT, 'app', 'src', 'main', 'assets', 'hymns.json'))

# ---------------------------------------------------------------------------
# Helper to load existing hymns
# ---------------------------------------------------------------------------
def load_existing_hymns(path):
    with open(path, 'r', encoding='utf-8') as f:
        data = json.load(f)
    # Build a dict keyed by id for easy merging
    return {h['id']: h for h in data}

# ---------------------------------------------------------------------------
# Extract hymns from the PDF
# ---------------------------------------------------------------------------
print('Extracting halves from PDF...')
all_halves = extract_half_pages(PDF_PATH)
print(f'Extracted {len(all_halves)} halves')

print('Segmenting hymns...')
raw_hymns = segment_hymns(all_halves)
print(f'Found {len(raw_hymns)} raw hymn entries')

# Parse each hymn into a structured dict
parsed_hymns = {}
for h_id, raw in raw_hymns.items():
    parsed = parse_full_hymn(h_id, raw)
    if parsed is None:
        continue
    parsed_hymns[h_id] = parsed

print(f'Parsed {len(parsed_hymns)} hymns')

# ---------------------------------------------------------------------------
# Merge with existing hymns
# ---------------------------------------------------------------------------
existing = load_existing_hymns(JSON_PATH)
merged = []
max_id = max(existing.keys()) if existing else 0

for h_id, hymn in parsed_hymns.items():
    if h_id in existing:
        # Replace the existing entry with the freshly parsed data
        merged.append(hymn)
        existing.pop(h_id)
    else:
        # New hymn – ensure a unique id (use the parsed id if it's > max_id)
        new_id = h_id if h_id > max_id else max_id + 1
        hymn['id'] = new_id
        merged.append(hymn)
        max_id = max(max_id, new_id)

# Append any remaining hymns that were not present in the PDF (preserve them)
merged.extend(existing.values())

# Sort by id for readability
merged.sort(key=lambda x: x['id'])

# ---------------------------------------------------------------------------
# Write back to hymns.json
# ---------------------------------------------------------------------------
with open(JSON_PATH, 'w', encoding='utf-8') as f:
    json.dump(merged, f, ensure_ascii=False, indent=2)

print(f'Updated hymns.json written to {JSON_PATH} with {len(merged)} entries')
