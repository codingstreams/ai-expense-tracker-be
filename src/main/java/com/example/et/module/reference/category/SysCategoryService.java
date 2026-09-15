package com.example.et.module.reference.category;

import java.util.List;
import java.util.UUID;

public interface SysCategoryService {
  List<SystemCategory> getAllSystemCategories();

  SystemCategory getSystemCategoryById(UUID id);
}