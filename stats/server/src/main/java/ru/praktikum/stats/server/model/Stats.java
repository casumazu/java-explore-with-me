package ru.praktikum.stats.server.model;

import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Stats {

    private String app;
    private String uri;
    private long hits;
}