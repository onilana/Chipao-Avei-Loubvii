"""Audit and clean up recovered hymns with empty titles or thin verses."""
import json
import re
import sys
import os

sys.path.insert(0, os.path.abspath(os.path.join(os.path.dirname(__file__), '..')))
from scripts.parse_full_hymns import extract_half_pages, get_categories_for_id, parse_key_pitch

PDF_PATH = (
    r"C:\Users\MEC-Oni\.gemini\antigravity\brain"
    r"\e2e67671-24b4-4903-8fe7-8645ee02122a\.user_uploaded"
    r"\media_1793689295416_353bfee4.pdf"
)
JSON_PATH = os.path.abspath(
    os.path.join(os.path.dirname(__file__), '..', 'app', 'src', 'main', 'assets', 'hymns.json')
)

# Manually inspect raw text around missing-title hymns
INSPECT_IDS = [165, 224, 233, 472]

print("Extracting PDF ...")
all_halves = extract_half_pages(PDF_PATH)
flat_lines = []
for _pg, _side, lines in all_halves:
    flat_lines.extend(lines)

print(f"Total lines: {len(flat_lines)}")

for hid in INSPECT_IDS:
    # Find first occurrence of this number
    pattern = re.compile(r'^\s*' + str(hid) + r'[\.\s]')
    for i, l in enumerate(flat_lines):
        if pattern.match(l.strip()) or l.strip() == str(hid):
            print(f"\n=== Hymn {hid} at line {i} ===")
            for j in range(max(0, i-2), min(len(flat_lines), i+30)):
                print(f"  [{j}] {repr(flat_lines[j])}")
            break
