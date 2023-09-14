package ru.praktikum.stats.dto;

import lombok.*;

public interface StatsView {
    String getApp();

    String getUri();

    Long getHits();
}
