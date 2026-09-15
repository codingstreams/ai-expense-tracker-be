package com.example.et.module.reference.category;

import com.example.et.module.reference.category.dto.CategoryDetailsResponse;
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
  CategoryDetailsResponse toDto(SystemCategory entity);

  // DTO to Entity
  @Mapping(target = "createdAt", ignore = true)
  @Mapping(target = "lastModifiedAt", ignore = true)
  SystemCategory toEntity(CategoryDetailsResponse dto);

  // Update existing entity from DTO
  @Mapping(target = "id", ignore = true)
  @Mapping(target = "createdAt", ignore = true)
  @Mapping(target = "lastModifiedAt", ignore = true)
  void updateEntityFromDto(CategoryDetailsResponse dto, @MappingTarget SystemCategory entity);
}
