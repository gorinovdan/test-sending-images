package org.example.service;

import org.example.entity.Image;
import org.example.entity.ImageEvent;
import org.example.repository.ImageEventRepository;
import org.example.repository.ImageRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Service
public class ImageService {
    private final ImageRepository imageRepository;
    private final ImageEventRepository imageEventRepository;
    private final ObjectMapper objectMapper;

    @Autowired
    public ImageService(ImageRepository imageRepository, ImageEventRepository imageEventRepository) {
        this.imageRepository = imageRepository;
        this.imageEventRepository = imageEventRepository;
        this.objectMapper = new ObjectMapper();
    }

    // Загрузка изображения: сохраняем файл без отправки события
    public Image uploadImage(MultipartFile file) throws Exception {
        Image image = new Image();
        image.setName(file.getOriginalFilename());
        image.setData(file.getBytes());
        image.setContentType(file.getContentType());
        image.setCreatedAt(LocalDateTime.now());
        return imageRepository.save(image);
    }

    // Получение изображения по id (без создания события)
    public Optional<Image> getImage(Long id) {
        return imageRepository.findById(id);
    }

    // Подтверждение доставки изображения – событие создаётся только если изображение гарантированно доставлено
    public void confirmDelivery(Long id) throws Exception {
        Optional<Image> imageOpt = imageRepository.findById(id);
        if (imageOpt.isEmpty()) {
            throw new Exception("Image not found with ID: " + id);
        }
        Image image = imageOpt.get();
        Map<String, Object> eventData = new HashMap<>();
        eventData.put("imageId", image.getId());
        // Формируем адрес (URL) для получения изображения; можно доработать с учетом домена
        eventData.put("url", "/api/images/" + image.getId());
        eventData.put("timestamp", LocalDateTime.now().toString());
        String eventDataJson = objectMapper.writeValueAsString(eventData);

        ImageEvent event = new ImageEvent();
        event.setImageId(image.getId());
        event.setEventType("IMAGE_DELIVERED");
        event.setEventData(eventDataJson);
        event.setTimestamp(LocalDateTime.now());
        imageEventRepository.save(event);
    }
}
