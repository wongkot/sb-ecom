package com.ecommerce.project.service;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Objects;
import java.util.UUID;

@Service
public class FileServiceImpl implements FileService {

    @Override
    public String uploadFile(String directoryPath, MultipartFile file) throws IOException {
        String originalFileName = Objects.requireNonNull(file.getOriginalFilename());
        String randomUniqueId = UUID.randomUUID().toString();
        String fileExt = originalFileName.substring(originalFileName.lastIndexOf('.'));
        String fileName = randomUniqueId.concat(fileExt);
        String filePath = directoryPath + File.separatorChar + fileName;

        File folder = new File(directoryPath);
        if (!folder.exists()) {
            boolean mkdirResult = folder.mkdir();
        }

        Files.copy(file.getInputStream(), Paths.get(filePath));

        return fileName;
    }
}
