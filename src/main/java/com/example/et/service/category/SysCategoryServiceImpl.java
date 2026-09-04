package com.example.et.service.category;

import com.example.et.model.core.SystemCategory;
import com.example.et.repo.SysCategoryRepo;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;

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
}