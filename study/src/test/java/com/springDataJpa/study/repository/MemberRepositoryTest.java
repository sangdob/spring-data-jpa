package com.springDataJpa.study.repository;

import com.springDataJpa.study.dto.MemberDto;
import com.springDataJpa.study.dto.MemberProjection;
import com.springDataJpa.study.dto.UsernameOnly;
import com.springDataJpa.study.dto.UsernameOnlyDto;
import com.springDataJpa.study.entity.Member;
import com.springDataJpa.study.entity.Team;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.Arrays;
import java.util.List;

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

        
        //????????? ???????????? ????????? ?????? ???????????? ????????? ???????????? ?????? ?????? ?????????
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

        List<Member> members = memberRepository.findEntityGraphByUsername("member1");

        members.forEach(m -> {
            log.info("member = {}", m.toString());
            log.info("member.team = {}", m.getTeam().getName());
            log.info("member.team.class = {}", m.getTeam().getClass());
        });
    }

    @Test
    public void queryHint() {
        Member member1 = memberRepository.save(new Member("member1", 10));
        em.flush();
        em.clear();

//      ????????? ????????? ???????????? ?????? ??? update??? ???????????? ??????????????????.
        Member readOnlyByUsername = memberRepository.findReadOnlyByUsername(member1.getUsername());

        em.flush();
        em.clear();
    }

    @Test
    public void callCustom() {
        List<Member> memberCustom = memberRepository.findMemberCustom();

    }

    @Test

    public void specMember() {
        Team teamA = new Team("teamA");
        em.persist(teamA);

        Member useA = new Member("useA", 1, teamA);
        Member useB = new Member("useB", 1, teamA);
        em.persist(useA);
        em.persist(useB);

        em.flush();
        em.clear();

        Specification<Member> spc = MemberSpec.teamname("teamA");
        List<Member> result = memberRepository.findAll(spc);

        result.forEach(m -> log.info("member = {}", m.toString()));

        assertThat(result.size()).isEqualTo(2);
    }

    /**
     * ex: Example class
     * inner join??? ???????????? ????????? left outer join?????? ?????? ????????? ????????? ?????? ??? ??????.
     */
    @Test
    public void queryByExample() {
        Team teamA = new Team("teamA");
        em.persist(teamA);

        Member useA = new Member("useA", 1, teamA);
        Member useB = new Member("useB", 1, teamA);
        em.persist(useA);
        em.persist(useB);

        em.flush();
        em.clear();

        Team team = new Team("teamA");
        Member member = new Member("useA",15, team);

        ExampleMatcher exampleMatcher = ExampleMatcher.matching().withIgnorePaths("age");

        Example<Member> of = Example.of(member, exampleMatcher);

        List<Member> result = memberRepository.findAll(of);

        assertThat(result.get(0).getUsername()).isEqualTo("useA");

    }

    @Test
    public void projections() {
        Team teamA = new Team("teamA");
        em.persist(teamA);

        Member useA = new Member("useA", 1, teamA);
        Member useB = new Member("useB", 1, teamA);
        em.persist(useA);
        em.persist(useB);

        em.flush();
        em.clear();

        List<UsernameOnly> result = memberRepository.findProjectionsByUsername("useA");
        result.forEach(u -> log.info("user = {}", u.toString()));
    }

    @Test
    public void projectionsDto() {
        Team teamA = new Team("teamA");
        em.persist(teamA);

        Member useA = new Member("useA", 1, teamA);
        Member useB = new Member("useB", 1, teamA);
        em.persist(useA);
        em.persist(useB);

        em.flush();
        em.clear();

        List<UsernameOnlyDto> result = memberRepository.findProjectionsDtoByUsername("useA");
        result.forEach(u -> log.info("user = name {} age {}", u.getUsername(), u.getAge()));
    }

    @Test
    public void nativeQuery() {
        Team teamA = new Team("teamA");
        em.persist(teamA);

        Member useA = new Member("useA", 1, teamA);
        Member useB = new Member("useB", 1, teamA);
        em.persist(useA);
        em.persist(useB);

        em.flush();
        em.clear();

        Member result = memberRepository.findByNativeQuery("useA");
        log.info("member = {}", result);
    }

    @Test
    public void nativePageQuery() {
        Team teamA = new Team("teamA");
        em.persist(teamA);

        Member useA = new Member("useA", 1, teamA);
        Member useB = new Member("useB", 1, teamA);
        em.persist(useA);
        em.persist(useB);

        em.flush();
        em.clear();

        Page<MemberProjection> result = memberRepository.findByNativeProjections(PageRequest.ofSize(10));
        result.getContent().forEach(m -> log.info("member username = {}, teamName = {}", m.getUsername(), m.getTeamName()));
    }
}
