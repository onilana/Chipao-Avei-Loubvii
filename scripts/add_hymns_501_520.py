"""
add_hymns_501_520.py
--------------------
Adds hymns 501-520 from the PDF (the extra 20 hymns beyond the original 500)
and corrects hymn 500 to its actual title from the PDF.
"""
import json, os, sys

sys.path.insert(0, os.path.abspath(os.path.join(os.path.dirname(__file__), '..')))
from scripts.parse_full_hymns import get_categories_for_id, parse_key_pitch

JSON_PATH = os.path.abspath(
    os.path.join(os.path.dirname(__file__), '..', 'app', 'src', 'main', 'assets', 'hymns.json')
)

# Category for all extra hymns: Revival, Choruses & Social (continuation of 452-500)
def make_hymn(hid, title, eng_title, key_sig, time_sig, verses, chorus=None,
              author="", composer="", scripture="", ref=""):
    phz, kname = parse_key_pitch(key_sig)
    first_line = verses[0]["lines"][0].rstrip(",;.:") if verses and verses[0]["lines"] else ""
    return {
        "id": hid,
        "title": title,
        "englishTitle": eng_title,
        "firstLine": first_line,
        "timeSignature": time_sig,
        "keySignature": key_sig,
        "hymnBookRef": ref,
        "author": author,
        "composer": composer,
        "scripture": scripture,
        "category": "Revival, Choruses & Social",
        "verses": verses,
        "chorus": chorus,
        "keynotePitchHz": phz,
        "keynoteName": kname,
        "tune": eng_title,
        "meter": time_sig,
        "year": "",
        "categories": ["Revival, Choruses & Social"],
    }

EXTRA_HYMNS = [
    # ── Hymn 500 CORRECTED ─────────────────────────────────────────────────────
    make_hymn(
        500, "JIHOVAH BASHA", "Behold God's Creation Telling",
        "Doh is G", "4/4",
        verses=[
            {"verseNumber": 1, "lines": [
                "Zhukhaio, Jihovah basha khenou paowe,",
                "Soupeiyu Mapao kho-a bue ngali sou",
                "Duki nou ve ngaolaisa teidei ashee nasu,",
                "Na veishi ashee dakhailai."
            ]},
            {"verseNumber": 2, "lines": [
                "Zhukhaio, Jihovah basha khenou paowe,",
                "Kho-a bue Jihovah maiye popa loure;",
                "Jihovah nou deiphao hai soushee louyu thaisu,",
                "Asawo ne buh daikilai?"
            ]},
            {"verseNumber": 3, "lines": [
                "Zhukhaio, Jihovah basha khenou paowe,",
                "Teideiphao hai larü kaoli büa zhao bailai;",
                "Jihovah nou deiphao hai soushee louyu thaisu,",
                "Ne buh kaolu lousu homoe?"
            ]},
        ],
        chorus=[
            "He, deiphao vamai kaishu lourao",
            "Maiyu buh thaowu rao;",
            "Tave loulo ava buh hisu,",
            "Puh bahsha khenou pao-a bue."
        ]
    ),
    # ── Hymn 501 ───────────────────────────────────────────────────────────────
    make_hymn(
        501, "MAPAO E NEVEI BAIE", "Lord, I Am Thine",
        "Doh is G", "4/4",
        verses=[
            {"verseNumber": 1, "lines": [
                "E teithe tsu Mapao wo thaifü talai,",
                "Mapaowo E Nevei baie,",
                "Lududo nou E kao-a duta yuma,",
                "Mapaowo E Nevei bai."
            ]},
            {"verseNumber": 2, "lines": [
                "Mainou E ye saba-a malaiyuma,",
                "Mapaowo E nevei bai,",
                "Loulouzü E peiyu Puhye ngaolai,",
                "Mapaowo E Nevei bai."
            ]},
        ],
        chorus=[
            "Teikhai teirü shi vea E Jisu veie,",
            "Teishi teile vei vea E Jisu veie;",
            "Thaine-a E nevei souvu zülai,",
            "Mapaowo E Nevei bai."
        ]
    ),
    # ── Hymn 502 ───────────────────────────────────────────────────────────────
    make_hymn(
        502, "NASHEE LOUYU THAISU", "Great Change Since I Was Born",
        "Doh is G", "4/4",
        verses=[
            {"verseNumber": 1, "lines": [
                "Raiki souyu vah soupulai more,",
                "Raiki souyu vah soupulai more,",
                "Raiki souyu vah soupulai more,",
                "Nashee louthai Ahru kure."
            ]},
            {"verseNumber": 2, "lines": [
                "Raiki souyu lou soupulai more,",
                "Raiki souyu lou soupulai more,",
                "Raiki souyu lou soupulai more,",
                "Nashee louthai Ahru kure."
            ]},
            {"verseNumber": 3, "lines": [
                "Raiki paoyu chi paopulai more,",
                "Raiki paoyu chi paopulai more,",
                "Raiki paoyu chi paopulai more,",
                "Nashee louthai Ahru kure."
            ]},
            {"verseNumber": 4, "lines": [
                "Raiki tayu zhai tapulai more,",
                "Raiki tayu zhai tapulai more,",
                "Raiki tayu zhai tapulai more,",
                "Nashee louthai Ahru kure."
            ]},
            {"verseNumber": 5, "lines": [
                "Raiki veyu buh vepulai more,",
                "Raiki veyu buh vepulai more,",
                "Raiki veyu buh vepulai more,",
                "Nashee louthai Ahru kure."
            ]},
            {"verseNumber": 6, "lines": [
                "Raiki jüpana jüpalai more,",
                "Raiki jüpana jüpalai more,",
                "Raiki jüpana jüpalai more,",
                "Nashee louthai Ahru kure."
            ]},
            {"verseNumber": 7, "lines": [
                "Mapao Jisu Khrista dakhai velai,",
                "Mapao Jisu Khrista dakhai velai,",
                "Mapao Jisu Khrista dakhai velai,",
                "Südonou ne souda lourao."
            ]},
        ],
        chorus=[
            "Nashee louyu thaisu",
            "Nashee louyu thaisu",
            "Nashee louyu thaisu",
            "Paosha louho sayu ngaolou-e."
        ]
    ),
    # ── Hymn 503 ───────────────────────────────────────────────────────────────
    make_hymn(
        503, "REVIVAL SEIVEPEIO", "Send A Great Revival",
        "Doh is F", "4/4",
        verses=[
            {"verseNumber": 1, "lines": [
                "Ahi vepeio he! Jihovah,",
                "dao raichoki büa;",
                "Seipeio revival deiphaova,",
                "süsü kho-a bue."
            ]},
            {"verseNumber": 2, "lines": [
                "Meibuh loure rai pheipapeio,",
                "hashi hulai peio;",
                "Nevei khouko sü nou loupeio,",
                "Avei chemo hie."
            ]},
            {"verseNumber": 3, "lines": [
                "Rashi bah hinou vupapeio",
                "Amahmo khaipeio;",
                "Ahi cheche le pothou",
                "poa Neye tholai."
            ]},
        ],
        chorus=[
            "Mapao revival seivepeio,",
            "Revival seivepeio,",
            "revival seivepeio;",
            "Mapao revival seivepeio,",
            "Ava nou soushee peilo."
        ]
    ),
    # ── Hymn 504 ───────────────────────────────────────────────────────────────
    make_hymn(
        504, "SHEEYU NUH", "City Of Holiness",
        "Doh is G", "4/4",
        verses=[
            {"verseNumber": 1, "lines": [
                "Sana nou souyu mashee nuh ali bue,",
                "Khailulou mai thethe lousoue;",
                "Mapao Jihovah ye lou sousho-a buwe,",
                "Mapaowo vebuh hinou."
            ]},
            {"verseNumber": 2, "lines": [
                "Veayu deiphao haihi E sü veivewe,",
                "Avei sheeyu nuh vewe;",
                "Daizho Mapao nou teidei taolu,",
                "E-a ve hrünüe."
            ]},
        ],
        chorus=[
            "Oh! daizhozhou khupasa,",
            "Sühi veayu alia hae",
            "Oh! nuh avei sühisu,",
            "E-a ve hrünüe;",
            "Mapao kikhu houpeio,",
            "E sühi ve hrülai."
        ]
    ),
    # ── Hymn 505 ───────────────────────────────────────────────────────────────
    make_hymn(
        505, "MAPAO DAORE DONOU SOUDALO", "Prepare To Meet Thy God",
        "Doh is Eb", "4/4",
        verses=[
            {"verseNumber": 1, "lines": [
                "Ne Mapaoye sü ve daoloure donou, (3 times)"
            ]},
            {"verseNumber": 2, "lines": [
                "Jisu hai ludu, cheche, hriyu nasüe. (3 times)"
            ]},
            {"verseNumber": 3, "lines": [
                "Neletao Rako Mashi ngaolou homoe? (3 times)"
            ]},
        ],
        chorus=[
            "Ne zhou Puh pai chou louzü;",
            "Nele khouwoso (3 times)",
            "Ne Puh pai chou louzü;",
            "nele khouwoso."
        ]
    ),
    # ── Hymn 506 ───────────────────────────────────────────────────────────────
    make_hymn(
        506, "VULI E AZHI HI BUE", "I Was Once In Darkness",
        "Doh is G", "4/4",
        verses=[
            {"verseNumber": 1, "lines": [
                "Teile lihi E mah soubue,",
                "Chuo E Mapaoye sü ngaoloue;",
                "Deiphao mah buh hisü bupu laimoe,",
                "Athao tyali vedao Mapao chuo E velou",
                "Khaipeiyu Kalvari hinou ngaoloue."
            ]},
        ],
        chorus=[
            "Khaipei-o he! Mapao",
            "Mapao Nevei soure donou vewe;",
            "Soukhai touyu mah hrülai,",
            "Mapao hi peiwe Ahrühai."
        ]
    ),
    # ── Hymn 507 ───────────────────────────────────────────────────────────────
    make_hymn(
        507, "PUH NOU EYE SOUVEIPEIE", "He Healed Me",
        "Doh is G", "4/4",
        verses=[
            {"verseNumber": 1, "lines": ["Puh nou Eye souvei peie (3 times) Jisuno. (repeat)"]},
            {"verseNumber": 2, "lines": ["Puh nou Amah khailupeie (3 times) kros hinou."]},
            {"verseNumber": 3, "lines": ["E Puh ye lülou tea nou (3 times) E kuwe."]},
            {"verseNumber": 4, "lines": ["Puh teithe li khaivelai (3 times) Raki nou."]},
            {"verseNumber": 5, "lines": ["Südonou ne soudalo (3 times) Chu hinou."]},
            {"verseNumber": 6, "lines": ["Alinou ali leshi (3 times) Koa hrülai"]},
            {"verseNumber": 7, "lines": ["Bah tao-a lousou zholai (3 times) Rakinou."]},
            {"verseNumber": 8, "lines": ["Hallelujah sou hrülai, (3 times) Raki nou."]},
        ]
    ),
    # ── Hymn 508 ───────────────────────────────────────────────────────────────
    make_hymn(
        508, "JISU THA VELAIRE", "Jesus Is Coming Soon",
        "Doh is Db", "4/4",
        verses=[
            {"verseNumber": 1, "lines": [
                "Deiphaomai tsüle hi, piyu nou phoupfü bue,",
                "Phukhai touyu hinou pheipa peilai;",
                "Veayu khailu peilai, ne Jihovah tholo,",
                "Khristamai tashulo, cheche phoulo."
            ]},
        ],
        chorus=[
            "Jisu tha velaire,",
            "shona ho khei laisa,",
            "Maikhemai nou choulai shabe houyu;",
            "Thaiyu mai hrishulai,",
            "Cheche mai zhao talai,",
            "Sounou hayu rakisü khetalai."
        ]
    ),
    # ── Hymn 509 ───────────────────────────────────────────────────────────────
    make_hymn(
        509, "JISU NOU MAHMAI KUE", "Jesus Invites The Sinners",
        "Doh is Eb", "4/4",
        verses=[
            {"verseNumber": 1, "lines": [
                "Jisu kue mah khaipeire donou,",
                "Saitan vei kilai soupulaiwu?",
                "Leshishoa kuwe taveloulo."
            ]},
            {"verseNumber": 2, "lines": [
                "Jisu nou kuveyu phakisu,",
                "Ne ve loulouzü daizho salai;",
                "Sana nuh hi ve hrünü mowu?"
            ]},
        ],
        chorus=[
            "Le lilouyu mai Jisu nou leshie,",
            "Chunou lelilo, teile hare;",
            "Jisu nou paoyu sü vetedo."
        ]
    ),
    # ── Hymn 510 ───────────────────────────────────────────────────────────────
    make_hymn(
        510, "JISU ZHIH HINOU VEILOURE", "Victory In Jesus",
        "Doh is Ab", "4/4",
        verses=[
            {"verseNumber": 1, "lines": [
                "E nou ko aveili süe, Mapao Jisu nou veilouyu,",
                "Adonou Puh sü Kalvari hinou ve thaipeire;",
                "Mahmo khailupeire zhe, Kalvari hinou küpapeie,",
                "Zhe süna vanou Amahmo khailou-a veiloure."
            ]},
            {"verseNumber": 2, "lines": [
                "E nou ko aveili süe Puh Kho-a zhe peiyu,",
                "Teithe ahrühai veayu khos hinou veiloure;",
                "Khü mashee peiyu zhe vanou,",
                "E panou veiloure."
            ]},
        ],
        chorus=[
            "Mapao Jisu zhih hinou,",
            "E teisa veiloure,",
            "Puh zhe sünou Eye phouloue;",
            "Alia E hrüloure,",
            "Puh E leshi pei vanou;",
            "Ahrühai Puh vei soure,",
            "E panou veiloure."
        ]
    ),
    # ── Hymn 511 ───────────────────────────────────────────────────────────────
    make_hymn(
        511, "SHULO ARAKO", "The Call For Rapture",
        "Doh is F", "4/4",
        verses=[
            {"verseNumber": 1, "lines": [
                "Tashulo Arako, tashulo mahmai ne,",
                "Nenou zhi-abu Mapao sü;",
                "Teikhai nou sapo teihao nou züpo",
                "Vekoure ne tashu sülourao."
            ]},
        ],
        chorus=[
            "Arako tashulo,",
            "Arako kaishulo,",
            "Ne zhi-anou bulai hiwu?",
            "Soulou shie Arako, soulou hoe mahmai ne,",
            "Zhou ne tashu Mapao sülourao."
        ]
    ),
    # ── Hymn 512 ───────────────────────────────────────────────────────────────
    make_hymn(
        512, "NE NASHU AHAI KHOLO", "Ye Must Be Born Again",
        "Doh is G", "4/4",
        verses=[
            {"verseNumber": 1, "lines": [
                "Nai teitheli kheiko phahi Nikodimas Jisu hi ve;",
                "Jisunou puh hisü paopeie",
                '"Ne nashee ahai kholo"'
            ]},
            {"verseNumber": 2, "lines": [
                "Khaiteithe teilesü vere,",
                "Nehrü teingheisü loutalai;",
                "Leshi samai louche lourao",
                "Nashee kulo ahai."
            ]},
        ],
        chorus=[
            "Ne nashee ahai kholo",
            "Raki teidei venülouzü nashee kulo;",
            "Ne nashee ahai kholo",
            "Raki teidei ngaore donou nashee kulo."
        ]
    ),
    # ── Hymn 513 ───────────────────────────────────────────────────────────────
    make_hymn(
        513, "AVEI AHRU HISUNOU", "In My Life",
        "Doh is Ab", "4/4",
        verses=[
            {"verseNumber": 1, "lines": [
                "Puh zhuhai sheetou",
                "Leshi shoyu zhusünou, (2 times)",
                "Eye tave saloulai,",
                "Deiphao mah buh hisünou."
            ]},
        ],
        chorus=[
            "Avei Ahru hisünou,",
            "Jisuye vedao pulai,",
            "Puhnou dakhai velouzü,",
            "Puhye vedao vuzülai",
            "Avei Ahru hisü nou."
        ]
    ),
    # ── Hymn 514 ───────────────────────────────────────────────────────────────
    make_hymn(
        514, "AVEI TEIDEI SU RAKI MOWU?", "Is Heaven Not My Home?",
        "Doh is F", "4/4",
        verses=[
            {"verseNumber": 1, "lines": [
                "Avei teidei sü raki mowu? E talai,",
                "Avei teidei sü raki mowu",
                "Avei teidei sü raki mowu",
                "Buhsühi, buh sühi Etalai. (3 times)"
            ]},
            {"verseNumber": 2, "lines": [
                "Kainaan teidei veishoyu buh hi. E talai,",
                "Kainaan teidei veishoyu buh hi.",
                "Kainaan teidei veishoyu buh hi.",
                "Buhsühi, buh sühi Etalai. (3 times)"
            ]},
            {"verseNumber": 3, "lines": [
                "Samaitaore netanü mowu?",
                "Buhsühi, buh sühi Etalai. (3 times)"
            ]},
        ]
    ),
    # ── Hymn 515 ───────────────────────────────────────────────────────────────
    make_hymn(
        515, "KALVARI HOSPITAL", "Calvary Hospital",
        "Doh is F", "4/4",
        verses=[
            {"verseNumber": 1, "lines": [
                "Deiphao maiye donou",
                "Sou veilaiyu mai donou",
                "Sounou sou-a bumai",
                "Deiphao pou-apeimai louzü."
            ]},
        ],
        chorus=[
            "Rako hilonou sou",
            "Kalvari Hospital hi",
            "Kalvari Hospital molouzü",
            "Veiloure buh kukhe-a bumai,",
            "Talaikhe lia hae."
        ]
    ),
    # ── Hymn 516 ───────────────────────────────────────────────────────────────
    make_hymn(
        516, "KHAIVE ZHIO", "Kum Ba Yah",
        "Doh is D", "3/4",
        verses=[
            {"verseNumber": 1, "lines": [
                "Khaive zhio Mapao,",
                "khaive zhio (3 times)",
                "Mapao wo khaive zhio."
            ]},
            {"verseNumber": 2, "lines": [
                "Kamai kra-a bue,",
                "khaive zhio. (3 times)",
                "Mapao wo khaive zhio."
            ]},
            {"verseNumber": 3, "lines": [
                "Kamai lousou bue,",
                "khaive zhio. (3 times)",
                "Mapao wo khaive zhio."
            ]},
            {"verseNumber": 4, "lines": [
                "Kamai veachi paoe,",
                "khaive zhio. (3 times)",
                "Mapao wo khaive zhio."
            ]},
            {"verseNumber": 5, "lines": [
                "Kum Ba Yah Mapao,",
                "Kum Ba Yah.",
                "Mapao wo khaive zhio. (3 times)"
            ]},
        ]
    ),
    # ── Hymn 517 ───────────────────────────────────────────────────────────────
    make_hymn(
        517, "TAKHAIRE, TAKHAIRE", "Rolled Away",
        "Doh is Bb", "4/4",
        verses=[
            {"verseNumber": 1, "lines": [
                "Takhaire, takhaire",
                "Amahmo teiteisü takhaire",
                "Amahmo teitei sü,",
                "Jisu zhe nou khüpeie",
                "Hallelujah takhaire",
                "Takhaire, takhaire;",
                "Amahmo teitei sü takhaihere."
            ]},
        ]
    ),
    # ── Hymn 518 ───────────────────────────────────────────────────────────────
    make_hymn(
        518, "REVIVAL SEIVEPEIO (II)", "Send A Great Revival (II)",
        "Doh is F", "4/4",
        verses=[
            {"verseNumber": 1, "lines": [
                "Ahi vepeio he! Jihovah,",
                "dao raichoki büa;",
                "Seipeio revival deiphaova,",
                "süsü kho-a bue."
            ]},
            {"verseNumber": 2, "lines": [
                "Meibuh loure rai pheipapeio,",
                "hashi hulai peio;",
                "Nevei khouko sü nou loupeio,",
                "Avei chemo hie."
            ]},
            {"verseNumber": 3, "lines": [
                "Rashi bah hinou vupapeio",
                "Amahmo khaipeio;",
                "Ahi cheche le pothou",
                "poa Neye tholai."
            ]},
        ],
        chorus=[
            "Mapao revival seivepeio,",
            "Revival seivepeio,",
            "revival seivepeio;",
            "Mapao revival seivepeio,",
            "Ava nou soushee peilo."
        ]
    ),
    # ── Hymn 519 ───────────────────────────────────────────────────────────────
    make_hymn(
        519, "E NOU LESHIYU TEIDEI", "The Land I Love",
        "Doh is A", "4/4",
        verses=[
            {"verseNumber": 1, "lines": [
                "Daorainou Ramainou soupeiyu Ateidei,",
                "Teidei haina kohai Enou kusha lou hoe;",
                "Teikhai ne taho teidei haina ko doupeio,",
                "Leshishoe, ngailouhoe, eh Poumai teideihai"
            ]},
        ],
        chorus=[
            "Deiphaohi Enou tasühao-a zhuphaki,",
            "Eh Poumai teidei haina bü-a ngaomohie;",
            "Reiko vaosüü, ngaire, kheko vourei kübue,",
            "Poupahnou sühao bue, Poumai teidei hisu."
        ]
    ),
    # ── Hymn 520 ───────────────────────────────────────────────────────────────
    make_hymn(
        520, "HALLELUJAH CHORUS", "Hallelujah Chorus",
        "Doh is D", "4/4",
        composer="C.F. Handel",
        scripture="Rev. 19:6",
        verses=[
            {"verseNumber": 1, "lines": [
                "Hallelujah, Hallelujah, Hallelujah, Hallelujah, Hallelujah,",
                "Hallelujah, Hallelujah, Hallelujah, Hallelujah, Hallelujah!",
                "Mapao nou tei sa khai ha yu Veo siie,",
                "Hallelujah, Hallelujah, Hallelujah, Hallelujah.",
                "Mapao nou tei sa khai ha yu Veo siie,",
                "Hallelujah, Hallelujah, Hallelujah, Hallelujah.",
                "Mapao nou tei sa khai ha yu Veo siie,",
                "Hallelujah, Hallelujah, Hallelujah, Hallelujah,",
                "Hallelujah!",
                "For the Lord God Omnipotent reigneth,",
                "The kingdom of this world is become",
                "The Kingdom of our Lord and of His Christ,",
                "And He shall reign for ever and ever,",
                "King of kings! and Lord of lords!",
                "Hallelujah, Hallelujah, Hallelujah, Hallelujah, Hallelujah!"
            ]}
        ]
    ),
]

# ── Load existing hymns and merge ─────────────────────────────────────────────
with open(JSON_PATH, 'r', encoding='utf-8') as f:
    hymns_list = json.load(f)

hymns_map = {h['id']: h for h in hymns_list}

added = 0
updated = 0
for hymn in EXTRA_HYMNS:
    hid = hymn['id']
    if hid in hymns_map:
        ex_lines = sum(len(v.get('lines', [])) for v in hymns_map[hid].get('verses', []))
        new_lines = sum(len(v.get('lines', [])) for v in hymn.get('verses', []))
        hymns_map[hid] = hymn
        updated += 1
        print(f"Updated  #{hid}: '{hymn['title']}' ({ex_lines} -> {new_lines} lines)")
    else:
        hymns_map[hid] = hymn
        added += 1
        print(f"Added    #{hid}: '{hymn['title']}'")

final = sorted(hymns_map.values(), key=lambda h: h['id'])
with open(JSON_PATH, 'w', encoding='utf-8') as f:
    json.dump(final, f, ensure_ascii=False, indent=2)

print(f"\nAdded: {added}  Updated: {updated}")
print(f"Total hymns in hymns.json: {len(final)}")
