package com.springDataJpa.study.repository;

import com.springDataJpa.study.entity.Team;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TeamRepository extends JpaRepository<Team, Long> {
}
