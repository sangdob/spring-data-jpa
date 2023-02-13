package com.springDataJpa.study.repository;

import com.springDataJpa.study.entity.Member;
import com.springDataJpa.study.entity.Team;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

import javax.persistence.criteria.Join;
import javax.persistence.criteria.JoinType;

public class MemberSpec {

    public static Specification<Member> teamname(final String teamname) {
        return (Specification<Member>) (root, query, criteriaBuilder) ->  {
            if (isEmpty(teamname)) {
                return null;
            }

            Join<Member, Team> team = root.join("team", JoinType.INNER);
            return criteriaBuilder.equal(team.get("name"), teamname);
        };
    }

    public static Specification<Member> username(final String username) {
        return (Specification<Member>) (root, query, criteriaBuilder) ->  {
            Join<Member, Team> user = root.join("team", JoinType.INNER);
            return criteriaBuilder.equal(user.get("username"), username);
        };
    }

    private static boolean isEmpty(String s) {
        return StringUtils.isEmpty(s);
    }
}
