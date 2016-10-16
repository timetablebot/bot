package de.lukweb.timetablebot.telegram;

import de.lukweb.timetablebot.sql.DB;
import de.lukweb.timetablebot.sql.UsersSQL;
import org.slf4j.LoggerFactory;
import org.telegram.telegrambots.meta.api.methods.groupadministration.GetChat;
import org.telegram.telegrambots.meta.api.objects.Chat;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class TelegramUser {

    private long chatid;
    private boolean blocked;
    private boolean disabled;
    private boolean verified;
    private TelegramRank rank;
    private int grade;
    private char classChar;
    private List<String> teachers; // lowercase
    private String telegramName;
    private MessageProcessor messageProcessor;

    public TelegramUser(Chat chat) {
        this.chatid = chat.getId();
        this.blocked = false;
        this.verified = false;
        this.rank = TelegramRank.USER;
        this.grade = -1;
        this.classChar = ' ';
        this.teachers = new ArrayList<>();
        this.telegramName = "";
        this.messageProcessor = new MessageProcessor(this);

        updateTelegramName(chat);
    }

    public TelegramUser(long chatid, boolean blocked, boolean disabled, boolean verified, TelegramRank rank,
                        int grade, char classChar, List<String> teachers) {
        this.chatid = chatid;
        this.blocked = blocked;
        this.disabled = disabled;
        this.verified = verified;
        this.rank = rank;
        this.grade = grade;
        this.classChar = classChar;
        this.teachers = teachers;
        this.telegramName = "";
        this.messageProcessor = new MessageProcessor(this);
    }

    public long getChatid() {
        return chatid;
    }

    public boolean isBlocked() {
        return blocked;
    }

    public void setBlocked(boolean blocked) {
        this.blocked = blocked;
    }

    public boolean isVerified() {
        return verified;
    }

    public void setVerified(boolean verified) {
        this.verified = verified;
    }

    public TelegramRank getRank() {
        return rank;
    }

    public void setRank(TelegramRank rank) {
        this.rank = rank;
    }

    public int getGrade() {
        return grade;
    }

    public void setGrade(int grade) {
        this.grade = grade;
    }

    public char getClassChar() {
        return classChar;
    }

    public void setClassChar(char classChar) {
        this.classChar = classChar;
    }

    /**
     * Gets the teachers which the user has been selected.
     * The teacher names will be in lower case.
     *
     * @return List of teacher names
     */
    public List<String> getTeachers() {
        return teachers;
    }

    public void setTeachers(List<String> teachers) {
        this.teachers = teachers;
    }

    public void setDisabled(boolean disabled) {
        this.disabled = disabled;
    }

    public boolean isDisabled() {
        return disabled;
    }

    public MessageProcessor messages() {
        return messageProcessor;
    }

    public String getTelegramName() {
        if (telegramName.isEmpty()) {
            updateTelegramName();
        }
        return telegramName;
    }

    public void updateTelegramName() {
        GetChat getChat = new GetChat().setChatId(chatid);
        Chat chat;
        try {
            chat = TelegramBot.get().execute(getChat);
        } catch (TelegramApiException e) {
            LoggerFactory.getLogger(getClass()).warn("Couldn't get chat with id " + chatid, e);
            return;
        }

        updateTelegramName(chat);
    }

    public void updateTelegramName(Chat chat) {
        String name = "";
        if (chat.getFirstName() != null) {
            name += chat.getFirstName() + " ";
        }
        if (chat.getLastName() != null) {
            name += chat.getLastName() + " ";
        }
        if (chat.getUserName() != null) {
            name += "(@" + chat.getUserName() + ")";
        }

        this.telegramName = name.trim();
    }

    public void saveChanges() {
        DB.get().useHandle(handle -> new UsersSQL(handle).update(this));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TelegramUser that = (TelegramUser) o;
        return chatid == that.chatid;
    }

    @Override
    public int hashCode() {
        return Objects.hash(chatid);
    }
}
