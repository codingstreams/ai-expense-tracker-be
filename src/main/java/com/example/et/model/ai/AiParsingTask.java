package com.example.et.model.ai;

import com.example.et.model.core.AppUser;
import com.example.et.model.core.BaseAudit;
import com.example.et.model.core.Transaction;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.util.UUID;

@Entity
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class AiParsingTask extends BaseAudit {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    private String rawInput;
    private String content;
    private String errorMessage;

    private UUID correlationId;

    @Enumerated(EnumType.STRING)
    private Status status;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "app_user_id")
    private AppUser appUser;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "transaction_id")
    @OnDelete(action = OnDeleteAction.SET_NULL)
    private Transaction transaction;
}
