package de.lukweb.timetablebot.telegram;

public enum TelegramRank {

    USER(1),
    ADMIN(2);

    private int importance;

    TelegramRank(int importance) {
        this.importance = importance;
    }

    public int getImportance() {
        return importance;
    }
}
