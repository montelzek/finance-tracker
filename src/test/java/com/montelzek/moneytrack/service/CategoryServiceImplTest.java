package com.montelzek.moneytrack.service;

import com.montelzek.moneytrack.model.Category;
import com.montelzek.moneytrack.repository.CategoryRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class CategoryServiceImplTest {

    @Mock
    private CategoryRepository categoryRepository;

    @InjectMocks
    private CategoryServiceImpl categoryService;

    @Test
    public void findByType_validType_shouldReturnListOfCategories() {
        // Arrange
        Category category1 = Category.builder()
                .name("Groceries")
                .type("EXPENSE")
                .build();
        Category category2 = Category.builder()
                .name("Entertainment")
                .type("EXPENSE")
                .build();
        when(categoryRepository.findByType("EXPENSE")).thenReturn(Arrays.asList(category1, category2));

        // Act
        List<Category> result = categoryService.findByType("EXPENSE");

        // Assert
        assertThat(result).isNotNull();
        assertThat(result).hasSize(2);
        assertThat(result.get(0).getType()).isEqualTo("EXPENSE");
        assertThat(result.get(1).getType()).isEqualTo("EXPENSE");
        verify(categoryRepository).findByType("EXPENSE");
    }

    @Test
    public void findByType_notValidType_shouldReturnEmptyList() {
        // Arrange
        when(categoryRepository.findByType("NON_EXISTENT")).thenReturn(List.of());

        // Act
        List<Category> result = categoryService.findByType("NON_EXISTENT");

        // Assert
        assertThat(result).isNotNull();
        assertThat(result).isEmpty();
        verify(categoryRepository).findByType("NON_EXISTENT");
    }

    @Test
    public void findById_validId_shouldReturnCategory() {
        // Arrange
        Category category1 = Category.builder()
                .id(1)
                .name("Groceries")
                .type("EXPENSE")
                .build();

        when(categoryRepository.findById(1)).thenReturn(Optional.of(category1));

        // Act
        Category result = categoryService.findById(1);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1);
        assertThat(result.getName()).isEqualTo("Groceries");
        assertThat(result.getType()).isEqualTo("EXPENSE");
        verify(categoryRepository).findById(1);
    }

    @Test
    public void findById_notValidId_shouldThrowRuntimeException() {
        // Arrange
        when(categoryRepository.findById(999)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatExceptionOfType(RuntimeException.class)
                .isThrownBy(() -> categoryService.findById(999))
                .withMessage("Category not found with id: 999");

        verify(categoryRepository).findById(999);
    }
}
