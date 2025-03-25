package com.montelzek.moneytrack.repository;

import com.montelzek.moneytrack.model.Category;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jdbc.EmbeddedDatabaseConnection;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase(connection = EmbeddedDatabaseConnection.H2)
public class CategoryRepositoryTest {

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private TestEntityManager testEntityManager;

    @BeforeEach
    void setup() {
        testEntityManager.persist(Category.builder()
                .name("Groceries")
                .type("TEST_TYPE")
                .build()
        );
        testEntityManager.persist(Category.builder()
                .name("Dining out")
                .type("TEST_TYPE")
                .build()
        );
        testEntityManager.persist(Category.builder()
                .name("Salary")
                .type("ASSET")
                .build()
        );
    }

    @Test
    void testFindByType_shouldReturnMatchingCategories() {
        // Act
        List<Category> categoryList = categoryRepository.findByType("TEST_TYPE");

        // Assert
        assertThat(categoryList).hasSize(2);
        for (Category c : categoryList) {
            assertThat(c.getType()).isEqualTo("TEST_TYPE");
        }
    }

    @Test
    void testFindByType_shouldReturnEmptyList() {
        // Act
        List<Category> categoryList = categoryRepository.findByType("NON_EXISTENT_TYPE");

        // Assert
        assertThat(categoryList).isEmpty();
    }
}
