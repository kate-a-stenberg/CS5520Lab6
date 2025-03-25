package com.numad.firesidechat;

public class Contact {

    private String name;
    private String status;
    private int icon;

    public Contact(String name) {
        this.name = name;
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
