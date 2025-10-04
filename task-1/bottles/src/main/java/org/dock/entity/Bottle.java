package org.dock.entity;


import lombok.Data;

import java.util.Stack;

@Data
public class Bottle {
    private final int id;
    private final int capacity;
    private Stack<String> liquids = new Stack<>();

    public boolean isEmpty() {
        return liquids.isEmpty() || liquids.peek().equals("_");
    }

    public boolean isFull() {
        return liquids.size() >= capacity;
    }

    public String topColor() {
        return liquids.isEmpty() ? "_" : liquids.peek();
    }

    public int freeSpace() {
        return capacity - liquids.size();
    }

    public boolean canReceiveFrom(Bottle other) {
        if (isFull() || other.isEmpty()) return false;
        return isEmpty() || topColor().equals(other.topColor());
    }

    public void addLiquid(String color) {
        if (isFull()) throw new IllegalStateException("Bottle is full");
        liquids.push(color);
    }

    public String removeLiquid() {
        if (isEmpty()) throw new IllegalStateException("Bottle is empty");
        return liquids.pop();
    }

    public Bottle copy() {
        Bottle copy = new Bottle(id, capacity);
        copy.liquids = (Stack<String>) liquids.clone();
        return copy;
    }
}
