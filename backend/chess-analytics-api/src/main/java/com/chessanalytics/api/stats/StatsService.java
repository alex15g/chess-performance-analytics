package com.chessanalytics.api.stats;

import com.chessanalytics.api.game.GameRepository;

import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class StatsService {

    private final GameRepository gameRepository;

    public StatsService(GameRepository gameRepository) {
        this.gameRepository = gameRepository;
    }

    public StatsDtos.Overview getOverview() {
        long total = gameRepository.count();
        long wins = gameRepository.countByOutcomeIgnoreCase("Win");
        long draws = gameRepository.countByOutcomeIgnoreCase("Draw");
        long losses = gameRepository.countByOutcomeIgnoreCase("Loss");

        return new StatsDtos.Overview(
            total,
            wins,
            draws,
            losses,
            percentage(wins, total),
            scorePercentage(wins, draws, total)
        );
    }

    public List<StatsDtos.ColorStats> getStatsByColor() {
        return gameRepository.getStatsByColor()
            .stream()
            .map(row -> {
                long total = value(row.getTotalGames());
                long wins = value(row.getWins());
                long draws = value(row.getDraws());
                long losses = value(row.getLosses());

                return new StatsDtos.ColorStats(
                    row.getColor(),
                    total,
                    wins,
                    draws,
                    losses,
                    percentage(wins, total),
                    scorePercentage(wins, draws, total)
                );
            })
            .toList();
    }

    public List<StatsDtos.OpeningStats> getStatsByOpening(int limit) {
        if (limit < 1 || limit > 20) {
            throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "Limit must be between 1 and 20."
            );
        }

        return gameRepository
            .getStatsByOpening(PageRequest.of(0, limit))
            .stream()
            .map(row -> {
                long total = value(row.getTotalGames());
                long wins = value(row.getWins());
                long draws = value(row.getDraws());
                long losses = value(row.getLosses());

                return new StatsDtos.OpeningStats(
                    row.getOpening(),
                    total,
                    wins,
                    draws,
                    losses,
                    percentage(wins, total),
                    scorePercentage(wins, draws, total)
                );
            })
            .toList();
    }

    private long value(Long number) {
        return number == null ? 0 : number;
    }

    private double percentage(long value, long total) {
        if (total == 0) {
            return 0;
        }

        return roundTwoDecimals(value * 100.0 / total);
    }

    private double scorePercentage(
        long wins,
        long draws,
        long total
    ) {
        if (total == 0) {
            return 0;
        }

        double points = wins + draws * 0.5;

        return roundTwoDecimals(points * 100.0 / total);
    }

    private double roundTwoDecimals(double value) {
        return Math.round(value * 100.0) / 100.0;
    }
}