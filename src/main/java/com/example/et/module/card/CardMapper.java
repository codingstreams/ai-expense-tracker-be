package com.example.et.module.card;

import com.example.et.module.card.dto.CardResponse;
import org.mapstruct.Builder;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(
    componentModel = "spring",
    builder = @Builder(disableBuilder = true)
)
public interface CardMapper {

  // Entity to DTO
  @Mapping(target = "accountId", source = "account.id")
  @Mapping(target = "limit", source = "account.balance")
  @Mapping(target = "bank", source = "account.bank")
  CardResponse toDto(Card entity);

  // DTO to Entity
  @Mapping(target = "appUser", ignore = true)
  @Mapping(target = "account", ignore = true)
  @Mapping(target = "createdAt", ignore = true)
  @Mapping(target = "lastModifiedAt", ignore = true)
  Card toEntity(CardResponse dto);

  // Update existing entity from DTO
  @Mapping(target = "id", ignore = true)
  @Mapping(target = "appUser", ignore = true)
  @Mapping(target = "account", ignore = true)
  @Mapping(target = "createdAt", ignore = true)
  @Mapping(target = "lastModifiedAt", ignore = true)
  void updateEntityFromDto(CardResponse dto, @MappingTarget Card entity);
}
