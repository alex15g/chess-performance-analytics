from pathlib import Path
import re

# Fisierul original si fisierul rezultat
INPUT_FILE = Path("data/games.pgn")
OUTPUT_FILE = Path("data/games_anonymized.pgn")

# Numele tau, care trebuie pastrat
MY_NAME = "Gavris,Alexandru Catalin"


def main():
    # Verificam daca fisierul original exista
    if not INPUT_FILE.exists():
        raise FileNotFoundError(
            f"Nu gasesc fisierul {INPUT_FILE}. "
            "Pune PGN-ul original in folderul data "
            "si numeste-l games.pgn."
        )

    # Citim toate partidele din fisier
    pgn_text = INPUT_FILE.read_text(encoding="utf-8-sig")

    # Extragem numele jucatorilor din antetele PGN
    player_names = re.findall(
        r'^\[(?:White|Black) "([^"]*)"\]',
        pgn_text,
        re.MULTILINE
    )

    # Pastram doar adversarii unici, in ordinea aparitiei
    opponents = []

    for name in player_names:
        if name != MY_NAME and name not in opponents:
            opponents.append(name)

    # Asociem fiecarui adversar un ID stabil
    mapping = {
        name: f"Opponent_{i:03d}"
        for i, name in enumerate(opponents, start=1)
    }

    # Inlocuim numele adversarilor in antetele PGN
    for old_name, opponent_id in mapping.items():
        pattern = (
            r'^\[(White|Black) "'
            + re.escape(old_name)
            + r'"\]$'
        )

        pgn_text = re.sub(
            pattern,
            lambda match: (
                f'[{match.group(1)} "{opponent_id}"]'
            ),
            pgn_text,
            flags=re.MULTILINE
        )

    # Cream folderul de iesire, daca nu exista
    OUTPUT_FILE.parent.mkdir(
        parents=True,
        exist_ok=True
    )

    # Salvam copia anonimizata
    OUTPUT_FILE.write_text(
        pgn_text,
        encoding="utf-8"
    )

    # Verificam numarul de partide procesate
    game_count = len(
        re.findall(r'^\[Event ', pgn_text, re.MULTILINE)
    )

    print(f"Partide procesate: {game_count}")
    print(f"Adversari unici anonimizati: {len(mapping)}")
    print(f"Fisier rezultat: {OUTPUT_FILE}")


if __name__ == "__main__":
    main()