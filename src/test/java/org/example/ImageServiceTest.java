package org.example;

import org.example.entity.Image;
import org.example.repository.ImageRepository;
import org.example.repository.ImageEventRepository;
import org.example.service.ImageService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Testcontainers
class ImageServiceTest {

    @Container
    private static final PostgreSQLContainer<?> POSTGRESQL_CONTAINER = new PostgreSQLContainer<>("postgres:13")
            .withDatabaseName("test-db")
            .withUsername("postgres")
            .withPassword("postgres");

    @DynamicPropertySource
    static void overrideProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", POSTGRESQL_CONTAINER::getJdbcUrl);
        registry.add("spring.datasource.username", POSTGRESQL_CONTAINER::getUsername);
        registry.add("spring.datasource.password", POSTGRESQL_CONTAINER::getPassword);
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "update");
    }

    @Autowired
    private ImageService imageService;

    @Autowired
    private ImageRepository imageRepository;

    @Autowired
    private ImageEventRepository imageEventRepository;

    @Test
    void testUploadImage() throws Exception {
        var file = new MockMultipartFile("file", "service-test.png", "image/png", "test content".getBytes());
        Image image = imageService.uploadImage(file);
        assertThat(image.getId()).isNotNull();

        Optional<Image> foundImage = imageRepository.findById(image.getId());
        assertThat(foundImage).isPresent();

        assertThat(imageEventRepository.findAll()).isNotEmpty();
    }

    @Test
    void testGetImageAndRecordEvent() throws Exception {
        var file = new MockMultipartFile("file", "service-get-test.png", "image/png", "test content get".getBytes());
        Image image = imageService.uploadImage(file);
        Long imageId = image.getId();

        Optional<Image> retrieved = imageService.getImage(imageId);
        assertThat(retrieved).isPresent();

        long count = imageEventRepository.findAll().stream()
                .filter(event -> event.getImageId().equals(imageId) && "IMAGE_SENT".equals(event.getEventType()))
                .count();
        assertThat(count).isGreaterThan(0);
    }
}
