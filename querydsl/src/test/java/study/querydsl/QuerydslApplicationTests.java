package study.querydsl;

import com.querydsl.core.QueryResults;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import study.querydsl.entity.Member;
import study.querydsl.entity.QMember;
import study.querydsl.entity.Team;

import javax.persistence.EntityManager;

import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static study.querydsl.entity.QMember.*;

@SpringBootTest
@Transactional
@Slf4j
class QuerydslApplicationTests {
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
}
