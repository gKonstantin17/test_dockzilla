package org.dock.service;

import org.dock.entity.Bottle;
import org.dock.entity.GameState;
import org.dock.entity.Move;

import java.util.*;

public class GameSolver {

    private static class StateNode implements Comparable<StateNode> {
        GameState state;
        List<Move> path;
        int cost; // g(n) - реальная стоимость пути
        int heuristic; // h(n) - эвристическая оценка

        StateNode(GameState state, List<Move> path, int cost) {
            this.state = state;
            this.path = path;
            this.cost = cost;
            this.heuristic = calculateHeuristic(state);
        }

        int totalCost() {
            return cost + heuristic;
        }

        @Override
        public int compareTo(StateNode other) {
            return Integer.compare(this.totalCost(), other.totalCost());
        }

        // Эвристика: количество "неправильных" капель
        private int calculateHeuristic(GameState state) {
            int score = 0;
            for (Bottle bottle : state.getBottles()) {
                if (bottle.isEmpty()) continue;

                // Если бутылка не однородная - штраф за каждый разный цвет
                if (!bottle.isUniform()) {
                    Set<String> colors = new HashSet<>(bottle.getLiquids());
                    score += colors.size() * 2;
                }

                // Если бутылка однородная но не полная - небольшой штраф
                if (bottle.isUniform() && !bottle.isFull() && !bottle.isEmpty()) {
                    score += 1;
                }
            }
            return score;
        }
    }

    public List<Move> solve(GameState initialState) {
        // Сначала пробуем жадный алгоритм
        List<Move> greedySolution = solveGreedy(initialState);
        if (!greedySolution.isEmpty()) {
            return greedySolution;
        }

        // Если жадный не сработал, используем A*
        return solveAStar(initialState);
    }

    private List<Move> solveGreedy(GameState initialState) {
        GameState state = initialState.copy();
        List<Move> solution = new ArrayList<>();
        int maxMoves = 1000;
        int movesCount = 0;

        while (!state.isSolved() && movesCount < maxMoves) {
            Move bestMove = findBestGreedyMove(state);
            if (bestMove == null) {
                break; // Застряли
            }

            executeMove(state, bestMove);
            solution.add(bestMove);
            movesCount++;
        }

        if (state.isSolved()) {
            System.out.println("Greedy solution found: " + solution.size() + " moves");
            return solution;
        }

        return Collections.emptyList();
    }

    private Move findBestGreedyMove(GameState state) {
        List<Move> possibleMoves = findPossibleMoves(state);
        if (possibleMoves.isEmpty()) return null;


        possibleMoves.sort((m1, m2) -> evaluateMove(m2, state) - evaluateMove(m1, state));

        return possibleMoves.get(0);
    }

    private List<Move> solveAStar(GameState initialState) {
        PriorityQueue<StateNode> openSet = new PriorityQueue<>();
        Map<String, Integer> visited = new HashMap<>();

        openSet.add(new StateNode(initialState, new ArrayList<>(), 0));
        visited.put(getStateHash(initialState), 0);

        int statesProcessed = 0;
        int maxStates = 100000;

        while (!openSet.isEmpty() && statesProcessed < maxStates) {
            StateNode current = openSet.poll();
            statesProcessed++;

            if (current.state.isSolved()) {
                System.out.println("A* solution found! States processed: " + statesProcessed);
                return current.path;
            }

            String currentHash = getStateHash(current.state);
            if (visited.containsKey(currentHash) && visited.get(currentHash) < current.cost) {
                continue;
            }

            List<Move> possibleMoves = findPossibleMoves(current.state);

            for (Move move : possibleMoves) {
                GameState nextState = current.state.copy();
                executeMove(nextState, move);

                String nextHash = getStateHash(nextState);
                int newCost = current.cost + 1;

                if (!visited.containsKey(nextHash) || visited.get(nextHash) > newCost) {
                    visited.put(nextHash, newCost);

                    List<Move> newPath = new ArrayList<>(current.path);
                    newPath.add(move);

                    openSet.add(new StateNode(nextState, newPath, newCost));
                }
            }

            if (statesProcessed % 5000 == 0) {
                System.out.println("Progress: " + statesProcessed + " states, queue: " + openSet.size());
            }
        }

        System.out.println("No solution found after " + statesProcessed + " states");
        return Collections.emptyList();
    }

    private String getStateHash(GameState state) {
        StringBuilder hash = new StringBuilder();
        for (Bottle bottle : state.getBottles()) {
            for (String liquid : bottle.getLiquids()) {
                hash.append(liquid);
            }
            hash.append("|");
        }
        return hash.toString();
    }

    private List<Move> findPossibleMoves(GameState state) {
        List<Move> moves = new ArrayList<>();
        List<Bottle> bottles = state.getBottles();

        for (int fromIdx = 0; fromIdx < bottles.size(); fromIdx++) {
            Bottle from = bottles.get(fromIdx);
            if (from.isEmpty()) continue;

            // Не переливаем из уже завершенной бутылки
            if (from.isCompleted()) continue;

            for (int toIdx = 0; toIdx < bottles.size(); toIdx++) {
                if (fromIdx == toIdx) continue;

                Bottle to = bottles.get(toIdx);

                // Не переливаем в завершенную бутылку
                if (to.isCompleted()) continue;

                if (canPour(from, to)) {
                    moves.add(new Move(fromIdx, toIdx));
                }
            }
        }

        // Сортируем ходы по эвристике
        moves.sort((m1, m2) -> evaluateMove(m2, state) - evaluateMove(m1, state));

        return moves;
    }

    private int evaluateMove(Move move, GameState state) {
        List<Bottle> bottles = state.getBottles();
        Bottle from = bottles.get(move.getFromBottle());
        Bottle to = bottles.get(move.getToBottle());

        int score = 0;

        // МАКСИМАЛЬНЫЙ приоритет: завершение бутылки
        if (!to.isEmpty() && from.topColor().equals(to.topColor())) {
            int afterPour = to.getLiquids().size() + Math.min(from.topColorCount(), to.freeSpace());
            if (afterPour == to.getCapacity()) {
                score += 1000;
            }
        }

        // Очень высокий приоритет: полное опустошение бутылки
        if (from.getLiquids().size() == from.topColorCount() &&
                from.topColorCount() <= to.freeSpace()) {
            score += 500;
        }

        // Высокий приоритет: объединение одинаковых цветов
        if (!to.isEmpty() && from.topColor().equals(to.topColor())) {
            score += 100 + from.topColorCount() * 10;
        }

        // Средний приоритет: переливание в пустую бутылку
        if (to.isEmpty()) {
            // Но только если переливаем всю однородную жидкость
            if (from.isUniform()) {
                score += 80;
            } else {
                score += 30;
            }
        }

        // ШТРАФ: не разбиваем почти завершенные бутылки
        if (from.isUniform() && from.getLiquids().size() > from.getCapacity() / 2) {
            score -= 200;
        }

        // ШТРАФ: не переливаем из завершенной бутылки
        if (from.isCompleted()) {
            score -= 10000;
        }

        return score;
    }

    private boolean canPour(Bottle from, Bottle to) {
        if (from.isEmpty() || to.isFull()) return false;
        if (from.isCompleted()) return false; // Не трогаем завершенные
        if (to.isCompleted()) return false;

        return to.isEmpty() || Objects.equals(from.topColor(), to.topColor());
    }

    public void executeMove(GameState state, Move move) {
        List<Bottle> bottles = state.getBottles();
        Bottle from = bottles.get(move.getFromBottle());
        Bottle to = bottles.get(move.getToBottle());

        if (canPour(from, to)) {
            from.pourTo(to);
        }
    }
}