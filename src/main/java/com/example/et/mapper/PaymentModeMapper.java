package com.example.et.mapper;

import com.example.et.controller.dto.paymentmode.PaymentModeDto;
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

  PaymentModeDto toDto(PaymentMode entity);

  @Mapping(target = "createdAt", ignore = true)
  @Mapping(target = "lastModifiedAt", ignore = true)
  PaymentMode toEntity(PaymentModeDto dto);

  @Mapping(target = "id", ignore = true)
  @Mapping(target = "createdAt", ignore = true)
  @Mapping(target = "lastModifiedAt", ignore = true)
  void updateEntityFromDto(PaymentModeDto dto, @MappingTarget PaymentMode entity);
}