"""Fix hymn 500 (Halleluyah Chorus) which got bloated with trailing content."""
import json
import sys
import os

sys.path.insert(0, os.path.abspath(os.path.join(os.path.dirname(__file__), '..')))
from scripts.parse_full_hymns import extract_half_pages, segment_hymns, parse_hymn_500

PDF_PATH = (
    r"C:\Users\MEC-Oni\.gemini\antigravity\brain"
    r"\e2e67671-24b4-4903-8fe7-8645ee02122a\.user_uploaded"
    r"\media_1793689295416_353bfee4.pdf"
)
JSON_PATH = os.path.abspath(
    os.path.join(os.path.dirname(__file__), '..', 'app', 'src', 'main', 'assets', 'hymns.json')
)

# Hymn 500 is the Halleluyah Chorus — hardcode correct data
HYMN_500 = {
    "id": 500,
    "title": "HALLELUYAH CHORUS",
    "englishTitle": "Hallelujah Chorus",
    "firstLine": "Hallelujah, Hallelujah, Hallelujah",
    "timeSignature": "4/4",
    "keySignature": "Doh is D",
    "hymnBookRef": "",
    "author": "",
    "composer": "C.F. Handel",
    "scripture": "Rev. 19:6",
    "category": "Revival, Choruses & Social",
    "verses": [
        {
            "verseNumber": 1,
            "lines": [
                "Hallelujah, Hallelujah, Hallelujah, Hallelujah, Hallelujah!",
                "Hallelujah, Hallelujah, Hallelujah, Hallelujah, Hallelujah!",
                "For the Lord God Omnipotent reigneth,",
                "Hallelujah, Hallelujah, Hallelujah, Hallelujah!",
                "For the Lord God Omnipotent reigneth,",
                "Hallelujah, Hallelujah, Hallelujah, Hallelujah!",
                "For the Lord God Omnipotent reigneth,",
                "Hallelujah, Hallelujah, Hallelujah, Hallelujah, Hallelujah, Hallelujah!",
                "The kingdom of this world is become",
                "The Kingdom of our Lord and of His Christ,",
                "And He shall reign for ever and ever,",
                "And He shall reign for ever and ever,",
                "King of kings! (for ever and ever, Hallelujah, Hallelujah!)",
                "And Lord of lords! (for ever and ever, Hallelujah, Hallelujah!)",
                "King of kings! (for ever and ever, Hallelujah, Hallelujah!)",
                "And Lord of lords! (for ever and ever, Hallelujah, Hallelujah!)",
                "King of kings and Lord of lords!",
                "And He shall reign for ever and ever,",
                "King of kings! and Lord of lords!",
                "And He shall reign for ever and ever,",
                "For ever and ever, for ever and ever,",
                "King of kings! and Lord of lords!",
                "Hallelujah, Hallelujah, Hallelujah, Hallelujah, Hallelujah!"
            ]
        }
    ],
    "chorus": None,
    "keynotePitchHz": 293.66,
    "keynoteName": "D",
    "tune": "Hallelujah Chorus",
    "meter": "4/4",
    "year": "",
    "categories": ["Revival, Choruses & Social"]
}

with open(JSON_PATH, 'r', encoding='utf-8') as f:
    hymns = json.load(f)

# Replace hymn 500
for i, h in enumerate(hymns):
    if h['id'] == 500:
        hymns[i] = HYMN_500
        break

with open(JSON_PATH, 'w', encoding='utf-8') as f:
    json.dump(hymns, f, ensure_ascii=False, indent=2)

h500 = next(x for x in hymns if x['id'] == 500)
lcount = sum(len(v.get('lines', [])) for v in h500.get('verses', []))
print(f"Hymn 500 fixed: {lcount} lines")
print("hymns.json saved successfully.")
