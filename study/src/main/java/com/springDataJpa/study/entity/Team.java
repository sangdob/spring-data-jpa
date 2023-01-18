package com.springDataJpa.study.entity;

import lombok.*;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@ToString(of = {"id", "name"})
public class Team {

    @Id
    @GeneratedValue
    @Column(name = "team_id")
    private Long id;

    private String name;

    @OneToMany(fetch = FetchType.LAZY
            , mappedBy = "team")
    @JoinColumn(name = "member_id")
    private List<Member> members = new ArrayList<>();


    public Team(String name) {
        this.name = name;
    }
}
