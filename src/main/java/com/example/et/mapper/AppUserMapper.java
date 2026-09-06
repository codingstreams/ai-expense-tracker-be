package com.example.et.mapper;

import com.example.et.controller.dto.appuser.AppUserDto;
import com.example.et.model.core.AppUser;
import org.mapstruct.Builder;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.factory.Mappers;

@Mapper(
    componentModel = "spring",
    uses = {AppUserConfigMapper.class},
    builder = @Builder(disableBuilder = true)
)
public interface AppUserMapper {

  AppUserMapper INSTANCE = Mappers.getMapper(AppUserMapper.class);

  // Entity to DTO
  AppUserDto toDto(AppUser entity);

  // DTO to Entity
  @Mapping(target = "password", ignore = true)
  @Mapping(target = "createdAt", ignore = true)
  @Mapping(target = "lastModifiedAt", ignore = true)
  AppUser toEntity(AppUserDto dto);

  // Update existing entity
  @Mapping(target = "id", ignore = true)
  @Mapping(target = "password", ignore = true)
  @Mapping(target = "createdAt", ignore = true)
  @Mapping(target = "lastModifiedAt", ignore = true)
  void updateEntityFromDto(AppUserDto dto, @MappingTarget AppUser entity);
}