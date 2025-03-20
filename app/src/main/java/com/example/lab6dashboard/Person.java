package com.example.lab6dashboard;

public class Person {

    private String name;
    private String status;
    private int icon;

    public Person(String name, String status) {
        this.name = name;
        this.status = status;
        this.icon = R.drawable.usericon;
    }

    public String getName() {
        return this.name;
    }

    public String getStatus() {
        return this.status;
    }

    public int getIcon() {
        return this.icon;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setStatus(String status) {
        this.status = status;
    }

}
