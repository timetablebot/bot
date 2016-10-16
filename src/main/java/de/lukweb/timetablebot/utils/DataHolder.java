package de.lukweb.timetablebot.utils;

public class DataHolder<K> {

    private K value;

    public DataHolder() {
    }

    public DataHolder(K value) {
        this.value = value;
    }

    public K get() {
        return value;
    }

    public void set(K value) {
        this.value = value;
    }

    public boolean hasData() {
        return value != null;
    }
}
