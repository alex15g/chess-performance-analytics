package com.chessanalytics.api.game;

import com.chessanalytics.api.stats.ColorStatsProjection;
import com.chessanalytics.api.stats.OpeningStatsProjection;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface GameRepository extends JpaRepository<Game, Long> {

    @Query("""
        SELECT g
        FROM Game g
        WHERE (
            :tournament IS NULL
            OR LOWER(g.tournament)
                LIKE LOWER(CONCAT('%', :tournament, '%'))
        )
        AND (
            :color IS NULL
            OR LOWER(g.color) = LOWER(:color)
        )
        ORDER BY g.date DESC, g.gameId ASC
        """)
    Page<Game> search(
        @Param("tournament") String tournament,
        @Param("color") String color,
        Pageable pageable
    );

    long countByOutcomeIgnoreCase(String outcome);

    @Query("""
        SELECT
            g.color AS color,
            COUNT(g) AS totalGames,
            SUM(CASE WHEN g.outcome = 'Win' THEN 1 ELSE 0 END) AS wins,
            SUM(CASE WHEN g.outcome = 'Draw' THEN 1 ELSE 0 END) AS draws,
            SUM(CASE WHEN g.outcome = 'Loss' THEN 1 ELSE 0 END) AS losses
        FROM Game g
        GROUP BY g.color
        ORDER BY g.color
        """)
    List<ColorStatsProjection> getStatsByColor();

    @Query("""
        SELECT
            g.opening AS opening,
            COUNT(g) AS totalGames,
            SUM(CASE WHEN g.outcome = 'Win' THEN 1 ELSE 0 END) AS wins,
            SUM(CASE WHEN g.outcome = 'Draw' THEN 1 ELSE 0 END) AS draws,
            SUM(CASE WHEN g.outcome = 'Loss' THEN 1 ELSE 0 END) AS losses
        FROM Game g
        GROUP BY g.opening
        ORDER BY COUNT(g) DESC
        """)
    List<OpeningStatsProjection> getStatsByOpening(Pageable pageable);
}