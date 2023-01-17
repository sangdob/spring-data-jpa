package com.springDataJpa.study.entity;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

@Entity
@Getter
@Setter
public class Member {

    @Id
    @GeneratedValue
    private Long id;
    private String userName;

    /**
     * jpa proxy 기술 사용시 private는 막히므로 protected로 열어줌
     */
    protected Member() {
    }

    public Member(String userName) {
        this.userName = userName;
    }
}
