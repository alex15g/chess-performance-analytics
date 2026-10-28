package com.chessanalytics.api.stats;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import com.chessanalytics.api.game.GameRepository;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;

@ExtendWith(MockitoExtension.class)
class StatsServiceTest {

    @Mock
    private GameRepository gameRepository;

    @InjectMocks
    private StatsService statsService;

    @Test
    void shouldCalculateOverviewCorrectly() {
        when(gameRepository.count()).thenReturn(4L);

        when(gameRepository.countByOutcomeIgnoreCase("Win"))
                .thenReturn(2L);

        when(gameRepository.countByOutcomeIgnoreCase("Draw"))
                .thenReturn(1L);

        when(gameRepository.countByOutcomeIgnoreCase("Loss"))
                .thenReturn(1L);

        StatsDtos.Overview result = statsService.getOverview();

        assertEquals(4L, result.totalGames());
        assertEquals(2L, result.wins());
        assertEquals(1L, result.draws());
        assertEquals(1L, result.losses());

        assertEquals(50.0, result.winRatePercent(), 0.01);
        assertEquals(62.5, result.scorePercent(), 0.01);
    }

    @Test
    void shouldReturnZeroPercentagesWhenThereAreNoGames() {
        when(gameRepository.count()).thenReturn(0L);

        when(gameRepository.countByOutcomeIgnoreCase("Win"))
                .thenReturn(0L);

        when(gameRepository.countByOutcomeIgnoreCase("Draw"))
                .thenReturn(0L);

        when(gameRepository.countByOutcomeIgnoreCase("Loss"))
                .thenReturn(0L);

        StatsDtos.Overview result = statsService.getOverview();

        assertEquals(0.0, result.winRatePercent(), 0.01);
        assertEquals(0.0, result.scorePercent(), 0.01);
    }

    @Test
    void shouldCalculateStatisticsByColor() {
        ColorStatsProjection white =
                mock(ColorStatsProjection.class);

        when(white.getColor()).thenReturn("White");
        when(white.getTotalGames()).thenReturn(4L);
        when(white.getWins()).thenReturn(2L);
        when(white.getDraws()).thenReturn(1L);
        when(white.getLosses()).thenReturn(1L);

        when(gameRepository.getStatsByColor())
                .thenReturn(List.of(white));

        List<StatsDtos.ColorStats> result =
                statsService.getStatsByColor();

        assertEquals(1, result.size());

        StatsDtos.ColorStats whiteStats = result.get(0);

        assertEquals("White", whiteStats.color());
        assertEquals(4L, whiteStats.totalGames());
        assertEquals(2L, whiteStats.wins());
        assertEquals(50.0, whiteStats.winRatePercent(), 0.01);
        assertEquals(62.5, whiteStats.scorePercent(), 0.01);
    }

    @Test
void shouldRejectInvalidOpeningLimits() {
    ResponseStatusException lowLimitException = assertThrows(
            ResponseStatusException.class,
            () -> statsService.getStatsByOpening(0)
    );

    assertEquals(
            HttpStatus.BAD_REQUEST,
            lowLimitException.getStatusCode()
    );

    ResponseStatusException highLimitException = assertThrows(
            ResponseStatusException.class,
            () -> statsService.getStatsByOpening(21)
    );

    assertEquals(
            HttpStatus.BAD_REQUEST,
            highLimitException.getStatusCode()
    );
}
}