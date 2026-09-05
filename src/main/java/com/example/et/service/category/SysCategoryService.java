package com.example.et.service.category;

import com.example.et.model.core.SystemCategory;

import java.util.List;
import java.util.UUID;

public interface SysCategoryService {
    List<SystemCategory> getAllSystemCategories();
    SystemCategory getSystemCategoryById(UUID id);
}