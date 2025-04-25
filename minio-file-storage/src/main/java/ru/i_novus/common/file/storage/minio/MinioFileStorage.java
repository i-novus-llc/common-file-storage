package ru.i_novus.common.file.storage.minio;

import io.minio.BucketExistsArgs;
import io.minio.GetObjectArgs;
import io.minio.GetObjectResponse;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.RemoveObjectArgs;
import okhttp3.ConnectionPool;
import okhttp3.OkHttpClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.i_novus.common.file.storage.api.FileStorage;

import java.io.InputStream;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static java.util.Collections.emptyList;

public class MinioFileStorage implements FileStorage {

    private static final Logger logger = LoggerFactory.getLogger(MinioFileStorage.class);

    private final MinioClient minioClient;
    private final MinioFileStorageProperties properties;

    public MinioFileStorage(MinioFileStorageProperties properties) {
        this(properties, null);
    }

    public MinioFileStorage(
            MinioFileStorageProperties properties,
            List<MinioFileStorageHttpClientCustomizer> customizers
    ) {
        OkHttpClient.Builder httpClientBuilder = new OkHttpClient
                .Builder()
                .connectionPool(
                        new ConnectionPool(
                                properties.getMaxIdleConnections(),
                                properties.getKeepAliveDurationMinutes(),
                                TimeUnit.MINUTES
                        )
                )
                .connectTimeout(properties.getConnectTimeoutSeconds(), TimeUnit.SECONDS)
                .readTimeout(properties.getReadTimeoutSeconds(), TimeUnit.SECONDS)
                .writeTimeout(properties.getWriteTimeoutSeconds(), TimeUnit.SECONDS);
        List<MinioFileStorageHttpClientCustomizer> nonNullCustomizers = customizers == null ? emptyList() : customizers;
        for (MinioFileStorageHttpClientCustomizer customizer : nonNullCustomizers) {
            httpClientBuilder = customizer.customize(httpClientBuilder);
        }
        this.minioClient = MinioClient.builder()
                                      .endpoint(properties.getEndpoint())
                                      .credentials(properties.getAccessKey(), properties.getSecretKey())
                                      .httpClient(httpClientBuilder.build())
                                      .build();
        this.properties = properties;
        createBucketIfNotExists();
    }

    private void createBucketIfNotExists() {
        logger.info("Check bucket {} existence", properties.getBucket());
        boolean found;
        try {
            found = minioClient
                    .bucketExists(
                            BucketExistsArgs.builder().bucket(properties.getBucket()).build()
                    );
        } catch (Exception e) {
            throw new MinioFileStorageException("Can't check if bucket " + properties.getBucket() + " exists", e);
        }
        if (!found) {
            logger.info("Bucket {} not found. Creating it", properties.getBucket());
            try {
                minioClient.makeBucket(
                        MakeBucketArgs.builder().bucket(properties.getBucket()).build()
                );
                logger.info("Successfully created bucket {}", properties.getBucket());
            } catch (Exception e) {
                throw new MinioFileStorageException("Can't create a bucket " + properties.getBucket(), e);
            }
        } else {
            logger.info("Bucket {} already exists", properties.getBucket());
        }
    }

    @Override
    public GetObjectResponse getContent(String path) {
        logger.debug("Trying to get object {}", path);
        GetObjectResponse response;
        try {
            response = minioClient.getObject(
                    GetObjectArgs
                            .builder()
                            .bucket(properties.getBucket())
                            .object(path)
                            .build()
            );
        } catch (Exception e) {
            throw new MinioFileStorageException("Failed to get file content: " + path, e);
        }
        logger.debug("Successfully got object {}", path);
        return response;
    }

    @Override
    public String saveContent(InputStream content, String name) {
        logger.debug("Trying to save object {}", name);
        try {
            minioClient.putObject(
                    PutObjectArgs
                            .builder()
                            .bucket(properties.getBucket())
                            .object(name)
                            .stream(content, -1, properties.getPartSize())
                            .contentType("application/octet-stream")
                            .build()
            );
        } catch (Exception e) {
            throw new MinioFileStorageException("Failed to save object " + name, e);
        }
        logger.debug("Successfully saved {}", name);
        return name;
    }

    @Override
    public void removeContent(String path) {
        logger.debug("Trying to remove {}", path);
        try {
            minioClient.removeObject(
                    RemoveObjectArgs
                            .builder()
                            .bucket(properties.getBucket())
                            .object(path)
                            .build()
            );
        } catch (Exception e) {
            throw new RuntimeException("Failed to remove object " + path, e);
        }
        logger.debug("Successfully removed {}", path);
    }

}
