package com.example.cafe.domain.trade.domain.entity;

import com.example.cafe.domain.member.entity.Member;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Data
@Table(name = "trade")
@EntityListeners(AuditingEntityListener.class)
public class Trade {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "trade_id")
    @Setter(AccessLevel.PRIVATE)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    private Member member;

    private String email;


    @Enumerated(EnumType.STRING)
    private TradeStatus tradeStatus;


    @OneToMany(mappedBy = "trade", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<TradeItem> tradeItems;

    private Integer totalPrice;

    private String address;

    @Column(name = "tradeUUID")
    private String tradeUUID;

    @CreatedDate
    @Setter(AccessLevel.PRIVATE)
    private LocalDateTime tradeRequestDate;

    @LastModifiedDate
    @Setter(AccessLevel.PRIVATE)
    private LocalDateTime tradeUpdatedDate;

    public void addTradeItem(TradeItem tradeItem) {
        this.tradeItems.add(tradeItem);
        tradeItem.setTrade(this); // 연관관계 설정
    }
}
