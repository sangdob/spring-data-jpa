package com.springDataJpa.study.repository;

import com.springDataJpa.study.entity.Member;

import java.util.List;

public interface MemberRepositoryCustom {

    List<Member> findMemberCustom();
}
