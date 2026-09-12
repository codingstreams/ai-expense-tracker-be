package com.example.et.module.reference.bank;

import com.example.et.module.reference.bank.dto.BankDto;
import org.mapstruct.Builder;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(
    componentModel = "spring",
    builder = @Builder(disableBuilder = true)
)
public interface BankMapper {

  // Entity to DTO
  BankDto toDto(Bank entity);

  // DTO to Entity (ignores BaseAudit timestamp fields)
  @Mapping(target = "createdAt", ignore = true)
  @Mapping(target = "lastModifiedAt", ignore = true)
  Bank toEntity(BankDto dto);

  // Update existing entity from DTO
  @Mapping(target = "id", ignore = true)
  @Mapping(target = "createdAt", ignore = true)
  @Mapping(target = "lastModifiedAt", ignore = true)
  void updateEntityFromDto(BankDto dto, @MappingTarget Bank entity);
}