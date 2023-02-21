package study.querydsl.entity;

import lombok.Getter;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

@Entity
@Getter
public class Hello {

    @Id
    @GeneratedValue
    @Column(name = "hello_id")
    private Long id;
}
