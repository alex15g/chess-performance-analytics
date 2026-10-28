from pathlib import Path
from datetime import datetime

import chess
import chess.pgn
import pandas as pd


# Identificam folderul principal al proiectului
PROJECT_ROOT = Path(__file__).resolve().parents[1]

# Fisierul PGN anonimizat si CSV-ul rezultat
INPUT_FILE = PROJECT_ROOT / "data" / "games_anonymized.pgn"
OUTPUT_FILE = PROJECT_ROOT / "data" / "games.csv"

# Numele meu
MY_NAME = "Gavris,Alexandru Catalin"


def parse_date(date_string):
    """Transforma data PGN in formatul YYYY-MM-DD."""
    try:
        return datetime.strptime(
            date_string, "%Y.%m.%d"
        ).date().isoformat()
    except (ValueError, TypeError):
        return None


def get_rating(value):
    """Converteste ratingul in numar intreg."""
    try:
        return int(value)
    except (ValueError, TypeError):
        return None


def get_opening(game):
    """
    Foloseste numele deschiderii din PGN, daca exista.
    In caz contrar, returneaza primele 4 mutari individuale.
    """
    opening = game.headers.get("Opening", "").strip()

    if opening and opening != "?":
        return opening

    board = game.board()
    moves = []

    for move in game.mainline_moves():
        san = board.san(move)

        if board.turn == chess.WHITE:
            moves.append(f"{board.fullmove_number}. {san}")
        else:
            moves.append(san)

        board.push(move)

        # Primele 4 mutari individuale
        if len(moves) == 4:
            break

    return " ".join(moves) if moves else "Unknown"


def get_player_data(headers):
    """Determina culoarea noastra si ratingurile jucatorilor."""

    white = headers.get("White", "").strip()
    black = headers.get("Black", "").strip()

    if white.casefold() == MY_NAME.casefold():
        return {
            "Color": "White",
            "OpponentID": black,
            "PlayerRating": get_rating(
                headers.get("WhiteElo")
            ),
            "OpponentRating": get_rating(
                headers.get("BlackElo")
            ),
        }

    if black.casefold() == MY_NAME.casefold():
        return {
            "Color": "Black",
            "OpponentID": white,
            "PlayerRating": get_rating(
                headers.get("BlackElo")
            ),
            "OpponentRating": get_rating(
                headers.get("WhiteElo")
            ),
        }

    raise ValueError(
        "Nu am putut identifica jucatorul nostru "
        "intr-o partida."
    )


def get_score(result, color):
    """
    Calculeaza rezultatul din perspectiva noastra:
    victorie = 1, remiza = 0.5, infrangere = 0.
    """

    if result == "1/2-1/2":
        return 0.5

    if result == "1-0":
        return 1 if color == "White" else 0

    if result == "0-1":
        return 0 if color == "White" else 1

    raise ValueError(
        f"Rezultat PGN necunoscut: {result}"
    )


def get_outcome(score):
    """Converteste scorul in victorie, remiza sau infrangere."""

    if score == 1:
        return "Win"

    if score == 0.5:
        return "Draw"

    return "Loss"


def main():
    # Verificam existenta fisierului de intrare
    if not INPUT_FILE.exists():
        raise FileNotFoundError(
            f"Nu gasesc fisierul: {INPUT_FILE}"
        )

    games_data = []

    with INPUT_FILE.open(
        "r", encoding="utf-8-sig"
    ) as pgn_file:

        game_number = 0

        while True:
            game = chess.pgn.read_game(pgn_file)

            if game is None:
                break

            game_number += 1

            # Oprim procesarea daca parserul raporteaza erori
            if game.errors:
                raise ValueError(
                    f"Erori la citirea partidei {game_number}: "
                    f"{game.errors}"
                )

            headers = game.headers

            # Determinam culoarea si ratingurile
            player_data = get_player_data(headers)

            opponent_id = player_data["OpponentID"]

            # Nu permitem exportarea numelui real
            if not opponent_id.startswith("Opponent_"):
                raise ValueError(
                    f"Partida {game_number} contine un adversar "
                    "care nu pare anonimizat. "
                    "Verifica fisierul PGN."
                )

            # Calculam scorul din perspectiva noastra
            score = get_score(
                headers.get("Result"),
                player_data["Color"]
            )

            # Salvam informatiile extrase
            games_data.append({
                "GameID": game_number,
                "Date": parse_date(
                    headers.get("Date", "")
                ),
                "Tournament": headers.get(
                    "Event", "Unknown"
                ),
                "Site": headers.get(
                    "Site", "Unknown"
                ),
                "Round": headers.get(
                    "Round", "Unknown"
                ),
                "Color": player_data["Color"],
                "OpponentID": opponent_id,
                "PlayerRating": player_data["PlayerRating"],
                "OpponentRating": player_data["OpponentRating"],
                "Score": score,
                "Outcome": get_outcome(score),
                "Opening": get_opening(game),
                "PlyCount": int(
                    headers.get(
                        "PlyCount",
                        len(list(game.mainline_moves()))
                    )
                ),
            })

    # Nu continuam daca nu am gasit nicio partida
    if not games_data:
        raise ValueError(
            "Nu a fost gasita nicio partida in fisierul PGN."
        )

    # Transformam datele intr-un tabel pandas
    df = pd.DataFrame(games_data)

    # Verificari de baza
    if df["GameID"].duplicated().any():
        raise ValueError("Exista GameID-uri duplicate.")

    if df["Date"].isna().any():
        raise ValueError(
            "Exista partide fara date valide."
        )

    if df["Score"].isna().any():
        raise ValueError(
            "Exista partide fara scor valid."
        )

    # Cream folderul de iesire, daca lipseste
    OUTPUT_FILE.parent.mkdir(
        parents=True,
        exist_ok=True
    )

    # Exportam tabelul in CSV
    df.to_csv(
        OUTPUT_FILE,
        index=False,
        encoding="utf-8-sig"
    )

    # Afisam un rezumat al procesarii
    print("ETL finalizat cu succes!")
    print(f"Partide procesate: {len(df)}")
    print(f"Victorii: {(df['Outcome'] == 'Win').sum()}")
    print(f"Remize: {(df['Outcome'] == 'Draw').sum()}")
    print(f"Infrangeri: {(df['Outcome'] == 'Loss').sum()}")
    print(f"Scor mediu: {df['Score'].mean():.2%}")
    print(f"Fisier generat: {OUTPUT_FILE}")


if __name__ == "__main__":
    main()