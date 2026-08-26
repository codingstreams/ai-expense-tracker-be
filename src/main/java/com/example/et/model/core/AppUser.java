package com.example.et.model.core;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.ColumnDefault;

import java.util.UUID;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AppUser extends BaseAudit {
  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID id;

  @Column(name = "name", nullable = false)
  private String name;

  @Column(name = "email", nullable = false, unique = true)
  private String email;

  @Column(name = "password", nullable = false)
  private String password;

  @Column(name = "is_onboarding_complete", nullable = false)
  @ColumnDefault("false")
  private Boolean isOnboardingComplete;

  @OneToOne(mappedBy = "appUser", cascade = CascadeType.ALL)
  private AppUserConfig appUserConfig;

  public static AppUser ofId(String userId) {
    return AppUser.builder().id(UUID.fromString(userId)).build();
  }
}
