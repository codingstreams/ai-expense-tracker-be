package com.example.et.module.account;

import com.example.et.module.account.dto.AccountDetailsResponse;
import com.example.et.module.reference.bank.BankMapper;
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
  AccountDetailsResponse toDto(Account entity);

  // DTO to Entity
  @Mapping(target = "appUser", ignore = true)
  @Mapping(target = "createdAt", ignore = true)
  @Mapping(target = "lastModifiedAt", ignore = true)
  Account toEntity(AccountDetailsResponse dto);

  // Update existing entity from DTO
  @Mapping(target = "id", ignore = true)
  @Mapping(target = "appUser", ignore = true)
  @Mapping(target = "createdAt", ignore = true)
  @Mapping(target = "lastModifiedAt", ignore = true)
  void updateEntityFromDto(AccountDetailsResponse dto, @MappingTarget Account entity);
}