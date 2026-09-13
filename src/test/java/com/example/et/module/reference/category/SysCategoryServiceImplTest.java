package com.example.et.module.reference.category;

import com.example.et.core.exception.ApiException;
import com.example.et.core.exception.ErrorCode;
import com.example.et.module.reference.category.internal.SysCategoryRepo;
import com.example.et.module.reference.category.internal.SysCategoryServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SysCategoryServiceImplTest {

  @Mock
  private SysCategoryRepo sysCategoryRepo;

  @InjectMocks
  private SysCategoryServiceImpl sysCategoryService;

  @Test
  void getAllSystemCategories_ShouldReturnAllCategories_WhenCategoriesExist() {
    UUID catId = UUID.randomUUID();
    SystemCategory category = SystemCategory.builder().id(catId).name("Groceries").build();

    when(sysCategoryRepo.findAll()).thenReturn(List.of(category));

    List<SystemCategory> result = sysCategoryService.getAllSystemCategories();

    assertNotNull(result);
    assertEquals(1, result.size());
    assertEquals("Groceries", result.get(0).getName());
    assertEquals(catId, result.get(0).getId());

    verify(sysCategoryRepo, times(1)).findAll();
  }

  @Test
  void getAllSystemCategories_ShouldReturnEmptyList_WhenNoCategoriesExist() {
    when(sysCategoryRepo.findAll()).thenReturn(Collections.emptyList());

    List<SystemCategory> result = sysCategoryService.getAllSystemCategories();

    assertNotNull(result);
    assertTrue(result.isEmpty());

    verify(sysCategoryRepo, times(1)).findAll();
  }

  @Test
  void getSystemCategoryById_ShouldReturnCategory_WhenFound() {
    UUID catId = UUID.randomUUID();
    SystemCategory category = SystemCategory.builder().id(catId).name("Utilities").build();

    when(sysCategoryRepo.findById(catId)).thenReturn(Optional.of(category));

    SystemCategory result = sysCategoryService.getSystemCategoryById(catId);

    assertNotNull(result);
    assertEquals(catId, result.getId());
    assertEquals("Utilities", result.getName());

    verify(sysCategoryRepo, times(1)).findById(catId);
  }

  @Test
  void getSystemCategoryById_ShouldThrowApiException_WhenNotFound() {
    UUID catId = UUID.randomUUID();

    when(sysCategoryRepo.findById(catId)).thenReturn(Optional.empty());

    ApiException ex = assertThrows(ApiException.class, () -> sysCategoryService.getSystemCategoryById(catId));

    assertEquals(ErrorCode.CATEGORY_NOT_FOUND, ex.getErrorCode());
    assertTrue(ex.getMessage().contains(catId.toString()));

    verify(sysCategoryRepo, times(1)).findById(catId);
  }
}
