package com.profitsoft.gamestats.statistics;

public class StatisticItem {
    private String value;
    private int count;

    public StatisticItem() {
    }

    public StatisticItem(String value, int count) {
        this.value = value;
        this.count = count;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }
}
