package com.example.et.mapper;

import com.example.et.controller.dto.TransactionDto;
import com.example.et.model.core.Transaction;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

import java.util.List;

@Mapper(componentModel = "spring")
public interface TransactionMapper {
  TransactionMapper INSTANCE = Mappers.getMapper(TransactionMapper.class);

  @Mapping(target = "accountId", source = "account.id")
  @Mapping(target = "paymentModeId", source = "paymentMode.id")
  @Mapping(target = "categoryId", source = "transactionCategory.id")
  TransactionDto transactionToTransactionDto(Transaction transaction);

  List<TransactionDto> transactionDtosToTransactionDtos(List<Transaction> transactions);
}
