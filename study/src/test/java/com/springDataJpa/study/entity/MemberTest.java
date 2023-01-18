package com.springDataJpa.study.entity;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
//@Rollback(false)
@Slf4j
class MemberTest {

    @PersistenceContext
    EntityManager entityManager;

    @Test
    public void testEntity() {
        Team teamA = new Team("teamA");
        Team teamB = new Team("teamB");
        entityManager.persist(teamA);
        entityManager.persist(teamB);

        Member userA = new Member("userA", 10, teamA);
        Member userB = new Member("userB", 15, teamA);
        Member userC = new Member("userC", 20, teamB);
        Member userD = new Member("userD", 5, teamB);
        entityManager.persist(userA);
        entityManager.persist(userB);
        entityManager.persist(userC);
        entityManager.persist(userD);

        entityManager.flush();
        entityManager.clear();

        List<Member> members = entityManager.createQuery("select m from Member m", Member.class)
                .getResultList();

        members.forEach(m -> {
            log.info("member = ", m.toString());
            log.info("member team = ", m.getTeam().toString());
        });
    }

}