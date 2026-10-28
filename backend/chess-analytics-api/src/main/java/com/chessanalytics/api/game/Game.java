package com.chessanalytics.api.game;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "games")
public class Game {

    @Id
    @Column(name = "game_id")
    private Long gameId;

    @Column(name = "game_date", nullable = false)
    private LocalDate date;

    @Column(nullable = false, length = 150)
    private String tournament;

    @Column(length = 150)
    private String site;

    @Column(name = "round_label", length = 30)
    private String round;

    @Column(nullable = false, length = 10)
    private String color;

    @Column(name = "opponent_id", nullable = false, length = 50)
    private String opponentId;

    @Column(name = "player_rating")
    private Integer playerRating;

    @Column(name = "opponent_rating")
    private Integer opponentRating;

    @Column(nullable = false, precision = 3, scale = 1)
    private BigDecimal score;

    @Column(nullable = false, length = 10)
    private String outcome;

    @Column(length = 255)
    private String opening;

    @Column(name = "ply_count")
    private Integer plyCount;

    // Constructor necesar pentru JPA
    public Game() {
    }

    public Long getGameId() {
        return gameId;
    }

    public void setGameId(Long gameId) {
        this.gameId = gameId;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public String getTournament() {
        return tournament;
    }

    public void setTournament(String tournament) {
        this.tournament = tournament;
    }

    public String getSite() {
        return site;
    }

    public void setSite(String site) {
        this.site = site;
    }

    public String getRound() {
        return round;
    }

    public void setRound(String round) {
        this.round = round;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public String getOpponentId() {
        return opponentId;
    }

    public void setOpponentId(String opponentId) {
        this.opponentId = opponentId;
    }

    public Integer getPlayerRating() {
        return playerRating;
    }

    public void setPlayerRating(Integer playerRating) {
        this.playerRating = playerRating;
    }

    public Integer getOpponentRating() {
        return opponentRating;
    }

    public void setOpponentRating(Integer opponentRating) {
        this.opponentRating = opponentRating;
    }

    public BigDecimal getScore() {
        return score;
    }

    public void setScore(BigDecimal score) {
        this.score = score;
    }

    public String getOutcome() {
        return outcome;
    }

    public void setOutcome(String outcome) {
        this.outcome = outcome;
    }

    public String getOpening() {
        return opening;
    }

    public void setOpening(String opening) {
        this.opening = opening;
    }

    public Integer getPlyCount() {
        return plyCount;
    }

    public void setPlyCount(Integer plyCount) {
        this.plyCount = plyCount;
    }
}