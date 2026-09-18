package com.deutschhub.domain.learning.learnerstate.model.enums;

public enum CurrentLevel {

    A0(0),
    A1(1),
    A2(2),
    B1(3),
    B2(4),
    C1(5),
    C2(6);

    private final int rank;

    CurrentLevel(int rank) {
        this.rank = rank;
    }

    public boolean isLowerThan(CurrentLevel other) {
        return this.rank < other.rank;
    }
}
