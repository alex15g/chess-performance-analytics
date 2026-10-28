package com.chessanalytics.api.stats;

public interface OpeningStatsProjection {

    String getOpening();

    Long getTotalGames();

    Long getWins();

    Long getDraws();

    Long getLosses();
}