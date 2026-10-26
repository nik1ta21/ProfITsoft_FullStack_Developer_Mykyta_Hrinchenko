package com.profitsoft.gamestats.model;

import java.util.List;

public class Game {
    private String title;
    private String developer;
    private int yearReleased;
    private List<String> genres;

    public Game() {
    }

    public Game(String title, String developer, int yearReleased, List<String> genres) {
        this.title = title;
        this.developer = developer;
        this.yearReleased = yearReleased;
        this.genres = genres;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDeveloper() {
        return developer;
    }

    public void setDeveloper(String developer) {
        this.developer = developer;
    }

    public int getYearReleased() {
        return yearReleased;
    }

    public void setYearReleased(int yearReleased) {
        this.yearReleased = yearReleased;
    }

    public List<String> getGenres() {
        return genres;
    }

    public void setGenres(List<String> genres) {
        this.genres = genres;
    }

    @Override
    public String toString() {
        return "Game{" +
                "title='" + title + '\'' +
                ", developer='" + developer + '\'' +
                ", yearReleased=" + yearReleased +
                ", genres=" + genres +
                '}';
    }
}
