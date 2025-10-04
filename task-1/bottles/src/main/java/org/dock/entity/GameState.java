package org.dock.entity;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Data
public class GameState {
    private List<Bottle> bottles;
    private List<Move> moveHistory = new ArrayList<>();

    public GameState(List<Bottle> bottles) {
        this.bottles = bottles.stream()
                .map(Bottle::copy)
                .collect(Collectors.toList());
    }

    public GameState copy() {
        return new GameState(bottles);
    }

    public boolean isSolved() {
        return bottles.stream().allMatch(bottle ->
                bottle.isEmpty() ||
                        (bottle.isFull() && isBottleUniform(bottle))
        );
    }

    private boolean isBottleUniform(Bottle bottle) {
        return bottle.getLiquids().stream().distinct().count() <= 1;
    }
}
