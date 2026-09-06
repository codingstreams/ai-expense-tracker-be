package com.example.et.mapper;

import com.example.et.controller.dto.ai.AiParsingTaskDto;
import com.example.et.model.ai.AiParsingTask;
import org.mapstruct.Builder;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(
    componentModel = "spring",
    uses = {TransactionMapper.class},
    builder = @Builder(disableBuilder = true)
)
public interface AiParsingTaskMapper {

  // Entity to DTO
  AiParsingTaskDto toDto(AiParsingTask entity);

  // DTO to Entity
  @Mapping(target = "appUser", ignore = true)
  @Mapping(target = "createdAt", ignore = true)
  @Mapping(target = "lastModifiedAt", ignore = true)
  AiParsingTask toEntity(AiParsingTaskDto dto);

  // Update existing entity from DTO
  @Mapping(target = "id", ignore = true)
  @Mapping(target = "appUser", ignore = true)
  @Mapping(target = "createdAt", ignore = true)
  @Mapping(target = "lastModifiedAt", ignore = true)
  void updateEntityFromDto(AiParsingTaskDto dto, @MappingTarget AiParsingTask entity);
}
