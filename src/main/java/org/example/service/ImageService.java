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

    public Image uploadImage(MultipartFile file) throws Exception {
        Image image = new Image();
        image.setName(file.getOriginalFilename());
        image.setData(file.getBytes());
        image.setContentType(file.getContentType());
        image.setCreatedAt(LocalDateTime.now());

        Image savedImage = imageRepository.save(image);

        // Сохранение события "IMAGE_UPLOADED"
        Map<String, Object> eventData = new HashMap<>();
        eventData.put("name", savedImage.getName());
        eventData.put("contentType", savedImage.getContentType());
        eventData.put("timestamp", savedImage.getCreatedAt().toString());

        String eventDataJson = objectMapper.writeValueAsString(eventData);

        ImageEvent event = new ImageEvent();
        event.setImageId(savedImage.getId());
        event.setEventType("IMAGE_UPLOADED");
        event.setEventData(eventDataJson);
        event.setTimestamp(LocalDateTime.now());
        imageEventRepository.save(event);

        return savedImage;
    }

    public Optional<Image> getImage(Long id) {
        Optional<Image> imageOpt = imageRepository.findById(id);
        imageOpt.ifPresent(image -> {
            try {
                // Сохранение события "IMAGE_SENT"
                Map<String, Object> eventData = new HashMap<>();
                eventData.put("imageId", image.getId());
                eventData.put("timestamp", LocalDateTime.now().toString());

                String eventDataJson = objectMapper.writeValueAsString(eventData);

                ImageEvent event = new ImageEvent();
                event.setImageId(image.getId());
                event.setEventType("IMAGE_SENT");
                event.setEventData(eventDataJson);
                event.setTimestamp(LocalDateTime.now());
                imageEventRepository.save(event);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        return imageOpt;
    }
}