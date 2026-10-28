package com.chessanalytics.api.stats;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/stats")
public class StatsController {

    private final StatsService statsService;

    public StatsController(StatsService statsService) {
        this.statsService = statsService;
    }

    @GetMapping("/overview")
    public StatsDtos.Overview getOverview() {
        return statsService.getOverview();
    }

    @GetMapping("/by-color")
    public List<StatsDtos.ColorStats> getStatsByColor() {
        return statsService.getStatsByColor();
    }

    @GetMapping("/by-opening")
    public List<StatsDtos.OpeningStats> getStatsByOpening(
        @RequestParam(defaultValue = "10") int limit
    ) {
        return statsService.getStatsByOpening(limit);
    }
}