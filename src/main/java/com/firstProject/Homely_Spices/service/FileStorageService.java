package com.firstProject.Homely_Spices.service;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Service
public class FileStorageService {

    public void deleteImageFromStorage(String imagePath) {
        try {
            String uploadDir = "src/main/resources/static/images/";
            String uploadDir2 = "target/classes/static/images";// Base directory
            Path filePath = Paths.get(uploadDir, imagePath);
            Path filePath2 = Paths.get(uploadDir2, imagePath);// Full path to the file
            Files.deleteIfExists(filePath); // Deletes the file if it exists
            Files.deleteIfExists(filePath2);
        } catch (IOException e) {
            System.out.println("Error deleting file: " + imagePath);
        }
    }

}
