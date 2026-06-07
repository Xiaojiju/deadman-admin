package com.mtfm.deadman.plugin.storage.local.provider;

import static org.assertj.core.api.Assertions.assertThat;

import com.mtfm.deadman.plugin.file.spi.FileStorageUploadContext;
import com.mtfm.deadman.plugin.file.spi.StoredFileRef;
import com.mtfm.deadman.plugin.storage.local.config.LocalStoragePluginProperties;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.util.StreamUtils;

/**
 * 本地存储 Provider 测试。
 */
class LocalFileStorageProviderTest {

    @TempDir
    java.nio.file.Path tempDir;

    private LocalFileStorageProvider provider;

    @BeforeEach
    void setUp() {
        LocalStoragePluginProperties properties = new LocalStoragePluginProperties();
        properties.setBasePath(tempDir.toString());
        properties.setPublicUrlPrefix("/files");
        provider = new LocalFileStorageProvider(properties);
    }

    @Test
    void shouldStoreOpenDeleteAndExposePublicUrl() throws Exception {
        byte[] content = "hello-file".getBytes(StandardCharsets.UTF_8);
        StoredFileRef ref = provider.store(FileStorageUploadContext.builder()
                .originalFilename("demo.txt")
                .contentType("text/plain")
                .size(content.length)
                .inputStream(new ByteArrayInputStream(content))
                .bizType("test")
                .uploaderUserId(1L)
                .build());

        assertThat(ref.providerId()).isEqualTo("local");
        assertThat(ref.accessUrl()).startsWith("/files/");
        assertThat(Files.exists(tempDir.resolve(ref.storageKey()))).isTrue();

        try (InputStream inputStream = provider.open(ref)) {
            assertThat(StreamUtils.copyToString(inputStream, StandardCharsets.UTF_8)).isEqualTo("hello-file");
        }

        provider.delete(ref);
        assertThat(Files.exists(tempDir.resolve(ref.storageKey()))).isFalse();
    }
}
