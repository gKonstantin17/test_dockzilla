package org.dock.service;

import org.dock.entity.Move;

import java.util.List;

public class SolutionFormatter {

    public static String format(List<Move> moves) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < moves.size(); i++) {
            if (i > 0 && i % 8 == 0) sb.append("\n");
            sb.append(moves.get(i)).append(" ");
        }
        return sb.toString().trim();
    }
}