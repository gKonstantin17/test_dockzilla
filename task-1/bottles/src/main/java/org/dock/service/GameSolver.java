package org.dock.service;

import org.dock.entity.Bottle;
import org.dock.entity.GameState;
import org.dock.entity.Move;

import java.util.ArrayList;
import java.util.List;

public class GameSolver {
    public List<Move> solve(GameState initialState) {
        List<Move> solution = new ArrayList<>();
        GameState currentState = initialState.copy();

        int maxMoves = 1000;
        int moves = 0;

        while (!currentState.isSolved() && moves < maxMoves) {
            List<Move> possibleMoves = findPossibleMoves(currentState);

            if (possibleMoves.isEmpty()) {
                System.out.println("No possible moves found!");
                break;
            }

            Move move = possibleMoves.get(0);
            executeMove(currentState, move);
            solution.add(move);
            moves++;

            System.out.println("Move " + moves + ": " + move);
        }

        if (currentState.isSolved()) {
            System.out.println("Puzzle solved in " + moves + " moves!");
        } else {
            System.out.println("Puzzle not solved after " + moves + " moves");
        }

        return solution;
    }

    private List<Move> findPossibleMoves(GameState state) {
        List<Move> moves = new ArrayList<>();
        List<Bottle> bottles = state.getBottles();

        for (int i = 0; i < bottles.size(); i++) {
            Bottle from = bottles.get(i);
            if (from.isEmpty()) continue;

            for (int j = 0; j < bottles.size(); j++) {
                if (i == j) continue;

                Bottle to = bottles.get(j);
                if (from.canReceiveFrom(to) || to.canReceiveFrom(from)) {
                    if (to.canReceiveFrom(from)) {
                        moves.add(new Move(i, j));
                    }
                    if (from.canReceiveFrom(to)) {
                        moves.add(new Move(j, i));
                    }
                }
            }
        }

        return moves;
    }

    private void executeMove(GameState state, Move move) {
        List<Bottle> bottles = state.getBottles();
        Bottle from = bottles.get(move.getFromBottle());
        Bottle to = bottles.get(move.getToBottle());

        if (to.canReceiveFrom(from)) {
            String liquid = from.removeLiquid();
            to.addLiquid(liquid);
            state.getMoveHistory().add(move);
        }
    }
}
