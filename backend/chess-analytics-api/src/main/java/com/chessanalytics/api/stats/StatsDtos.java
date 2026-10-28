package com.chessanalytics.api.stats;

public final class StatsDtos {

    private StatsDtos() {
    }

    public record Overview(
        long totalGames,
        long wins,
        long draws,
        long losses,
        double winRatePercent,
        double scorePercent
    ) {
    }

    public record ColorStats(
        String color,
        long totalGames,
        long wins,
        long draws,
        long losses,
        double winRatePercent,
        double scorePercent
    ) {
    }

    public record OpeningStats(
        String opening,
        long totalGames,
        long wins,
        long draws,
        long losses,
        double winRatePercent,
        double scorePercent
    ) {
    }
}