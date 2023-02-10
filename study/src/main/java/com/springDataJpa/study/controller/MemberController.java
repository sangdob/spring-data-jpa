package com.springDataJpa.study.controller;

import com.springDataJpa.study.entity.Member;
import com.springDataJpa.study.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
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
    public void init(){
        memberRepository.save(new Member("userA"));
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

}
