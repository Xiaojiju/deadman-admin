package com.mtfm.deadman.plugin.storage.oss.provider;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.aliyun.oss.OSS;
import com.aliyun.oss.model.OSSObject;
import com.mtfm.deadman.plugin.file.spi.FileStorageUploadContext;
import com.mtfm.deadman.plugin.file.spi.StoredFileRef;
import com.mtfm.deadman.plugin.storage.oss.client.OssClientFactory;
import com.mtfm.deadman.plugin.storage.oss.config.OssPublicUrlMode;
import com.mtfm.deadman.plugin.storage.oss.config.OssStoragePluginProperties;
import com.mtfm.deadman.plugin.storage.oss.routing.OssBucketResolver;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.util.StreamUtils;

/**
 * 阿里云 OSS 存储 Provider 测试（Mock OSS 客户端）。
 */
@ExtendWith(MockitoExtension.class)
class OssFileStorageProviderTest {

    @Mock
    private OSS oss;

    @Mock
    private OssClientFactory ossClientFactory;

    private OssStoragePluginProperties properties;

    private OssFileStorageProvider provider;

    @BeforeEach
    void setUp() {
        when(ossClientFactory.getClient()).thenReturn(oss);
        properties = new OssStoragePluginProperties();
        properties.setDefaultBucket("engineering-public");
        properties.setBucketRouting(Map.of(
                "rent", "engineering-user-upload",
                "merchant-license", "engineering-private"));
        properties.setCdnDomains(Map.of(
                "engineering-user-upload", "https://cdn-user.example.com"));
        properties.setBucketAccessModes(Map.of(
                "engineering-private", "signed"));
        properties.setPublicUrlMode(OssPublicUrlMode.CDN);
        properties.setSignedUrlExpireSeconds(3600);

        OssBucketResolver bucketResolver = new OssBucketResolver(properties);
        provider = new OssFileStorageProvider(ossClientFactory, bucketResolver, properties);
    }

    @Test
    void shouldStoreToRoutedBucketWithCdnUrl() throws Exception {
        byte[] content = "oss-demo".getBytes(StandardCharsets.UTF_8);
        StoredFileRef ref = provider.store(FileStorageUploadContext.builder()
                .originalFilename("photo.jpg")
                .contentType("image/jpeg")
                .size(content.length)
                .inputStream(new ByteArrayInputStream(content))
                .bizType("rent")
                .uploaderUserId(1L)
                .build());

        assertThat(ref.providerId()).isEqualTo("oss");
        assertThat(ref.storageBucket()).isEqualTo("engineering-user-upload");
        assertThat(ref.storageKey()).startsWith("rent/");
        assertThat(ref.storageKey()).endsWith(".jpg");
        assertThat(ref.accessUrl()).startsWith("https://cdn-user.example.com/rent/");

        ArgumentCaptor<String> bucketCaptor = ArgumentCaptor.forClass(String.class);
        verify(oss).putObject(bucketCaptor.capture(), eq(ref.storageKey()), any(InputStream.class));
        assertThat(bucketCaptor.getValue()).isEqualTo("engineering-user-upload");
    }

    @Test
    void shouldStorePrivateBucketWithSignedUrl() throws Exception {
        when(oss.generatePresignedUrl(eq("engineering-private"), any(String.class), any()))
                .thenReturn(new URL("https://oss.example.com/signed-object"));

        byte[] content = "license".getBytes(StandardCharsets.UTF_8);
        StoredFileRef ref = provider.store(FileStorageUploadContext.builder()
                .originalFilename("license.pdf")
                .contentType("application/pdf")
                .size(content.length)
                .inputStream(new ByteArrayInputStream(content))
                .bizType("merchant-license")
                .uploaderUserId(2L)
                .build());

        assertThat(ref.storageBucket()).isEqualTo("engineering-private");
        assertThat(ref.accessUrl()).isEqualTo("https://oss.example.com/signed-object");
    }

    @Test
    void shouldOpenAndDeleteUsingPersistedBucket() throws Exception {
        StoredFileRef ref = new StoredFileRef(
                "oss", "rent/2026/06/23/demo.txt", "https://cdn-user.example.com/rent/2026/06/23/demo.txt",
                "engineering-user-upload");

        OSSObject ossObject = new OSSObject();
        ossObject.setObjectContent(new ByteArrayInputStream("hello-oss".getBytes(StandardCharsets.UTF_8)));
        when(oss.getObject("engineering-user-upload", ref.storageKey())).thenReturn(ossObject);

        try (InputStream inputStream = provider.open(ref)) {
            assertThat(StreamUtils.copyToString(inputStream, StandardCharsets.UTF_8)).isEqualTo("hello-oss");
        }

        provider.delete(ref);
        verify(oss).deleteObject("engineering-user-upload", ref.storageKey());
    }
}
