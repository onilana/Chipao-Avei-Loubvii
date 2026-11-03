"""Patch the 4 hymns that have empty/broken titles and fix their verse structures."""
import json
import re
import os
import sys

sys.path.insert(0, os.path.abspath(os.path.join(os.path.dirname(__file__), '..')))

JSON_PATH = os.path.abspath(
    os.path.join(os.path.dirname(__file__), '..', 'app', 'src', 'main', 'assets', 'hymns.json')
)

PATCHES = {
    165: {
        "title": "KHAI APEIO NGAOLOUTHAI",
        "englishTitle": "Since I Found My Saviour",
        "keySignature": "Doh is Ab",
        "verses": [
            {"verseNumber": 1, "lines": [
                "Ahrü hai tyaku shoa zhingaoe,",
                "Khai-apeio ngaolouthai,",
                "Puh zhuvea kross hinou zhingaoe,",
                "Eye leshi khai-apeio."
            ]},
            {"verseNumber": 2, "lines": [
                "Puh leshi nou Eye phouloue",
                "E khai-apeio ngaoloue,",
                "Khailu louyu Puh nou peiwe,",
                "Avei Mapao khai-apeio."
            ]},
            {"verseNumber": 3, "lines": [
                "Teihao nou chaovelouyu ma,",
                "Khai-apeio ngaolouthai,",
                "Puh etao leshi phaive loure,",
                "Avei Mapao khai-apeio."
            ]}
        ],
        "chorus": [
            "Lekiyu, piyu hare,",
            "Jisu nou teiva peiwe",
            "Raki hai dousho mobue,",
            "Khai-apeio ngaolouthai"
        ],
        "firstLine": "Ahru hai tyaku shoa zhingaoe"
    },
    224: {
        "title": "NE DUSU MA-A TARE",
        "englishTitle": "Drifting Down",
        "keySignature": "Doh is Ab",
        "verses": [
            {"verseNumber": 1, "lines": [
                "Ne dusu ma-a tare,",
                "ne dusu ma-a tare,",
                "Dushi losu nenou tala kou-are;",
                "Ne du tama lelouzü,",
                "Dakhai vete holaire;",
                "Dushi hilosu, ne nou tala koure."
            ]},
            {"verseNumber": 2, "lines": [
                "Kilo buyu mainou ne kuwe khaive lou tea nou,",
                "Ne ludu shilo su takoupu khaiso;",
                "Ne tala kouyu louzü dakhaive louho laido,",
                "Südonou ne aku hilo tapuso."
            ]}
        ],
        "chorus": [
            "Ne ludusü ma",
            "Raishu hrolü taloure;",
            "Ne tala louzü,",
            "Ne Jihovah leshi hai,",
            "Tachopa louyu laire;",
            "Ne tala khaiso,",
            "ludu kaomo hisuü."
        ],
        "firstLine": "Ne dusu ma-a tare"
    },
    233: {
        "title": "ETAO LOU SOUPULAIKHE",
        "englishTitle": "Let Us Sing Again",
        "keySignature": "Doh is A",
        "verses": [
            {"verseNumber": 1, "lines": [
                "Etao Mapao shoyu lousou pulaikhe,",
                "Eh Puo leshi süre donou Puh thaipeie;",
                "Deiphao maihi Puh leshi eh paolaikhe,",
                "Puh dai büa ekhemai donou la peisa?"
            ]},
            {"verseNumber": 2, "lines": [
                "Souvapei donou etao Puh sholaikhe,",
                "Puhnou ahri chipao etaohi khaipeie,",
                "Puh louche loumai hi Puh ngheitei tea bue,",
                "Puh zhuvea lüloumai hi pongao peilai."
            ]}
        ],
        "chorus": [
            "Khupa ko etao paolaikhe,",
            "Puh dai büa kros hinou thaie;",
            "Sasho-a paoshoyu Puh vei sou peilai,",
            "Etao do soure donou Puh nou vea peie."
        ],
        "firstLine": "Etao Mapao shoyu lousou pulaikhe"
    },
    472: {
        "title": "KHRISTA, HAI VUPAPEIYU BEBEIE",
        "englishTitle": "Christ, The Source Of Deliverance",
        "keySignature": "Doh is E",
        "verses": [
            {"verseNumber": 1, "lines": [
                "Deiphao azhi buphaki,",
                "Chechemo tho-a buphaki,",
                "Vei thoutai vuthou lou-a,",
                "Paopa rahshi shee loure.",
                "Khrista pei aveih hinou rah."
            ]},
            {"verseNumber": 2, "lines": [
                "Rahshi vei-a buh nou,",
                "Pfe deh pfetho palou me khemai lou-a,",
                "Puh ye paosholaikhe,",
                "Puh hii khaive louwe."
            ]}
        ],
        "chorus": [
            "Khrista hai vupa bebeie,",
            "Ehkhemai nou vupa loure,",
            "Hrii kutare Ehkhemai asala,",
            "Mai hriilai peiyu Puh Chuwo asalo."
        ],
        "firstLine": "Deiphao azhi buphaki"
    }
}

with open(JSON_PATH, 'r', encoding='utf-8') as f:
    hymns = json.load(f)

patched = 0
for h in hymns:
    hid = h['id']
    if hid in PATCHES:
        patch = PATCHES[hid]
        for key, val in patch.items():
            h[key] = val
        # Ensure firstLine is set from verse 1 line 1
        if h.get('verses') and h['verses'][0].get('lines'):
            h['firstLine'] = h['verses'][0]['lines'][0].rstrip(',;.:')
        # Set category
        from scripts.parse_full_hymns import get_categories_for_id, parse_key_pitch
        pcat, acats = get_categories_for_id(hid)
        h['category'] = pcat
        h['categories'] = acats
        if h.get('keySignature'):
            phz, kname = parse_key_pitch(h['keySignature'])
            h['keynotePitchHz'] = phz
            h['keynoteName'] = kname
        patched += 1
        print(f"Patched #{hid}: '{h['title']}'")

with open(JSON_PATH, 'w', encoding='utf-8') as f:
    json.dump(hymns, f, ensure_ascii=False, indent=2)

print(f"\nPatched {patched} hymns. Total: {len(hymns)} hymns in hymns.json")
