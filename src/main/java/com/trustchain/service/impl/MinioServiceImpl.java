package com.trustchain.service.impl;

import com.trustchain.config.MinioConfig;
import com.trustchain.service.MinioService;
import io.minio.*;
import io.minio.http.Method;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.net.URL;
import java.util.Objects;
import java.util.UUID;

@Service
public class MinioServiceImpl implements MinioService {

    private final MinioConfig config;

    private final MinioClient client;
    private static final Logger logger = LogManager.getLogger(MinioServiceImpl.class);

    @Autowired
    MinioServiceImpl(MinioConfig config) {
        this.config = config;
        this.client = MinioClient.builder()
                .endpoint(config.getEndpoint())
                .credentials(config.getAccessKey(), config.getSecretKey())
                .build();
    }

    public String upload(MultipartFile file) {
        try {
            int suffixIndex = Objects.requireNonNull(file.getOriginalFilename()).lastIndexOf(".");
            String suffix = file.getOriginalFilename().substring(suffixIndex);
            String fileName = "tmp/" + UUID.randomUUID().toString().replace("-", "") + suffix;
            client.putObject(PutObjectArgs.builder()
                    .bucket(config.getBucket())
                    .object(fileName)
                    .stream(file.getInputStream(), file.getSize(), -1)
                    .contentType(file.getContentType())
                    .build());

            if (this.presignedUrl(fileName) != null) {
                return fileName;
            } else {
                return null;
            }
        } catch (Exception e) {
            return null;
        }
    }


    public boolean copy(String oldPath, String newPath) {
        try {
            client.copyObject(CopyObjectArgs.builder()
                    .bucket(config.getBucket())
                    .object(newPath)
                    .source(CopySource.builder().bucket(config.getBucket()).object(oldPath).build())
                    .build());
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean move(String oldPath, String newPath) {
        return false;
    }

    public String presignedUrl(String file) {
        try {
            return client.getPresignedObjectUrl(GetPresignedObjectUrlArgs.builder()
                    .method(Method.GET)
                    .bucket(config.getBucket())
                    .object(file)
                    .build());
        } catch (Exception e) {
            return null;
        }
    }

    public boolean isUrl(String path) {
        try {
            new URL(path);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
