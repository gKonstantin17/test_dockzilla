package org.dock;

import org.dock.entity.Bottle;
import org.dock.entity.GameState;
import org.dock.entity.Move;
import org.dock.service.InputParser;
import org.dock.service.GameSolver;
import org.dock.service.SolutionFormatter;

import java.util.List;

public class Main {
    public static void main(String[] args) {
        // from image
        String[][] input = {
                // from left to right.
                // from bottom to top
                {"04", "04", "10", "02"},
                {"08", "12", "08", "01"},
                {"09", "05", "07", "10"},
                {"05", "02", "03", "05"},
                {"07", "08", "11", "06"},
                {"02", "01", "12", "12"},
                {"11", "08", "07", "04"},
                {"01", "03", "11", "10"},
                {"09", "09", "07", "10"},
                {"11", "06", "02", "06"},
                {"03", "09", "06", "04"},
                {"01", "12", "03", "05"},
                {"_", "_", "_", "_"},
                {"_", "_", "_", "_"}
        };
        // other example
//        String[][] input = {
//                {"02","09","04"},
//                {"01","08","09"},
//                {"09","07","05"},
//                {"05","03","01"},
//                {"06","01","08"},
//                {"02","02","06"},
//                {"04","07","08"},
//                {"04","07","05"},
//                {"03","03","06"},
//                {"_","_","_"},
//                {"_","_","_"}
//        };

        GameState initialState = InputParser.parse(input);

        GameSolver solver = new GameSolver();
        List<Move> solution = solver.solve(initialState);

        System.out.println("\nSolution found (" + solution.size() + " moves):");
        System.out.println(SolutionFormatter.format(solution));
    }
}