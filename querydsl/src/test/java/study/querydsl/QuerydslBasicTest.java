package study.querydsl;

import com.querydsl.core.QueryResults;
import com.querydsl.core.Tuple;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import study.querydsl.entity.Member;
import study.querydsl.entity.QTeam;
import study.querydsl.entity.Team;

import javax.persistence.EntityManager;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static study.querydsl.entity.QMember.member;
import static study.querydsl.entity.QTeam.team;

@SpringBootTest
@Transactional
@Slf4j
public class QuerydslBasicTest {

    @Autowired
    EntityManager em;

    JPAQueryFactory query;

    @BeforeEach
    public void before() {
        query = new JPAQueryFactory(em);

        Team teamA = new Team("teamA");
        Team teamB = new Team("teamB");

        em.persist(teamA);
        em.persist(teamB);

        Member memberA = new Member("member1", 15, teamA);
        Member memberB = new Member("member2", 10, teamB);
        Member memberC = new Member("member3", 5, teamB);

        em.persist(memberA);
        em.persist(memberB);
        em.persist(memberC);

        em.flush();
        em.clear();
    }
    @Test
    public void startJPQL() {
        Member findMember = em.createQuery("select m from Member m where m.username = :username", Member.class)
                .setParameter("username", "member1")
                .getSingleResult();

        assertThat(findMember.getUsername()).isEqualTo("member1");
    }

    @Test
    public void startQuerydsl() {
        Member findMember = query.select(member)
                .from(member)
                .where(member.username.eq("member1"))
                .fetchOne();

        assertThat(findMember.getUsername()).isEqualTo("member1");
    }

    @Test
    public void search() {
        Member findMember = query.selectFrom(member)
                .where(member.username.eq("member1")
                        .and(member.age.eq(15)))
                .fetchOne();

        assertThat(findMember.getUsername()).isEqualTo("member1");
        assertThat(findMember.getAge()).isEqualTo(15);
    }

    @Test
    public void resultFetchTest() {
        List<Member> fetch = query.selectFrom(member)
                .fetch();

//		Member fetchOne = query.selectFrom(member)
//				.fetchOne();

        Member fetchFirst = query.selectFrom(member)
                .fetchFirst();

        QueryResults<Member> queryResult = query.selectFrom(member)
                .fetchResults();

        long total = queryResult.getTotal();
        long limit = queryResult.getLimit();
        List<Member> results = queryResult.getResults();

        log.info("{}",total);
        log.info("{}",limit);
        results.forEach(m -> log.info(m.toString()));
    }

    @Test
    public void sort() {
        em.persist(new Member(null, 100, null));
        em.persist(new Member("member5", 100, null));
        em.persist(new Member("member6", 100, null));

        List<Member> result = query.selectFrom(member)
                .where(member.age.eq(100))
                .orderBy(member.age.desc(), member.username.asc().nullsLast())
                .fetch();

        Member member5 = result.get(0);
        Member member6 = result.get(1);
        Member memberNull = result.get(2);
        assertThat(member5.getUsername()).isEqualTo("member5");
        assertThat(member6.getUsername()).isEqualTo("member6");
        assertThat(memberNull.getUsername()).isNull();
    }

    @Test
    public void paging() {
        List<Member> result = query.selectFrom(member)
                .orderBy(member.username.desc())
                .offset(1)
                .limit(2)
                .fetch();
        assertThat(result.size()).isEqualTo(2);
    }

    @Test
    public void paging2() {
        QueryResults<Member> result = query.selectFrom(member)
                .orderBy(member.username.desc())
                .offset(1)
                .limit(2)
                .fetchResults();

        assertThat(result.getTotal()).isEqualTo(3);
        assertThat(result.getLimit()).isEqualTo(2);
        assertThat(result.getOffset()).isEqualTo(1);
        assertThat(result.getResults().size()).isEqualTo(2);
    }

    @Test
    public void aggregation() throws Exception{
        List<Tuple> result = query
                .select(member.count(),
                        member.age.sum(),
                        member.age.avg(),
                        member.age.max(),
                        member.age.min())
                .from(member)
                .fetch();

        Tuple tuple = result.get(0);
        assertThat(tuple.get(member.count())).isEqualTo(3);
        assertThat(tuple.get(member.age.sum())).isEqualTo(30);
        assertThat(tuple.get(member.age.avg())).isEqualTo(10);
        assertThat(tuple.get(member.age.max())).isEqualTo(15);
        assertThat(tuple.get(member.age.min())).isEqualTo(5);
    }

    @Test
    public void group() {
        List<Tuple> fetch = query.select(team.name, member.age.avg())
                .from(member)
                .join(member.team, team)
                .groupBy(team.name)
                .fetch();

        Tuple teamA = fetch.get(0);
        Tuple teamB = fetch.get(1);

        assertThat(teamA.get(team.name)).isEqualTo("teamA");
        assertThat(teamA.get(member.age.avg())).isEqualTo(15);

        assertThat(teamB.get(team.name)).isEqualTo("teamB");
        assertThat(teamB.get(member.age.avg())).isEqualTo(7.5);
    }

    @Test
    public void join() {
        List<Member> teamA = query.selectFrom(member)
                .join(member.team, team)
                .where(team.name.eq("teamA"))
                .fetch();

        assertThat(teamA).extracting("username")
                .containsExactly("member1");
    }

    @Test
    public void leftJoin() {
        List<Member> teamA = query.selectFrom(member)
                .leftJoin(member.team, team)
                .where(team.name.eq("teamA"))
                .fetch();

        assertThat(teamA).extracting("username")
                .containsExactly("member1");
    }


    /**
     * 세타 조인 (연간관계가 없는 필드로 조인하는 방법)
     * 회원의 이름이 팀 이름과 같은 회원 조회
     */
    @Test
    public void thetaJoin() {
        em.persist(new Member("teamA"));
        em.persist(new Member("teamB"));
        em.persist(new Member("teamC"));

        List<Member> result = query.select(member)
                .from(member, team)
                .where(member.username.eq(team.name))
                .fetch();

        assertThat(result)
                .extracting("username")
                .containsExactly("teamA", "teamB");
    }

    @Test
    public void joinOnFiltering() {
        List<Tuple> result = query.select(member, team)
                .from(member)
                .leftJoin(member.team, team)
                .on(team.name.eq("teamA"))
                .fetch();

        result.forEach(t -> log.info("tuple = {}", t));
    }
}
