package io.dnd.goyo.common.storage;

public interface FileStorage {

    String generatePresignedUrl(String objectName);
}
