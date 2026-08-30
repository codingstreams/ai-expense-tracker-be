package com.example.et.model.core;

import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@EqualsAndHashCode(callSuper = false)
@Entity
@Table(name = "app_user")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AppUser extends BaseAudit{
  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  @Column(name = "id", updatable = false, nullable = false)
  private UUID id;

  @Column(name = "name", nullable = false)
  private String name;

  @Column(name = "email", nullable = false, unique = true)
  private String email;

  @Column(name = "password", nullable = false)
  private String password;

  @Builder.Default
  @Column(name = "is_onboarding_complete", nullable = false)
  private boolean isOnboardingComplete = false;

  @OneToOne(mappedBy = "appUser", cascade = CascadeType.ALL)
  private AppUserConfig appUserConfig;
}
