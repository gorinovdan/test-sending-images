package org.example;

import org.example.entity.Image;
import org.example.entity.ImageEvent;
import org.example.repository.ImageRepository;
import org.example.repository.ImageEventRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Testcontainers
class ImageRepositoryTest {

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
    private ImageRepository imageRepository;

    @Autowired
    private ImageEventRepository imageEventRepository;

    @Test
    void testSaveAndFindImage() {
        var image = new Image();
        image.setName("repo-test.png");
        image.setContentType("image/png");
        image.setData("dummy data".getBytes());
        image.setCreatedAt(LocalDateTime.now());

        var savedImage = imageRepository.save(image);
        Optional<Image> foundImage = imageRepository.findById(savedImage.getId());
        assertThat(foundImage)
                .isPresent()
                .get()
                .extracting(Image::getName)
                .isEqualTo("repo-test.png");
    }

    @Test
    void testSaveImageEvent() {
        var event = new ImageEvent();
        event.setImageId(1L);
        event.setEventType("TEST_EVENT");
        event.setEventData("{\"key\":\"value\"}");
        event.setTimestamp(LocalDateTime.now());

        var savedEvent = imageEventRepository.save(event);
        Optional<ImageEvent> foundEvent = imageEventRepository.findById(savedEvent.getId());
        assertThat(foundEvent)
                .isPresent()
                .get()
                .extracting(ImageEvent::getEventType)
                .isEqualTo("TEST_EVENT");
    }
}
