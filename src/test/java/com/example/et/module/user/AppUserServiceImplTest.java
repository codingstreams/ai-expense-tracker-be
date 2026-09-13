package com.example.et.module.user;

import com.example.et.module.reference.paymentmode.PaymentMode;
import com.example.et.module.reference.paymentmode.internal.PaymentModeRepo;
import com.example.et.module.user.dto.AppUserDto;
import com.example.et.module.user.dto.UpdateUserConfigReq;
import com.example.et.module.user.dto.UpdateUserDetailsDto;
import com.example.et.module.user.dto.UserDetailsDto;
import com.example.et.module.user.internal.AppUserConfigRepo;
import com.example.et.module.user.internal.AppUserRepo;
import com.example.et.module.user.internal.AppUserServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AppUserServiceImplTest {

  @Mock
  private AppUserRepo appUserRepo;

  @Mock
  private AppUserConfigRepo appUserConfigRepo;

  @Mock
  private PaymentModeRepo paymentModeRepo;

  @Mock
  private AppUserMapper appUserMapper;

  @InjectMocks
  private AppUserServiceImpl appUserService;

  private UUID userUuid;
  private String userId;
  private AppUser sampleUser;

  @BeforeEach
  void setUp() {
    userUuid = UUID.randomUUID();
    userId = userUuid.toString();
    sampleUser = AppUser.builder()
        .id(userUuid)
        .name("John Doe")
        .email("john@example.com")
        .password("encoded-password")
        .onboardingComplete(true)
        .build();
  }

  // ----------------------------------------------------------------------
  // checkUserExists & saveUser & checkIsUserOnboardedByEmail
  // ----------------------------------------------------------------------
  @Test
  void checkUserExists_ShouldReturnTrue_WhenExists() {
    when(appUserRepo.existsByEmail("john@example.com")).thenReturn(true);

    assertTrue(appUserService.checkUserExists("john@example.com"));
    verify(appUserRepo, times(1)).existsByEmail("john@example.com");
  }

  @Test
  void checkUserExists_ShouldReturnFalse_WhenNotExists() {
    when(appUserRepo.existsByEmail("unknown@example.com")).thenReturn(false);

    assertFalse(appUserService.checkUserExists("unknown@example.com"));
  }

  @Test
  void saveUser_ShouldSaveAndReturnUser() {
    when(appUserRepo.save(sampleUser)).thenReturn(sampleUser);

    AppUser saved = appUserService.saveUser(sampleUser);

    assertNotNull(saved);
    assertEquals("John Doe", saved.getName());
    verify(appUserRepo, times(1)).save(sampleUser);
  }

  @Test
  void checkIsUserOnboardedByEmail_ShouldReturnTrue_WhenOnboarded() {
    when(appUserRepo.existsByEmailAndOnboardingComplete("john@example.com", true)).thenReturn(true);

    assertTrue(appUserService.checkIsUserOnboardedByEmail("john@example.com"));
  }

  // ----------------------------------------------------------------------
  // getUserByUserIdWithConfig
  // ----------------------------------------------------------------------
  @Test
  void getUserByUserIdWithConfig_ShouldReturnUserDetailsDto() {
    UserDetailsDto dto = new UserDetailsDto(
        "john@example.com", "John", true,
        AppUserConfig.LanguagePreference.EN, 50000, AppUserConfig.Currency.INR, "UPI"
    );

    when(appUserRepo.findByIdWithUserConfig(userUuid)).thenReturn(dto);

    UserDetailsDto result = appUserService.getUserByUserIdWithConfig(userId);

    assertNotNull(result);
    assertEquals("john@example.com", result.email());
    verify(appUserRepo, times(1)).findByIdWithUserConfig(userUuid);
  }

  // ----------------------------------------------------------------------
  // updateUserConfig
  // ----------------------------------------------------------------------
  @Test
  void updateUserConfig_ShouldUpdateConfigAndReturnDto() {
    AppUserConfig config = AppUserConfig.builder()
        .id(UUID.randomUUID())
        .appUser(sampleUser)
        .spendLimit(10000)
        .build();

    PaymentMode paymentMode = PaymentMode.builder().id(UUID.randomUUID()).name("UPI").build();

    UpdateUserDetailsDto updateDto = new UpdateUserDetailsDto(
        AppUserConfig.LanguagePreference.HI,
        25000,
        AppUserConfig.Currency.INR,
        "UPI",
        true
    );

    when(appUserConfigRepo.findByUserId(userUuid)).thenReturn(Optional.of(config));
    when(paymentModeRepo.findByNameIgnoreCase("UPI")).thenReturn(Optional.of(paymentMode));
    when(appUserConfigRepo.save(config)).thenReturn(config);

    UpdateUserDetailsDto result = appUserService.updateUserConfig(userId, updateDto);

    assertNotNull(result);
    assertEquals(AppUserConfig.LanguagePreference.HI, config.getLanguagePreference());
    assertEquals(25000, config.getSpendLimit());
    assertEquals(AppUserConfig.Currency.INR, config.getCurrency());
    assertEquals(paymentMode, config.getPaymentMode());
    assertTrue(sampleUser.isOnboardingComplete());
    verify(appUserConfigRepo, times(1)).save(config);
  }

  @Test
  void updateUserConfig_ShouldThrowException_WhenUserNotFound() {
    UpdateUserDetailsDto updateDto = new UpdateUserDetailsDto(
        AppUserConfig.LanguagePreference.EN, 10000, AppUserConfig.Currency.INR, "UPI", true
    );

    when(appUserConfigRepo.findByUserId(userUuid)).thenReturn(Optional.empty());

    assertThrows(RuntimeException.class, () -> appUserService.updateUserConfig(userId, updateDto));
    verify(appUserConfigRepo, never()).save(any());
  }

  @Test
  void updateUserConfig_ShouldThrowException_WhenPaymentModeNotFound() {
    AppUserConfig config = AppUserConfig.builder()
        .id(UUID.randomUUID())
        .appUser(sampleUser)
        .build();

    UpdateUserDetailsDto updateDto = new UpdateUserDetailsDto(
        AppUserConfig.LanguagePreference.EN, 10000, AppUserConfig.Currency.INR, "INVALID_MODE", true
    );

    when(appUserConfigRepo.findByUserId(userUuid)).thenReturn(Optional.of(config));
    when(paymentModeRepo.findByNameIgnoreCase("INVALID_MODE")).thenReturn(Optional.empty());

    assertThrows(RuntimeException.class, () -> appUserService.updateUserConfig(userId, updateDto));
    verify(appUserConfigRepo, never()).save(any());
  }

  // ----------------------------------------------------------------------
  // getUserByEmail & getUserById
  // ----------------------------------------------------------------------
  @Test
  void getUserByEmail_ShouldReturnUser_WhenFound() {
    when(appUserRepo.findByEmail("john@example.com")).thenReturn(Optional.of(sampleUser));

    AppUser result = appUserService.getUserByEmail("john@example.com");

    assertNotNull(result);
    assertEquals("John Doe", result.getName());
  }

  @Test
  void getUserByEmail_ShouldThrowUsernameNotFoundException_WhenNotFound() {
    when(appUserRepo.findByEmail("missing@example.com")).thenReturn(Optional.empty());

    assertThrows(UsernameNotFoundException.class, () -> appUserService.getUserByEmail("missing@example.com"));
  }

  @Test
  void getUserById_ShouldReturnUser_WhenFound() {
    when(appUserRepo.findById(userUuid)).thenReturn(Optional.of(sampleUser));

    AppUser result = appUserService.getUserById(userId);

    assertNotNull(result);
    assertEquals(userUuid, result.getId());
  }

  @Test
  void getUserById_ShouldThrowUsernameNotFoundException_WhenNotFound() {
    when(appUserRepo.findById(userUuid)).thenReturn(Optional.empty());

    assertThrows(UsernameNotFoundException.class, () -> appUserService.getUserById(userId));
  }

  // ----------------------------------------------------------------------
  // getUserByUserIdWithConfigV2 & updateUserConfigV2
  // ----------------------------------------------------------------------
  @Test
  void getUserByUserIdWithConfigV2_ShouldReturnDto_WhenFound() {
    AppUserDto appUserDto = new AppUserDto("John Doe", "john@example.com", true, null);

    when(appUserRepo.findById(userUuid)).thenReturn(Optional.of(sampleUser));
    when(appUserMapper.toDto(sampleUser)).thenReturn(appUserDto);

    AppUserDto result = appUserService.getUserByUserIdWithConfigV2(userId);

    assertNotNull(result);
    assertEquals("John Doe", result.name());
  }

  @Test
  void getUserByUserIdWithConfigV2_ShouldThrowException_WhenNotFound() {
    when(appUserRepo.findById(userUuid)).thenReturn(Optional.empty());

    assertThrows(RuntimeException.class, () -> appUserService.getUserByUserIdWithConfigV2(userId));
  }

  @Test
  void updateUserConfigV2_ShouldUpdateAndReturnDto_WhenValid() {
    UUID paymentModeId = UUID.randomUUID();
    PaymentMode paymentMode = PaymentMode.builder().id(paymentModeId).name("CARD").build();
    AppUserConfig config = AppUserConfig.builder().id(UUID.randomUUID()).appUser(sampleUser).build();
    sampleUser.setAppUserConfig(config);

    UpdateUserConfigReq req = new UpdateUserConfigReq(
        AppUserConfig.LanguagePreference.EN, 60000, AppUserConfig.Currency.INR, paymentModeId.toString(), true
    );

    AppUserDto responseDto = new AppUserDto("John Doe", "john@example.com", true, null);

    when(appUserRepo.findById(userUuid)).thenReturn(Optional.of(sampleUser));
    when(paymentModeRepo.findById(paymentModeId)).thenReturn(Optional.of(paymentMode));
    when(appUserRepo.save(sampleUser)).thenReturn(sampleUser);
    when(appUserMapper.toDto(sampleUser)).thenReturn(responseDto);

    AppUserDto result = appUserService.updateUserConfigV2(userId, req);

    assertNotNull(result);
    assertEquals(60000, config.getSpendLimit());
    assertEquals(paymentMode, config.getPaymentMode());
    verify(appUserRepo, times(1)).save(sampleUser);
  }

  // ----------------------------------------------------------------------
  // loadUserByUsername
  // ----------------------------------------------------------------------
  @Test
  void loadUserByUsername_ShouldLoadByUuid_WhenIdentifierIsUuid() {
    when(appUserRepo.findById(userUuid)).thenReturn(Optional.of(sampleUser));

    UserDetails userDetails = appUserService.loadUserByUsername(userId);

    assertNotNull(userDetails);
    assertEquals(userId, userDetails.getUsername());
    assertEquals("encoded-password", userDetails.getPassword());
    assertTrue(userDetails.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_USER")));
  }

  @Test
  void loadUserByUsername_ShouldLoadByEmail_WhenIdentifierIsNotUuid() {
    when(appUserRepo.findByEmail("john@example.com")).thenReturn(Optional.of(sampleUser));

    UserDetails userDetails = appUserService.loadUserByUsername("john@example.com");

    assertNotNull(userDetails);
    assertEquals(userId, userDetails.getUsername());
    verify(appUserRepo, times(1)).findByEmail("john@example.com");
  }

  @Test
  void loadUserByUsername_ShouldThrowUsernameNotFoundException_WhenNeitherFound() {
    when(appUserRepo.findByEmail("notfound@example.com")).thenReturn(Optional.empty());

    assertThrows(UsernameNotFoundException.class, () -> appUserService.loadUserByUsername("notfound@example.com"));
  }
}
