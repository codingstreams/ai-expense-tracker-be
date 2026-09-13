package com.example.et.module.reference.paymentmode;

import com.example.et.core.exception.ApiException;
import com.example.et.core.exception.ErrorCode;
import com.example.et.module.reference.paymentmode.dto.PaymentModeDetailsResponse;
import com.example.et.module.reference.paymentmode.internal.PaymentModeRepo;
import com.example.et.module.reference.paymentmode.internal.PaymentModeServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PaymentModeServiceImplTest {

  @Mock
  private PaymentModeRepo paymentModeRepo;

  @Mock
  private PaymentModeMapper paymentModeMapper;

  @InjectMocks
  private PaymentModeServiceImpl paymentModeService;

  @Test
  void getAllPaymentModes_ShouldReturnListOfDtos_WhenPaymentModesExist() {
    UUID id = UUID.randomUUID();
    PaymentMode paymentMode = PaymentMode.builder().id(id).name("UPI").build();
    PaymentModeDetailsResponse dto = new PaymentModeDetailsResponse(id, "UPI");

    when(paymentModeRepo.findAll()).thenReturn(List.of(paymentMode));
    when(paymentModeMapper.toDto(paymentMode)).thenReturn(dto);

    List<PaymentModeDetailsResponse> result = paymentModeService.getAllPaymentModes();

    assertNotNull(result);
    assertEquals(1, result.size());
    assertEquals("UPI", result.get(0).name());
    assertEquals(id, result.get(0).id());

    verify(paymentModeRepo, times(1)).findAll();
    verify(paymentModeMapper, times(1)).toDto(paymentMode);
  }

  @Test
  void getAllPaymentModes_ShouldReturnEmptyList_WhenNoneExist() {
    when(paymentModeRepo.findAll()).thenReturn(Collections.emptyList());

    List<PaymentModeDetailsResponse> result = paymentModeService.getAllPaymentModes();

    assertNotNull(result);
    assertTrue(result.isEmpty());

    verify(paymentModeRepo, times(1)).findAll();
    verifyNoInteractions(paymentModeMapper);
  }

  @Test
  void getPaymentModeById_ShouldReturnDto_WhenFound() {
    UUID id = UUID.randomUUID();
    PaymentMode paymentMode = PaymentMode.builder().id(id).name("CREDIT_CARD").build();
    PaymentModeDetailsResponse dto = new PaymentModeDetailsResponse(id, "CREDIT_CARD");

    when(paymentModeRepo.findById(id)).thenReturn(Optional.of(paymentMode));
    when(paymentModeMapper.toDto(paymentMode)).thenReturn(dto);

    PaymentModeDetailsResponse result = paymentModeService.getPaymentModeById(id);

    assertNotNull(result);
    assertEquals(id, result.id());
    assertEquals("CREDIT_CARD", result.name());

    verify(paymentModeRepo, times(1)).findById(id);
    verify(paymentModeMapper, times(1)).toDto(paymentMode);
  }

  @Test
  void getPaymentModeById_ShouldThrowApiException_WhenNotFound() {
    UUID id = UUID.randomUUID();

    when(paymentModeRepo.findById(id)).thenReturn(Optional.empty());

    ApiException ex = assertThrows(ApiException.class, () -> paymentModeService.getPaymentModeById(id));

    assertEquals(ErrorCode.PAYMENT_MODE_NOT_FOUND, ex.getErrorCode());
    assertTrue(ex.getMessage().contains(id.toString()));

    verify(paymentModeRepo, times(1)).findById(id);
    verifyNoInteractions(paymentModeMapper);
  }
}
