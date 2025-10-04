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
                {"02", "10", "04", "04"},
                {"01", "08", "12", "08"},
                {"10", "07", "05", "09"},
                {"05", "03", "02", "05"},
                {"06", "11", "08", "07"},
                {"12", "12", "01", "02"},
                {"04", "07", "08", "11"},
                {"10", "11", "03", "01"},
                {"10", "07", "09", "09"},
                {"06", "02", "06", "11"},
                {"04", "06", "09", "03"},
                {"05", "03", "12", "01"},
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