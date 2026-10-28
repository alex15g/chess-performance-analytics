package com.chessanalytics.api.stats;

public interface ColorStatsProjection {

    String getColor();

    Long getTotalGames();

    Long getWins();

    Long getDraws();

    Long getLosses();
}