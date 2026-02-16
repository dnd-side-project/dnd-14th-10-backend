package io.dnd.goyo.common.storage;

import java.util.List;

public interface FileStorage {

    String generatePresignedUrl(String objectKey);

    String generatePublicUrl(String objectKey);

    void deleteObjects(List<String> objectKeys);
}
