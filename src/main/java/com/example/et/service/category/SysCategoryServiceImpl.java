package com.example.et.service.category;

import com.example.et.model.core.SystemCategory;
import com.example.et.repo.SysCategoryRepo;
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
    @Cacheable("systemCategories")
    public List<SystemCategory> getAllSystemCategories() {
        return sysCategoryRepo.findAll();
    }

    @Override
    @Cacheable(value = "systemCategories", key = "#id")
    public SystemCategory getSystemCategoryById(UUID id) {
        return sysCategoryRepo.findById(id)
            .orElseThrow(() -> new RuntimeException("SystemCategory ID: %s not found".formatted(id)));
    }
}