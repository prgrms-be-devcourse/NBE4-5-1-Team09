package com.example.cafe.domain.trade.repository;

import com.example.cafe.domain.trade.domain.entity.Trade;
import com.example.cafe.domain.trade.domain.entity.TradeStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface TradeRepository extends JpaRepository<Trade, Long> {
    Optional<Trade> findByTradeUUID(String tradeUUID);

    List<Trade> findByTradeStatus(TradeStatus tradeStatus);

    @Query("SELECT CASE WHEN COUNT(ti) > 0 THEN true ELSE false END FROM Trade t " +
            "JOIN t.tradeItems ti " +
            "WHERE t.member.id = :memberId AND ti.item.id = :itemId")
    boolean existsByMemberIdAndItemId(@Param("memberId") Long memberId, @Param("itemId") Long itemId);
}
