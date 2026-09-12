package com.example.et.module.ai.insight;

import com.example.et.core.model.BaseAudit;
import com.example.et.module.auth.AppUser;
import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Entity
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class AiInsight extends BaseAudit {
  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID id;

  @Column(nullable = false)
  private String period;

  @Column(columnDefinition = "text")
  private String summary;

  private String topSpendingCategory;

  private Float topSpendingPercentage;

  @Column(columnDefinition = "text")
  private String topSpendingInsight;

  @Column(columnDefinition = "text")
  private String anomalies;

  @Column(columnDefinition = "text")
  private String actionableTips;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "app_user_id", nullable = false)
  private AppUser appUser;
}