package com.example.et.module.dataseeder;

import com.example.et.module.account.AccountService;
import com.example.et.module.auth.AuthService;
import com.example.et.module.auth.dto.RegisterUserRequest;
import com.example.et.module.card.CardService;
import com.example.et.module.dashboard.DashboardService;
import com.example.et.module.dataseeder.internal.DataSeederServiceImpl;
import com.example.et.module.reference.bank.BankMapper;
import com.example.et.module.reference.bank.internal.BankRepo;
import com.example.et.module.reference.category.internal.SysCategoryRepo;
import com.example.et.module.reference.paymentmode.internal.PaymentModeRepo;
import com.example.et.module.transaction.TransactionService;
import com.example.et.module.user.AppUserService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DataSeederServiceImplTest {

  @Mock
  private AuthService authService;

  @Mock
  private AppUserService appUserService;

  @Mock
  private DashboardService dashboardService;

  @Mock
  private AccountService accountService;

  @Mock
  private CardService cardService;

  @Mock
  private TransactionService transactionsService;

  @Mock
  private BankRepo bankRepo;

  @Mock
  private PaymentModeRepo paymentModeRepo;

  @Mock
  private SysCategoryRepo systemCategoryRepo;

  @Mock
  private BankMapper bankMapper;

  @InjectMocks
  private DataSeederServiceImpl dataSeederService;

  @Test
  void seedUsers_ShouldRegisterSpecifiedNumberOfUsers() {
    when(authService.register(any(RegisterUserRequest.class))).thenReturn(null);

    int result = dataSeederService.seedUsers(1);

    assertEquals(1, result);
    verify(authService, times(1)).register(any(RegisterUserRequest.class));
  }

  @Test
  void seedUsers_ZeroCount_ShouldNotCallRegister() {
    int result = dataSeederService.seedUsers(0);

    assertEquals(0, result);
    verifyNoInteractions(authService);
  }
}
