package com.example.et.module.reference.paymentmode;

import com.example.et.module.reference.paymentmode.dto.PaymentModeDto;
import org.mapstruct.Builder;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(
    componentModel = "spring",
    builder = @Builder(disableBuilder = true)
)
public interface PaymentModeMapper {

  PaymentModeDto toDto(PaymentMode entity);

  @Mapping(target = "createdAt", ignore = true)
  @Mapping(target = "lastModifiedAt", ignore = true)
  PaymentMode toEntity(PaymentModeDto dto);

  @Mapping(target = "id", ignore = true)
  @Mapping(target = "createdAt", ignore = true)
  @Mapping(target = "lastModifiedAt", ignore = true)
  void updateEntityFromDto(PaymentModeDto dto, @MappingTarget PaymentMode entity);
}