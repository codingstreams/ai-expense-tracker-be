package com.example.et.module.user;

import com.example.et.module.reference.paymentmode.PaymentModeMapper;
import com.example.et.module.user.dto.AppUserConfigDto;
import org.mapstruct.Builder;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(
    componentModel = "spring",
    uses = {PaymentModeMapper.class},
    builder = @Builder(disableBuilder = true)
)
public interface AppUserConfigMapper {

  // Entity to DTO (MapStruct delegates paymentMode mapping to PaymentModeMapper)
  AppUserConfigDto toDto(AppUserConfig entity);

  // DTO to Entity
  @Mapping(target = "appUser", ignore = true)
  @Mapping(target = "createdAt", ignore = true)
  @Mapping(target = "lastModifiedAt", ignore = true)
  AppUserConfig toEntity(AppUserConfigDto dto);

  // Update existing entity from DTO
  @Mapping(target = "id", ignore = true)
  @Mapping(target = "appUser", ignore = true)
  @Mapping(target = "createdAt", ignore = true)
  @Mapping(target = "lastModifiedAt", ignore = true)
  void updateEntityFromDto(AppUserConfigDto dto, @MappingTarget AppUserConfig entity);
}