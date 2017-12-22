package com.example.ivan.gitbrowser;

/**
 * Created by ivan on 22.12.17.
 * model which present subscriber data
 */

public class Subscriber {

    private String login;
    private String avatar;


    public Subscriber(String login, String avatar) {
        this.login = login;
        this.avatar = avatar;
    }


    public String getLogin() {
        return login;
    }

    public void setLogin(String login) {
        this.login = login;
    }

    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }
}
