package org.dock;

import org.dock.entity.GameState;
import org.dock.entity.Move;
import org.dock.service.FileService;
import org.dock.service.GameSolver;
import org.dock.service.SolutionFormatter;

import java.util.List;

public class Main {
    public static void main(String[] args) {
        String[][] input = {
                {"R", "R", "B", "B"},
                {"B", "R", "B", "R"},
                {"R", "B", "R", "B"},
                {"B", "B", "R", "R"},
                {"R", "B", "B", "R"},
                {"B", "R", "R", "B"},
                {"_", "_", "_", "_"},
                {"_", "_", "_", "_"},
                {"_", "_", "_", "_"},
                {"_", "_", "_", "_"},
                {"_", "_", "_", "_"},
                {"_", "_", "_", "_"},
                {"_", "_", "_", "_"},
                {"_", "_", "_", "_"}
        };

        FileService fileService = new FileService();
        GameState initialState = fileService.loadFromArray(input);

        GameSolver solver = new GameSolver();
        List<Move> solution = solver.solve(initialState);

        System.out.println("Solution found:");
        System.out.println(SolutionFormatter.format(solution));

        try {
            fileService.saveSolution(solution, "solution.txt");
            System.out.println("\nSolution saved to solution.txt");
        } catch (Exception e) {
            System.out.println("Error saving solution: " + e.getMessage());
        }
    }
}
