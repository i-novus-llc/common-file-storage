package ru.i_novus.common.file.storage.minio;

import io.minio.MinioClient;
import okhttp3.ConnectionPool;
import okhttp3.OkHttpClient;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import ru.i_novus.common.file.storage.api.FileStorage;

import java.util.List;
import java.util.concurrent.TimeUnit;

@Configuration
@AutoConfiguration
@EnableConfigurationProperties(MinioFileStorageProperties.class)
@ConditionalOnProperty(
        prefix = "common-file-storage.minio",
        name = "enabled",
        havingValue = "true",
        matchIfMissing = true
)
public class MinioFileStorageAutoConfiguration {

    @Bean
    public MinioClient minioClient(
            MinioFileStorageProperties properties,
            List<MinioFileStorageHttpClientCustomizer> customizers
    ) {
        OkHttpClient httpClient = new OkHttpClient
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
                .writeTimeout(properties.getWriteTimeoutSeconds(), TimeUnit.SECONDS)
                .build();
        for (MinioFileStorageHttpClientCustomizer customizer : customizers) {
            httpClient = customizer.customize(httpClient);
        }
        return MinioClient.builder()
                          .endpoint(properties.getEndpoint())
                          .credentials(properties.getAccessKey(), properties.getSecretKey())
                          .httpClient(httpClient)
                          .build();
    }

    @Bean
    public FileStorage minioFileStorage(MinioClient client, MinioFileStorageProperties properties) {
        return new MinioFileStorage(client, properties);
    }

}
