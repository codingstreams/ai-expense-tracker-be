package com.example.et.mapper;

import com.example.et.controller.dto.paymentmode.PaymentModeSummaryDto;
import com.example.et.model.core.PaymentMode;
import org.mapstruct.Builder;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(
    componentModel = "spring",
    builder = @Builder(disableBuilder = true)
)
public interface PaymentModeMapper {

  PaymentModeSummaryDto toDto(PaymentMode entity);

  @Mapping(target = "createdAt", ignore = true)
  @Mapping(target = "lastModifiedAt", ignore = true)
  PaymentMode toEntity(PaymentModeSummaryDto dto);

  @Mapping(target = "id", ignore = true)
  @Mapping(target = "createdAt", ignore = true)
  @Mapping(target = "lastModifiedAt", ignore = true)
  void updateEntityFromDto(PaymentModeSummaryDto dto, @MappingTarget PaymentMode entity);
}