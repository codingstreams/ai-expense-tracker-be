package com.example.et.module.transaction;

import com.example.et.module.account.AccountMapper;
import com.example.et.module.reference.category.SystemCategoryMapper;
import com.example.et.module.reference.paymentmode.PaymentModeMapper;
import com.example.et.module.transaction.dto.TransactionDto;
import com.example.et.module.transaction.dto.TransactionResponseDto;
import org.mapstruct.Builder;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(
    componentModel = "spring",
    uses = {AccountMapper.class, PaymentModeMapper.class, SystemCategoryMapper.class},
    builder = @Builder(disableBuilder = true)
)
public interface TransactionMapper {

  // Entity to DTO
  TransactionDto toDto(Transaction entity);

  // DTO to Entity
  @Mapping(target = "appUser", ignore = true)
  @Mapping(target = "createdAt", ignore = true)
  @Mapping(target = "lastModifiedAt", ignore = true)
  Transaction toEntity(TransactionDto dto);

  // Update existing entity from DTO
  @Mapping(target = "id", ignore = true)
  @Mapping(target = "appUser", ignore = true)
  @Mapping(target = "createdAt", ignore = true)
  @Mapping(target = "lastModifiedAt", ignore = true)
  void updateEntityFromDto(TransactionDto dto, @MappingTarget Transaction entity);

  // Mapping to TransactionResponseDto for existing API responses
  @Mapping(target = "account", expression = "java(entity.getAccount() != null && entity.getAccount().getBank() != null ? entity.getAccount().getBank().getName() : \"CASH\")")
  @Mapping(target = "paymentMode", expression = "java(entity.getPaymentMode() != null ? entity.getPaymentMode().getName() : \"\")")
  @Mapping(target = "category", expression = "java(entity.getTransactionCategory() != null ? entity.getTransactionCategory().getName() : \"\")")
  TransactionResponseDto toResponseDto(Transaction entity);
}
