package org.dock.service;

import lombok.Data;
import org.dock.entity.Bottle;
import org.dock.entity.GameState;

import java.util.ArrayList;
import java.util.List;

@Data
public class InputParser {

    public static GameState parse(String[][] input) {
        List<Bottle> bottles = new ArrayList<>();
        int capacity = input[0].length;

        for (int i = 0; i < input.length; i++) {
            Bottle bottle = new Bottle(i, capacity);
            for (int j = 0; j < input[i].length; j++) {
                String color = input[i][j];
                if (!color.equals("_")) {
                    bottle.addLiquid(color);
                }
            }
            bottles.add(bottle);
        }

        return new GameState(bottles);
    }

}
