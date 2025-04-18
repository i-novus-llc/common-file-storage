# Minio File Storage

Java-библиотека для хранения и чтения файлов через [MinIO](https://min.io), с автоконфигурацией Spring Boot и возможностью гибкой настройки.

---

##  Подключение

Добавьте зависимость в ваш `pom.xml`:

```xml
<dependency>
  <groupId>ru.i-novus.common</groupId>
  <artifactId>minio-file-storage</artifactId>
  <version>2.3</version>
</dependency>
```

Укажите необходимые настройки:

```yaml
common-file-storage:
  minio:
    endpoint: http://localhost:9000
    bucket: file-storage
    access-key: minioadmin
    secret-key: minioadmin
    enabled: true
    part-size: 10485760
    max-idle-connections: 5
    keep-alive-duration-minutes: 5
    connect-timeout-seconds: 10
    read-timeout-seconds: 10
    write-timeout-seconds: 10
```

Настройка `common-file-storage.minio.enabled` контролирует включение/выключение MinioFileStorage. 
