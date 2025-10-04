package org.dock.entity;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class Move {
    private int fromBottle;
    private int toBottle;

    @Override
    public String toString() {
        return String.format("(%2d, %2d)", fromBottle, toBottle);
    }
}
