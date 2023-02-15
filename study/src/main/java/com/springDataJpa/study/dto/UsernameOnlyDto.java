package com.springDataJpa.study.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

public class UsernameOnlyDto {
    public UsernameOnlyDto(String username, int age) {
        this.username = username;
        this.age = age;
    }

    private final String username;
    private final int age;

    public String getUsername() {
        return username;
    }

    public int getAge() {
        return age;
    }
}
