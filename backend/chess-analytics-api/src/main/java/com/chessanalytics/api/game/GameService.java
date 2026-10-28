package com.chessanalytics.api.game;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;

@Service
public class GameService {

    private final GameRepository gameRepository;

    public GameService(GameRepository gameRepository) {
        this.gameRepository = gameRepository;
    }

    public Page<Game> getGames(
        String tournament,
        String color,
        int page,
        int size
    ) {
        if (page < 0) {
            throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "Page must be greater than or equal to 0."
            );
        }

        if (size < 1 || size > 100) {
            throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "Size must be between 1 and 100."
            );
        }

        tournament = normalize(tournament);
        color = normalize(color);

        if (color != null
            && !color.equalsIgnoreCase("White")
            && !color.equalsIgnoreCase("Black")) {

            throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "Color must be White or Black."
            );
        }

        return gameRepository.search(
            tournament,
            color,
            PageRequest.of(page, size)
        );
    }

    public Game getGameById(Long id) {
        return gameRepository.findById(id)
            .orElseThrow(() ->
                new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Game not found: " + id
                )
            );
    }

    private String normalize(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }

        return value.trim();
    }
}