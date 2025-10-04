package org.dock.entity;

import lombok.Data;
import java.util.Stack;

@Data
public class Bottle {
    private final int id;
    private final int capacity;
    private Stack<String> liquids = new Stack<>();

    public boolean isEmpty() {
        return liquids.isEmpty();
    }

    public boolean isFull() {
        return liquids.size() >= capacity;
    }

    public String topColor() {
        return liquids.isEmpty() ? null : liquids.peek();
    }

    public int topColorCount() {
        if (isEmpty()) return 0;
        String topColor = topColor();
        int count = 0;
        for (int i = liquids.size() - 1; i >= 0; i--) {
            if (liquids.get(i).equals(topColor)) {
                count++;
            } else {
                break;
            }
        }
        return count;
    }

    public int freeSpace() {
        return capacity - liquids.size();
    }

    public void addLiquid(String color) {
        if (isFull()) throw new IllegalStateException("Bottle is full");
        liquids.push(color);
    }

    public void addMultipleLiquids(String color, int amount) {
        for (int i = 0; i < amount; i++) {
            addLiquid(color);
        }
    }

    public String removeLiquid() {
        if (isEmpty()) throw new IllegalStateException("Bottle is empty");
        return liquids.pop();
    }

    public void removeMultipleLiquids(int amount) {
        for (int i = 0; i < amount; i++) {
            removeLiquid();
        }
    }

    public Bottle copy() {
        Bottle copy = new Bottle(id, capacity);
        copy.liquids = new Stack<>();
        copy.liquids.addAll(this.liquids);
        return copy;
    }

    public boolean isUniform() {
        if (isEmpty()) return true;
        return liquids.stream().distinct().count() <= 1;
    }

    public boolean isCompleted() {
        return isFull() && isUniform();
    }

    public int pourTo(Bottle to) {
        if (isEmpty() || to.isFull()) return 0;
        String color = topColor();
        if (color == null) return 0;
        if (!to.isEmpty() && !color.equals(to.topColor())) return 0;

        int amount = Math.min(topColorCount(), to.freeSpace());
        // Удаляем из этой бутылки и добавляем в цель
        removeMultipleLiquids(amount);
        to.addMultipleLiquids(color, amount);
        return amount;
    }
}