package com.example.et.module.reference.category.internal;

import com.example.et.core.config.CacheNames;
import com.example.et.module.reference.category.SysCategoryService;
import com.example.et.module.reference.category.SystemCategory;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class SysCategoryServiceImpl implements SysCategoryService {
    private final SysCategoryRepo sysCategoryRepo;

    @Override
    @Transactional
    @Cacheable(value = CacheNames.SYSTEM_CATEGORIES)
    public List<SystemCategory> getAllSystemCategories() {
        return sysCategoryRepo.findAll();
    }

    @Override
    @Cacheable(value = CacheNames.SYSTEM_CATEGORIES, key = "#id")
    public SystemCategory getSystemCategoryById(UUID id) {
        return sysCategoryRepo.findById(id)
            .orElseThrow(() -> new RuntimeException("SystemCategory ID: %s not found".formatted(id)));
    }
}