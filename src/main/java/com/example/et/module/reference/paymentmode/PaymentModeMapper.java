package com.example.et.module.reference.paymentmode;

import com.example.et.module.reference.paymentmode.dto.PaymentModeDetailsResponse;
import org.mapstruct.Builder;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(
    componentModel = "spring",
    builder = @Builder(disableBuilder = true)
)
public interface PaymentModeMapper {

  PaymentModeDetailsResponse toDto(PaymentMode entity);

  @Mapping(target = "createdAt", ignore = true)
  @Mapping(target = "lastModifiedAt", ignore = true)
  PaymentMode toEntity(PaymentModeDetailsResponse dto);

  @Mapping(target = "id", ignore = true)
  @Mapping(target = "createdAt", ignore = true)
  @Mapping(target = "lastModifiedAt", ignore = true)
  void updateEntityFromDto(PaymentModeDetailsResponse dto, @MappingTarget PaymentMode entity);
}