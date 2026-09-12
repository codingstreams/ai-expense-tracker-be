package com.example.et.module.ai.insight;

import com.example.et.module.ai.dto.AiInsightDto;
import org.mapstruct.Builder;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import java.util.Arrays;
import java.util.List;

@Mapper(
    componentModel = "spring",
    builder = @Builder(disableBuilder = true)
)
public interface AiInsightMapper {

  // Entity to DTO
  @Mapping(target = "generatedAt", source = "createdAt")
  @Mapping(target = "topSpendingCategory", source = "entity", qualifiedByName = "toTopCategory")
  @Mapping(target = "anomalies", source = "anomalies", qualifiedByName = "splitString")
  @Mapping(target = "actionableTips", source = "actionableTips", qualifiedByName = "splitString")
  AiInsightDto toDto(AiInsight entity);

  // DTO to Entity
  @Mapping(target = "id", ignore = true)
  @Mapping(target = "appUser", ignore = true)
  @Mapping(target = "createdAt", source = "generatedAt")
  @Mapping(target = "lastModifiedAt", ignore = true)
  @Mapping(target = "topSpendingCategory", source = "dto.topSpendingCategory.category")
  @Mapping(target = "topSpendingPercentage", expression = "java(dto.topSpendingCategory() != null && dto.topSpendingCategory().percentage() != null ? dto.topSpendingCategory().percentage().floatValue() : null)")
  @Mapping(target = "topSpendingInsight", source = "dto.topSpendingCategory.insight")
  @Mapping(target = "anomalies", source = "anomalies", qualifiedByName = "joinList")
  @Mapping(target = "actionableTips", source = "actionableTips", qualifiedByName = "joinList")
  AiInsight toEntity(AiInsightDto dto);

  @Named("toTopCategory")
  default AiInsightDto.TopCategory toTopCategory(AiInsight entity) {
    if (entity == null || entity.getTopSpendingCategory() == null) {
      return null;
    }
    return new AiInsightDto.TopCategory(
        entity.getTopSpendingCategory(),
        entity.getTopSpendingPercentage() != null ? entity.getTopSpendingPercentage().doubleValue() : null,
        entity.getTopSpendingInsight()
    );
  }

  @Named("splitString")
  default List<String> splitString(String value) {
    if (value == null || value.isBlank()) {
      return List.of();
    }
    return Arrays.asList(value.split(";"));
  }

  @Named("joinList")
  default String joinList(List<String> list) {
    if (list == null || list.isEmpty()) {
      return null;
    }
    return String.join(";", list);
  }
}
