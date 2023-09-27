package ru.praktikum.stats.server.model;

import lombok.*;

@Data
public class Stats {

    private String app;
    private String uri;
    private Long hits;
}