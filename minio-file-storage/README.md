# Minio File Storage

Java-библиотека для хранения и чтения файлов через [MinIO](https://min.io), с автоконфигурацией Spring Boot и возможностью гибкой настройки.

---

##  Подключение

Добавьте зависимость в ваш `pom.xml`:

```xml
<dependency>
  <groupId>ru.i-novus.common</groupId>
  <artifactId>minio-file-storage</artifactId>
  <version>2.4</version>
</dependency>
```

## Использование

### Минимально рабочий пример

Необходимо создать экземпляр MinioFileStorage

```java
import okhttp3.OkHttpClient;
import ru.i_novus.common.file.storage.minio.MinioFileStorage;
import ru.i_novus.common.file.storage.minio.MinioFileStorageHttpClientCustomizer;
import ru.i_novus.common.file.storage.minio.MinioFileStorageProperties;

import java.util.Arrays;

void example() {
    MinioFileStorageProperties properties = new MinioFileStorageProperties();
    properties.setEndpoint("http://localhost:9000");
    properties.setAccessKey("minioadmin");
    properties.setSecretKey("minioadmin");
    properties.setBucket("file-storage");
    MinioFileStorage fileStorage = new MinioFileStorage(properties);
}
```

### Переопределение настроек http-клиента

```java
import ru.i_novus.common.file.storage.minio.MinioFileStorage;
import ru.i_novus.common.file.storage.minio.MinioFileStorageProperties;

void example() {
    MinioFileStorageProperties properties = new MinioFileStorageProperties();
//  ...  
    MinioFileStorage fileStorage = new MinioFileStorage(
            properties,
            Arrays.asList(
                    new MinioFileStorageHttpClientCustomizer() {
                        @Override
                        public OkHttpClient.Builder customize(OkHttpClient.Builder clientBuilder) {
//                          Здесь можем переопределить какие-то свойства http-клиента, 
//                          который используется minio-клиентом  
                        }
                    }
            )
    );
}
```

### Пример использования в связке со Spring

Расширяем класс `MinioFileStorageProperties` и аннотируем его `@ConfigurationProperties`:

```java
import ru.i_novus.common.file.storage.minio.MinioFileStorageProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "common-file-storage.minio") // можно выбрать другой префикс
public class MyMinioFileStorageProperties extends MinioFileStorageProperties {
}
```

Создаем конфигурацию:

```java
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import ru.i_novus.common.file.storage.minio.MinioFileStorage;
import ru.i_novus.common.file.storage.minio.MinioFileStorageHttpClientCustomizer;

import java.util.List;

@Configuration
@EnableConfigurationProperties(MyMinioFileStorageProperties.class)
public class MinioFileStorageConfiguration {

    private final MinioFileStorageProperties properties;
    private final List<MinioFileStorageHttpClientCustomizer> customizers;

    public MinioFileStorageConfiguration(MinioFileStorageProperties properties, List<MinioFileStorageHttpClientCustomizer> customizers) {
        this.properties = properties;
        this.customizers = customizers;
    }

    @Bean
    public MinioFileStorage minioFileStorage() {
        return new MinioFileStorage(
                properties,
                customizers
        );
    }

}
```

Теперь можем настраивать клиент через `application.yaml`:

```yaml
common-file-storage:
  minio:
    endpoint: http://localhost:9000
    bucket: file-storage
    access-key: minioadmin
    secret-key: minioadmin
    part-size: 10485760
    max-idle-connections: 5
    keep-alive-duration-minutes: 5
    connect-timeout-seconds: 10
    read-timeout-seconds: 10
    write-timeout-seconds: 10
```

