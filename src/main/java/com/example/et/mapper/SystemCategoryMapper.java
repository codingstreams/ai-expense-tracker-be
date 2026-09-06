package com.example.et.mapper;

import com.example.et.controller.dto.category.SystemCategoryDto;
import com.example.et.model.core.SystemCategory;
import org.mapstruct.Builder;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(
    componentModel = "spring",
    builder = @Builder(disableBuilder = true)
)
public interface SystemCategoryMapper {

  // Entity to DTO
  SystemCategoryDto toDto(SystemCategory entity);

  // DTO to Entity
  @Mapping(target = "createdAt", ignore = true)
  @Mapping(target = "lastModifiedAt", ignore = true)
  SystemCategory toEntity(SystemCategoryDto dto);

  // Update existing entity from DTO
  @Mapping(target = "id", ignore = true)
  @Mapping(target = "createdAt", ignore = true)
  @Mapping(target = "lastModifiedAt", ignore = true)
  void updateEntityFromDto(SystemCategoryDto dto, @MappingTarget SystemCategory entity);
}
