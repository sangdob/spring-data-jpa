package com.springDataJpa.study.controller;

import com.springDataJpa.study.entity.Member;
import com.springDataJpa.study.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.PostConstruct;
import java.util.Optional;

@RestController
@RequiredArgsConstructor
public class MemberController {
    private final MemberRepository memberRepository;

    @PostConstruct
    public void init() {
        for (int i = 0; i < 100; i++) {
            memberRepository.save(new Member("user" + i, i));
        }
    }

    @GetMapping("/members/{id}")
    public ResponseEntity findMemberById(@PathVariable("id") Long id) {
        Optional<Member> member = memberRepository.findById(id);

        return ResponseEntity.ok().body(member.get());
    }

    @GetMapping("/members2/{id}")
    public ResponseEntity findMember2(@PathVariable("id") Member member) {
        return ResponseEntity.ok().body(member.getUsername());
    }

    @GetMapping("/members")
    public Page<Member> list(@PageableDefault(size = 5) Pageable pageable) {
        return memberRepository.findAll(pageable);
    }
}
