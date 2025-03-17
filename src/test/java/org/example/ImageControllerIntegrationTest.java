package org.example;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.*;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.core.io.ByteArrayResource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
class ImageControllerIntegrationTest {

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
    private TestRestTemplate restTemplate;

    @Test
    void testUploadAndGetImage() {
        // Подготавливаем "фейковое" изображение
        byte[] imageBytes = "fake image content".getBytes();
        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        ByteArrayResource resource = new ByteArrayResource(imageBytes) {
            @Override
            public String getFilename() {
                return "test.png";
            }
        };
        body.add("file", resource);

        // Отправляем запрос на загрузку изображения
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);
        HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);
        ResponseEntity<String> uploadResponse = restTemplate.postForEntity("/api/images/upload", requestEntity, String.class);

        assertThat(uploadResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        String responseBody = uploadResponse.getBody();
        String prefix = "Image uploaded successfully with ID: ";
        Long imageId = Long.valueOf(responseBody.substring(prefix.length()).trim());

        // Проверяем получение изображения по id
        ResponseEntity<byte[]> getResponse = restTemplate.getForEntity("/api/images/" + imageId, byte[].class);
        assertThat(getResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        byte[] returnedImage = getResponse.getBody();
        assertThat(returnedImage).isEqualTo(imageBytes);
    }

    @Test
    void testGetNonExistingImage() {
        ResponseEntity<byte[]> response = restTemplate.getForEntity("/api/images/999999", byte[].class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void testConfirmDeliveryEndpoint() {
        // Загружаем изображение
        byte[] imageBytes = "fake image content".getBytes();
        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        ByteArrayResource resource = new ByteArrayResource(imageBytes) {
            @Override
            public String getFilename() {
                return "test.png";
            }
        };
        body.add("file", resource);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);
        HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);
        ResponseEntity<String> uploadResponse = restTemplate.postForEntity("/api/images/upload", requestEntity, String.class);
        assertThat(uploadResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        String responseBody = uploadResponse.getBody();
        String prefix = "Image uploaded successfully with ID: ";
        assert responseBody != null;
        long imageId = Long.parseLong(responseBody.substring(prefix.length()).trim());

        // Сначала получаем изображение, имитируя его доставку клиенту
        ResponseEntity<byte[]> getResponse = restTemplate.getForEntity("/api/images/" + imageId, byte[].class);
        assertThat(getResponse.getStatusCode()).isEqualTo(HttpStatus.OK);

        // Подтверждаем доставку через специальный эндпоинт
        ResponseEntity<String> confirmResponse = restTemplate.postForEntity("/api/images/" + imageId + "/confirm", null, String.class);
        assertThat(confirmResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(confirmResponse.getBody()).contains("Image delivery confirmed");
    }
}
