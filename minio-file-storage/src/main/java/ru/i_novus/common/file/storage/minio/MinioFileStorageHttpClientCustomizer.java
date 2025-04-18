package ru.i_novus.common.file.storage.minio;

import okhttp3.OkHttpClient;

@FunctionalInterface
public interface MinioFileStorageHttpClientCustomizer {
    OkHttpClient customize(OkHttpClient client);
}
