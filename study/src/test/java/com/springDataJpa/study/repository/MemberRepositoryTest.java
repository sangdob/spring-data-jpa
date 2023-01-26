package com.springDataJpa.study.repository;

import com.springDataJpa.study.entity.Member;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
//@Rollback(false)
public class MemberRepositoryTest {

    @Autowired
    private MemberRepository memberRepository;
    @Test
    public void saveMember() {
        Member member = new Member("memberUserA");
        Member savedMember = memberRepository.save(member);

        Member findMember = memberRepository.findById(savedMember.getId()).get();

        assertThat(findMember.getId()).isEqualTo(savedMember.getId());
        assertThat(findMember.getUsername()).isEqualTo(savedMember.getUsername());
        assertThat(findMember).isEqualTo(savedMember);
    }

    @Test
    public void CRUDTest() {
        Member member = new Member("memberUserA");
        Member member2 = new Member("memberUserB");

        memberRepository.save(member);
        memberRepository.save(member2);

        Member findMember = memberRepository.findById(member.getId()).get();
        Member findMember2 = memberRepository.findById(member2.getId()).get();

        assertThat(member.getId()).isEqualTo(findMember.getId());
        assertThat(member.getId()).isEqualTo(findMember.getId());

        List<Member> all = memberRepository.findAll();
        assertThat(all.size()).isEqualTo(2);

        long count = memberRepository.count();
        assertThat(count).isEqualTo(2);

        memberRepository.delete(member);
        memberRepository.delete(member2);

        long deletedCount = memberRepository.count();
        assertThat(deletedCount).isEqualTo(0);
    }

    @Test
    public void findByUsernameAndAgeGreaterThan() {
        Member member = new Member("usera", 10);
        Member member2 = new Member("usera", 20);

        memberRepository.save(member);
        memberRepository.save(member2);

        List<Member> result = memberRepository.findByUsernameAndAgeGreaterThan("usera", 15);

        assertThat(result.get(0).getUsername()).isEqualTo("usera");
        assertThat(result.get(0).getAge()).isEqualTo(20);
    }
}
