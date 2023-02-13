package com.springDataJpa.study.repository;

import com.springDataJpa.study.dto.MemberDto;
import com.springDataJpa.study.entity.Member;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

import javax.persistence.LockModeType;
import javax.persistence.QueryHint;
import java.util.List;
import java.util.Optional;

public interface MemberRepository extends JpaRepository<Member, Long>, MemberRepositoryCustom, JpaSpecificationExecutor<Member>{

//    List<Member> findByUsername(String username);

    List<Member> findByUsernameAndAgeGreaterThan(String username, int age);

    List<Member> findTop3HelloBy();

    /**
     * query 필수사항이 아님 namedQuery를 우선순위로 찾아 적용하도록 만들어 져 있음
     *
     * @param username
     * @return
     */
//    @Query(name = "Member.findByUsername")
    List<Member> findByUsername(@Param("username") String username);

    @Query("select m from Member m where m.username = :username and m.age = :age")
    List<Member> findUser(@Param("username") String username, @Param("age") int age);

    @Query("select m.username from Member m")
    List<String> findUsernameList();

    @Query("select new com.springDataJpa.study.dto.MemberDto(m.id, m.username, t.name) from Member m" +
            " join Team t" +
            " on m.team.id = t.id")
    List<MemberDto> findMemberDto();

    @Query("select m from Member m where m.username in :names")
    List<Member> findByNames(@Param("names") List<String> names);

    List<Member> findListByUsername(String username);

    Member findMemberByUsername(String username);

    Optional<Member> findOptionalMemberByUsername(String username);

    Page<Member> findByAge(int age, Pageable pageable);

    Page<Member> findByUsername(String username, Pageable pageable);

    Slice<Member> findSliceByAge(int age, Pageable pageable);

    /**
     * 복잡한 상황일 경우 countQuery를 정의 가능
     *
     * @param age
     * @param pageable
     * @return
     */
    @Query(value = "select m from Member m" +
            " left join m.team t",
            countQuery = "select count(m) from Member m")
    List<Member> findListByAge(int age, Pageable pageable);

    @Modifying(clearAutomatically = true,
            flushAutomatically = true)
    @Query("update Member m set m.age = m.age + 1 where m.age >= :age")
    int bulkAgePlus(@Param("age") int age);

    @Query("select m from Member m join fetch m.team")
    List<Member> findMemberFetchJoin();

    @Override
    @EntityGraph(attributePaths = {"team"})
    List<Member> findAll();

    @EntityGraph(attributePaths = {"team"})
    @Query("select m from Member m")
    List<Member> findMemberEntityGraph();

    @EntityGraph(attributePaths = {"team"})
    List<Member> findEntityGraphByUsername(@Param("username") String username);

    @QueryHints(value= @QueryHint(name = "org.hibernate.readOnly", value="true"))
    Member findReadOnlyByUsername(String username);

    /**
     * Lock Annotation
     * jpa에서 제공하는 어노테이션
     * PESSIMISTIC_WRITE => where ? = ? for update 형식
     *
     * @param username
     * @return
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    List<Member> findLockByUsername(String username);

}
