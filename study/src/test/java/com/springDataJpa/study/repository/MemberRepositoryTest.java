package com.springDataJpa.study.repository;

import com.springDataJpa.study.dto.MemberDto;
import com.springDataJpa.study.entity.Member;
import com.springDataJpa.study.entity.Team;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
@Slf4j
//@Rollback(false)
public class MemberRepositoryTest {
    @Autowired
    private TeamRepository teamRepository;

    @Autowired
    private MemberRepository memberRepository;

    @PersistenceContext
    private EntityManager em;

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

    @Test
    public void findHelloBy() {
        List<Member> helloBy = memberRepository.findTop3HelloBy();
    }

    @Test
    public void findUsernameList() {
        Member member = new Member("usera", 10);
        Member member2 = new Member("usera", 20);

        memberRepository.save(member);
        memberRepository.save(member2);

        List<String> usernameList = memberRepository.findUsernameList();
        usernameList.forEach(e -> log.info("username = {}", e));
    }

    @Test
    public void findMemberDto() {
        Team teamA = new Team("teamA");
        teamRepository.save(teamA);

        Team teamB = new Team("teamB");
        teamRepository.save(teamB);

        Member member = new Member("A", 10, teamA);
        memberRepository.save(member);
        Member member2 = new Member("B", 20, teamB);
        memberRepository.save(member2);

        List<MemberDto> memberDto = memberRepository.findMemberDto();
        memberDto.forEach(dto -> log.info("memberDto = {}", dto.toString()));
    }

    @Test
    public void findByNames() {
        Member member = new Member("A", 10);
        memberRepository.save(member);
        Member member2 = new Member("B", 20);
        memberRepository.save(member2);

        List<Member> result = memberRepository.findByNames(Arrays.asList("A", "B"));
        result.forEach(name -> log.info("name = {}", name));
    }

    @Test
    public void findByPage() {
        memberRepository.save(new Member("usera", 10));
        memberRepository.save(new Member("userB", 10));
        memberRepository.save(new Member("userC", 10));
        memberRepository.save(new Member("userD", 10));
        memberRepository.save(new Member("userE", 10));

        int age = 10;
        PageRequest pageRequest = PageRequest.of(0, 3, Sort.by(Sort.Direction.DESC, "username"));

        //when
        Page<Member> page = memberRepository.findByAge(age, pageRequest);

        //transfer Dto
        Page<MemberDto> toMap = page.map(member -> new MemberDto(member.getId(), member.getUsername(), null));

        //then
        List<Member> content = page.getContent();
        long totalCount = page.getTotalElements();

        toMap.forEach(data -> {
            log.info("member = {}", data.toString());});

        assertThat(toMap.getContent().size()).isEqualTo(3);
        assertThat(totalCount).isEqualTo(5);
        assertThat(page.getNumber()).isEqualTo(0);
        assertThat(page.getTotalPages()).isEqualTo(2);
        assertThat(page.isFirst()).isTrue();
        assertThat(page.hasNext()).isTrue();
    }

    @Test
    public void bulkAgePlus() {
        memberRepository.save(new Member("usera", 10));
        memberRepository.save(new Member("userB", 15));
        memberRepository.save(new Member("userC", 11));
        memberRepository.save(new Member("userD", 13));
        memberRepository.save(new Member("userE", 14));

        int i = memberRepository.bulkAgePlus(13);

        
        //영속성 컨텍스트 내부의 값을 불러오게 되므로 변경되지 않은 값을 가져옴
        List<Member> userE = memberRepository.findByUsername("userE");
        userE.forEach(e -> log.info(e.toString()));

        assertThat(i).isEqualTo(1);
    }

    @Test
    public void findMemberLazy() {
        //given
        //member1 -> teamA
        //member2 -> teamB

        Team teamA = new Team("teamA");
        Team teamB = new Team("teamB");
        teamRepository.save(teamA);
        teamRepository.save(teamB);
        Member member1 = new Member("member1", 10, teamA);
        Member member2 = new Member("member1", 10, teamB);
        memberRepository.save(member1);
        memberRepository.save(member2);

        em.flush();
        em.clear();

        List<Member> members = memberRepository.findAll();

        members.forEach(m -> {log.info("member = {}", m.toString());
        log.info("member.team = {}", m.getTeam().getName());
        });
    }
}
