"""Scan PDF for hymns beyond ID 500 (the extra 20 hymns)."""
import re
import sys
import os

sys.path.insert(0, os.path.abspath(os.path.join(os.path.dirname(__file__), '..')))
from scripts.parse_full_hymns import extract_half_pages

PDF_PATH = (
    r"C:\Users\MEC-Oni\.gemini\antigravity\brain"
    r"\e2e67671-24b4-4903-8fe7-8645ee02122a\.user_uploaded"
    r"\media_1793689295416_353bfee4.pdf"
)

print("Extracting PDF ...")
all_halves = extract_half_pages(PDF_PATH)
flat_lines = []
for pg, side, lines in all_halves:
    for l in lines:
        flat_lines.append((pg, side, l))

total = len(flat_lines)
print(f"Total raw lines: {total}")

# Scan for hymn numbers 501-600 or any special section after 500
print("\nSearching for numbers > 500 or section headers after hymn 500 ...")

found_500 = False
after_500_lines = []

for i, (pg, side, line) in enumerate(flat_lines):
    s = line.strip()

    # Track when we pass hymn 500
    if re.match(r'^500[\.\s]', s) or s == '500':
        found_500 = True

    if found_500:
        after_500_lines.append((i, pg, side, s))

    # Look for any 3-digit number 501-599
    m = re.match(r'^(5[0-9]{2}|[6-9]\d\d|\d{3})[\.\s]', s)
    if m:
        num = int(m.group(1))
        if 501 <= num <= 600:
            context_start = max(0, i - 2)
            context_end = min(total, i + 20)
            print(f"\n>>> Found #{num} at line {i} (page {pg}{side}):")
            for j in range(context_start, context_end):
                print(f"  [{j}] {repr(flat_lines[j][2])}")

# Print first 200 lines after hymn 500
print(f"\n\n=== First 200 lines after hymn 500 ===")
for i, pg, side, s in after_500_lines[:200]:
    print(f"  [line {i}, p{pg}{side}] {repr(s)}")
