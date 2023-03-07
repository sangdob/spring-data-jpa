package study.querydsl;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.QueryResults;
import com.querydsl.core.Tuple;
import com.querydsl.core.types.Expression;
import com.querydsl.core.types.ExpressionUtils;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.CaseBuilder;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import study.querydsl.dto.MemberDto;
import study.querydsl.dto.QMemberDto;
import study.querydsl.dto.UserDto;
import study.querydsl.entity.Member;
import study.querydsl.entity.QMember;
import study.querydsl.entity.QTeam;
import study.querydsl.entity.Team;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.PersistenceUnit;
import java.util.List;

import static com.querydsl.core.types.Projections.*;
import static com.querydsl.jpa.JPAExpressions.*;
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

    @PersistenceUnit
    EntityManagerFactory emf;

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
                .where(usernameEq("member1"))
                .fetchOne();

        assertThat(findMember.getUsername()).isEqualTo("member1");
    }

    @Test
    public void search() {
        Member findMember = query.selectFrom(member)
                .where(usernameEq("member1")
                        .and(ageEq(15)))
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
                .where(ageEq(100))
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

    /**
     * 외부조인일 경우 활용
     */
    @Test
    public void joinWhereFiltering() {
        List<Tuple> result = query.select(member, team)
                .from(member)
                .join(member.team, team)
//                .on(team.name.eq("teamA"))
                .where(team.name.eq("teamA"))
                .fetch();

        result.forEach(t -> log.info("tuple = {}", t));
    }


    @Test
    public void fetchJoinNo() {
        em.flush();
        em.clear();

        Member findMember = query.selectFrom(member)
                .join(member.team, team)
                .where(usernameEq("member1"))
                .fetchOne();

        boolean loaded = emf.getPersistenceUnitUtil().isLoaded(findMember.getTeam());
        assertThat(loaded).as("fetch join no").isFalse();
    }

    @Test
    public void fetchJoinUse() {
        em.flush();
        em.clear();

        Member findMember = query.selectFrom(member)
                .join(member.team, team)
                .fetchJoin()
                .where(usernameEq("member1"))
                .fetchOne();

        boolean loaded = emf.getPersistenceUnitUtil().isLoaded(findMember.getTeam());
        assertThat(loaded).as("fetch join no").isTrue();
    }

    /**
     * 나이가 가장 많은 회원
     */
    @Test
    public void subQuery() {
//        Member findMember = query.select(member)
//                .from(member)
//                .join(member.team, team)
//                .fetchJoin()
//                .orderBy(member.age.desc())
//                .limit(1)
//                .fetchOne();

        QMember subMember = new QMember("memberSub");

        List<Member> result = query.selectFrom(member)
                .where(member.age.eq(
                        select(subMember.age.max())
                                .from(subMember)
                ))
                .fetch();

        log.info("old one = {}", result.get(0).toString());

        assertThat(result).extracting("age")
                .containsExactly(15);
    }
    
    /**
     * 나이가 평균 이상인 회원
     */
    @Test
    public void subQueryGoe() {
        QMember subMember = new QMember("memberSub");
        
        List<Member> result = query
                .selectFrom(member)
                .where(member.age.goe(
                        select(subMember.age.avg())
                                .from(subMember)
                ))
                .fetch();

        result.forEach(m -> log.info("{}", m.toString()));

//        순서가 섞일 경우 테스트에 지장이 간다..... list 순서대로 테스팅
        assertThat(result).extracting("age")
                .containsExactly(15, 10);
    }

    @Test
    public void selectSubQuery() {
        QMember subMember = new QMember("memberSub");

        List<Tuple> result = query.select(member.username,
                        select(subMember.age.avg())
                                .from(subMember))
                .from(member)
                .fetch();

        result.forEach(r -> log.info("result = {}", r));

    }

    @Test
    public void basicCase() {
        List<String> caseResult = query.select(member.age
                        .when(10).then("10years")
                        .when(15).then("15years")
                        .otherwise("others"))
                .from(member)
                .fetch();

        caseResult.forEach(r -> log.info("member years = {}", r));

    }

    @Test
    public void complexCase() {
        List<String> result = query.select(new CaseBuilder()
                        .when(member.age.between(0, 10)).then("0~10")
                        .when(member.age.between(10, 20)).then("10~20")
                        .otherwise("others"))
                .from()
                .fetch();

        result.forEach(r -> log.info("member years = {}", r));
    }

    @Test
    public void constant() {
        List<Tuple> constant = query.select(member.username
                        , Expressions.constant("A"))
                .from(member)
                .fetch();

        constant.forEach(r -> log.info("member = {}", r));
    }

    @Test
    public void concat() {
        List<String> result = query.select(member.username.concat("_").concat(member.age.stringValue()))
                .from(member)
                .fetch();

        result.forEach(r -> log.info("member = {}", r));
    }

    @Test
    public void simpleProjection() {
        List<String> result = query.select(member.username)
                .from(member)
                .fetch();

        result.forEach(r -> log.info("result = {}", r));
    }

    @Test
    public void tupleProjection() {
        List<Tuple> result = query.select(member.username
                        , member.age)
                .from(member)
                .fetch();

        result.forEach(r -> log.info("result = {}", r));
    }

    @Test
    public void findDtoByJPQL() {
        List<MemberDto> result = em.createQuery("select new study.querydsl.dto.MemberDto(m.username, m.age) " +
                        "from Member m"
                        , MemberDto.class)
                .getResultList();

        result.forEach(r -> log.info(r.toString()));
    }

    @Test
    public void findDtoBySetter() {
        List<MemberDto> result = query
                .select(bean(MemberDto.class
                        , member.username
                        , member.age))
                .from(member)
                .fetch();

        result.forEach(r -> log.info(r.toString()));
    }

    @Test
    public void findDtoByField() {
        List<MemberDto> result = query
                .select(fields(MemberDto.class
                        , member.username
                        , member.age))
                .from(member)
                .fetch();

        result.forEach(r -> log.info(r.toString()));
    }

    /**
     * 이름이 다를 경우 컬럼의 이름을 변경
     */
    @Test
    public void findUserDto() {
        List<UserDto> result = query
                .select(fields(UserDto.class
                        , member.username.as("name")
                        , member.age))
                .from(member)
                .fetch();

        result.forEach(r -> log.info(r.toString()));
    }

    @Test
    public void findUserDtoSubQuery() {
        QMember subMember = new QMember("subMember");

        List<UserDto> result = query
                .select(fields(UserDto.class
                        , member.username.as("name")
                        , ExpressionUtils
                                .as(select(subMember.age.max())
                                        .from(subMember), "age")
                ))
                .from(member)
                .fetch();

        result.forEach(r -> log.info(r.toString()));
    }

    @Test
    public void findDtoByConstructor() {
        List<MemberDto> result = query
                .select(constructor(MemberDto.class
                        , member.username
                        , member.age))
                .from(member)
                .fetch();

        result.forEach(r -> log.info(r.toString()));
    }

    @Test
    public void findDtoByQueryProjection() {
        List<MemberDto> result = query.select(new QMemberDto(member.username, member.age))
                .from(member)
                .fetch();

        result.forEach(r -> log.info(r.toString()));
    }

    @Test
    public void dynamicQuery_BooleanBuilder() {
        String usernameParam = "member1";
        Integer ageParam = 15;

        List<Member> members = searchMember(usernameParam, ageParam);

        assertThat(members.size()).isEqualTo(1);
    }

    private List<Member> searchMember(String usernameParam, Integer ageParam) {
        BooleanBuilder builder = new BooleanBuilder();

        if (usernameParam != null) {
            builder.and(usernameEq(usernameParam));
        }

        if (ageParam != null) {
            builder.and(ageEq(ageParam));
        }

        return query
                .selectFrom(member)
                .where(builder)
                .fetch();
    }

    @Test
    public void dynamicQuery_WhereParam() {
        String usernameParam = "member1";
        Integer ageParam = 15;

        List<Member> result = searchMember2(usernameParam, ageParam);

        assertThat(result.size()).isEqualTo(1);
        assertThat(result.get(0).getUsername()).isEqualTo(usernameParam);
        assertThat(result.get(0).getAge()).isEqualTo(ageParam);
    }

    @Test
    public void bulkUpdate() {
//      lt : this < right

        // member 1 = 10
        // member 2 = 5
        long count = query.update(member)
                .set(member.username, "비회원")
                .where(member.age.lt(15))
                .execute();

        List<Member> result = query
                .selectFrom(member)
                .fetch();

        result.forEach(r -> log.info(r.toString()));

        assertThat(count).isEqualTo(2);
    }

    @Test
    public void bulkAdd() {
        long count = query.update(member)
                .set(member.age, member.age.add(1))
                .execute();
    }

    @Test
    public void bulkDelete() {
    }

    private List<Member> searchMember2(String usernameParam, Integer ageParam) {
        return query.selectFrom(member)
                .where(usernameEq(usernameParam), ageEq(ageParam))
                .fetch();
    }

    private BooleanExpression usernameEq(String usernameParam) {
        return usernameParam != null ? member.username.eq(usernameParam) : null;
    }

    private BooleanExpression ageEq(Integer ageParam) {
        return ageParam != null ? member.age.eq(ageParam) : null;
    }

    private BooleanExpression allEq(String usernameCond, Integer ageCond){
        return usernameEq(usernameCond).and(ageEq(ageCond));
    }
}
