package com.chessanalytics.api.game;

import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/games")
public class GameController {

    private final GameService gameService;

    public GameController(GameService gameService) {
        this.gameService = gameService;
    }

    @GetMapping
    public Page<Game> getGames(
        @RequestParam(required = false) String tournament,
        @RequestParam(required = false) String color,
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "10") int size
    ) {
        return gameService.getGames(
            tournament,
            color,
            page,
            size
        );
    }

    @GetMapping("/{id}")
    public Game getGameById(@PathVariable Long id) {
        return gameService.getGameById(id);
    }
}