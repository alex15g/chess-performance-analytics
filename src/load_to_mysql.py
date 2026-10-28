import os
from pathlib import Path
import csv
from decimal import Decimal

import mysql.connector
from mysql.connector import Error

PROJECT_ROOT = Path(__file__).resolve().parents[1]
CSV_FILE = PROJECT_ROOT / "data" / "games.csv"


def optional_int(value):
    """Converteste o valoare in int sau returneaza None."""
    if value is None or value.strip() == "":
        return None
    return int(value)


def main():
    if not CSV_FILE.exists():
        raise FileNotFoundError(
            f"Nu gasesc fisierul CSV: {CSV_FILE}"
        )

    with CSV_FILE.open(
        "r",
        encoding="utf-8-sig",
        newline=""
    ) as file:
        reader = csv.DictReader(file)
        rows = list(reader)

    if not rows:
        raise ValueError("Fisierul CSV nu contine partide.")

    if len(rows) != 57:
        raise ValueError(
            f"Se asteptau 57 de partide, dar am gasit {len(rows)}."
        )

    connection = None
    cursor = None

    try:
        connection = mysql.connector.connect(
        host=os.getenv("DB_HOST", "127.0.0.1"),
        port=int(os.getenv("DB_PORT", "3306")),
        user=os.environ["DB_USERNAME"],
        password=os.environ["DB_PASSWORD"],
        database=os.getenv("DB_NAME", "chess_analytics")
)

        cursor = connection.cursor()

        sql = """
            INSERT INTO games (
                game_id,
                game_date,
                tournament,
                site,
                round_label,
                color,
                opponent_id,
                player_rating,
                opponent_rating,
                score,
                outcome,
                opening,
                ply_count
            )
            VALUES (
                %s, %s, %s, %s, %s, %s, %s,
                %s, %s, %s, %s, %s, %s
            )
            ON DUPLICATE KEY UPDATE
                game_date = VALUES(game_date),
                tournament = VALUES(tournament),
                site = VALUES(site),
                round_label = VALUES(round_label),
                color = VALUES(color),
                opponent_id = VALUES(opponent_id),
                player_rating = VALUES(player_rating),
                opponent_rating = VALUES(opponent_rating),
                score = VALUES(score),
                outcome = VALUES(outcome),
                opening = VALUES(opening),
                ply_count = VALUES(ply_count)
        """

        values = []

        for row in rows:
            opponent_id = row["OpponentID"].strip()

            # Verificam ca numele adversarului este anonimizat.
            if not opponent_id.startswith("Opponent_"):
                raise ValueError(
                    f"ID de adversar neanonimizat: {opponent_id}"
                )

            values.append((
                int(row["GameID"]),
                row["Date"],
                row["Tournament"],
                row["Site"],
                row["Round"],
                row["Color"],
                opponent_id,
                optional_int(row["PlayerRating"]),
                optional_int(row["OpponentRating"]),
                Decimal(row["Score"]),
                row["Outcome"],
                row["Opening"],
                optional_int(row["PlyCount"])
            ))

        cursor.executemany(sql, values)
        connection.commit()

        cursor.execute("SELECT COUNT(*) FROM games")
        total_games = cursor.fetchone()[0]

        print("Import finalizat cu succes!")
        print(f"Randuri procesate: {len(values)}")
        print(f"Partide existente in baza de date: {total_games}")

    except (Error, ValueError, KeyError, OSError) as error:
        if connection is not None:
            connection.rollback()
        print(f"Importul a esuat: {error}")
        raise

    finally:
        if cursor is not None:
            cursor.close()

        if connection is not None and connection.is_connected():
            connection.close()


if __name__ == "__main__":
    main()