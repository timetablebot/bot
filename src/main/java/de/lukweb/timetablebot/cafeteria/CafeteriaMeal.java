package de.lukweb.timetablebot.cafeteria;

import java.time.LocalDate;

public class CafeteriaMeal {

    private LocalDate day;
    private boolean vegetarian;
    private String description;
    private double price;

    public CafeteriaMeal(LocalDate day, boolean vegetarian, String description, double price) {
        this.day = day;
        this.vegetarian = vegetarian;
        this.description = description;
        this.price = price;
    }

    public LocalDate getDay() {
        return day;
    }

    public long getEpochDay() {
        return day.toEpochDay();
    }

    public boolean isVegetarian() {
        return vegetarian;
    }

    public String getDescription() {
        return description;
    }

    public double getPrice() {
        return price;
    }


}
