package com.example.cafe.domain.member.repository;

import com.example.cafe.domain.member.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MemberRepository extends JpaRepository<Member, Long> {
    public Optional<Member> findByEmail(String email);

    @Query("SELECT m.id FROM Member m")
    List<Long> findAllMemberIds();
}
