package org.dock.service;

import lombok.Data;
import org.dock.entity.Bottle;
import org.dock.entity.GameState;
import org.dock.entity.Move;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

@Data
public class FileService {

    public GameState loadFromArray(String[][] input) {
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

    public void saveSolution(List<Move> solution, String filename) throws IOException {
        try (PrintWriter writer = new PrintWriter(filename)) {
            for (int i = 0; i < solution.size(); i++) {
                if (i > 0 && i % 8 == 0) writer.println();
                writer.print(solution.get(i) + " ");
            }
            writer.println();
        }
    }
}
