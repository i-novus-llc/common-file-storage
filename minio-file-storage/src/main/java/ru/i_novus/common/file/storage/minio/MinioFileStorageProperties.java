package ru.i_novus.common.file.storage.minio;

public class MinioFileStorageProperties {

    private String endpoint;
    private String accessKey;
    private String secretKey;
    private String bucket;
    private int partSize = 10 * 1024 * 1024;
    private int maxIdleConnections = 5;
    private long keepAliveDurationMinutes = 5;
    private long connectTimeoutSeconds = 10;
    private long readTimeoutSeconds = 10;
    private long writeTimeoutSeconds = 10;

    public String getEndpoint() {
        return endpoint;
    }

    public void setEndpoint(String endpoint) {
        this.endpoint = endpoint;
    }

    public String getAccessKey() {
        return accessKey;
    }

    public void setAccessKey(String accessKey) {
        this.accessKey = accessKey;
    }

    public String getSecretKey() {
        return secretKey;
    }

    public void setSecretKey(String secretKey) {
        this.secretKey = secretKey;
    }

    public String getBucket() {
        return bucket;
    }

    public void setBucket(String bucket) {
        this.bucket = bucket;
    }

    public int getPartSize() {
        return partSize;
    }

    public void setPartSize(int partSize) {
        this.partSize = partSize;
    }

    public int getMaxIdleConnections() {
        return maxIdleConnections;
    }

    public void setMaxIdleConnections(int maxIdleConnections) {
        this.maxIdleConnections = maxIdleConnections;
    }

    public long getKeepAliveDurationMinutes() {
        return keepAliveDurationMinutes;
    }

    public void setKeepAliveDurationMinutes(long keepAliveDurationMinutes) {
        this.keepAliveDurationMinutes = keepAliveDurationMinutes;
    }

    public long getConnectTimeoutSeconds() {
        return connectTimeoutSeconds;
    }

    public void setConnectTimeoutSeconds(long connectTimeoutSeconds) {
        this.connectTimeoutSeconds = connectTimeoutSeconds;
    }

    public long getReadTimeoutSeconds() {
        return readTimeoutSeconds;
    }

    public void setReadTimeoutSeconds(long readTimeoutSeconds) {
        this.readTimeoutSeconds = readTimeoutSeconds;
    }

    public long getWriteTimeoutSeconds() {
        return writeTimeoutSeconds;
    }

    public void setWriteTimeoutSeconds(long writeTimeoutSeconds) {
        this.writeTimeoutSeconds = writeTimeoutSeconds;
    }

}
