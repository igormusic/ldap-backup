package com.rbc.ldapapi.model;

import java.util.UUID;

public class User {

    private String firstName;
    private String lastName;
    private String password;
    protected UUID userId;

    public static User Create(
                                UUID userId,
                                String firstName,
                                String lastName,
                                String password){
        User user = new User();

        user.userId = userId;
        user.firstName = firstName;
        user.lastName = lastName;
        user.password = password;
        return user;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public UUID getUserId() {
        return userId;
    }

    public String getPassword() {
        return password;
    }

    public String getDN(String usersOU){
        return getDN(userId, usersOU);
    }

    public static String getDN(UUID userId, String usersOU){
        return "cn=" + userId.toString() +"," + usersOU;
    }
}
