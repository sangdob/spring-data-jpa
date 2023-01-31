package com.springDataJpa.study.repository;

import com.springDataJpa.study.entity.Member;
import com.springDataJpa.study.jpaRepository.MemberJpaRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.assertj.core.api.Assertions.*;

@SpringBootTest
@Transactional
class MemberJpaRepositoryTest {

    @Autowired
    private MemberJpaRepository memberJpaRepository;
//    @Autowired
//    private MemberRepository memberRepository;

    @Test
    public void saveMember() {
        Member member = new Member("memberUserA");
        Member savedMember = memberJpaRepository.save(member);

        Member findMember = memberJpaRepository.find(savedMember.getId());

        assertThat(findMember.getId()).isEqualTo(savedMember.getId());
        assertThat(findMember.getUsername()).isEqualTo(savedMember.getUsername());
        assertThat(findMember).isEqualTo(savedMember);
    }

    @Test
    public void CRUDTest() {
        Member member = new Member("memberUserA");
        Member member2 = new Member("memberUserB");
        memberJpaRepository.save(member);
        memberJpaRepository.save(member2);

        Member findMember = memberJpaRepository.find(member.getId());
        Member findMember2 = memberJpaRepository.find(member2.getId());

        assertThat(member.getId()).isEqualTo(findMember.getId());
        assertThat(member.getId()).isEqualTo(findMember.getId());

        List<Member> all = memberJpaRepository.findAll();
        assertThat(all.size()).isEqualTo(2);

        long count = memberJpaRepository.count();
        assertThat(count).isEqualTo(2);

        memberJpaRepository.delete(member);
        memberJpaRepository.delete(member2);

        long deletedCount = memberJpaRepository.count();
        assertThat(deletedCount).isEqualTo(0);
    }

    @Test
    public void findByUsernameAndAgeGreaterThan() {
        Member member = new Member("usera", 10);
        Member member2 = new Member("usera", 20);

        memberJpaRepository.save(member);
        memberJpaRepository.save(member2);

        List<Member> result = memberJpaRepository.findByUsernameAndAgeGreaterThan("usera", 15);

        assertThat(result.get(0).getUsername()).isEqualTo("usera");
        assertThat(result.get(0).getAge()).isEqualTo(20);
    }

    @Test
    public void findByPage() {
        memberJpaRepository.save(new Member("usera", 10));
        memberJpaRepository.save(new Member("userB", 10));
        memberJpaRepository.save(new Member("userC", 10));
        memberJpaRepository.save(new Member("userD", 10));
        memberJpaRepository.save(new Member("userE", 10));

        int age = 10;
        int offset = 0;
        int limit = 3;

        List<Member> members = memberJpaRepository.findByPage(age, offset, limit);
        long totalCount = memberJpaRepository.totalCount(age);

        assertThat(members.size()).isEqualTo(3);
        assertThat(totalCount).isEqualTo(5L);
    }
}