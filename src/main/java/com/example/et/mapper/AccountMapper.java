package com.example.et.mapper;

import com.example.et.controller.dto.account.AccountDto;
import com.example.et.model.core.Account;
import org.mapstruct.Builder;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(
    componentModel = "spring",
    uses = {BankMapper.class},
    builder = @Builder(disableBuilder = true)
)
public interface AccountMapper {
  // Entity to DTO
  AccountDto toDto(Account entity);

  // DTO to Entity
  @Mapping(target = "appUser", ignore = true)
  @Mapping(target = "createdAt", ignore = true)
  @Mapping(target = "lastModifiedAt", ignore = true)
  Account toEntity(AccountDto dto);

  // Update existing entity from DTO
  @Mapping(target = "id", ignore = true)
  @Mapping(target = "appUser", ignore = true)
  @Mapping(target = "createdAt", ignore = true)
  @Mapping(target = "lastModifiedAt", ignore = true)
  void updateEntityFromDto(AccountDto dto, @MappingTarget Account entity);
}