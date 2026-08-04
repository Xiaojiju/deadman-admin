package com.mtfm.deadman.plugin.storage.cos.provider;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.Map;

import org.apache.http.client.methods.HttpRequestBase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.util.StreamUtils;

import com.mtfm.deadman.plugin.file.spi.FileStorageUploadContext;
import com.mtfm.deadman.plugin.file.spi.StoredFileRef;
import com.mtfm.deadman.plugin.storage.cos.client.CosClientFactory;
import com.mtfm.deadman.plugin.storage.cos.config.CosPublicUrlMode;
import com.mtfm.deadman.plugin.storage.cos.config.CosStoragePluginProperties;
import com.mtfm.deadman.plugin.storage.cos.routing.CosBucketResolver;
import com.qcloud.cos.COSClient;
import com.qcloud.cos.model.COSObject;
import com.qcloud.cos.model.COSObjectInputStream;
import com.qcloud.cos.model.ObjectMetadata;

/**
 * 腾讯云 COS 存储 Provider 测试（Mock COS 客户端）。
 */
@ExtendWith(MockitoExtension.class)
class CosFileStorageProviderTest {

    @Mock
    private COSClient cos;

    @Mock
    private CosClientFactory cosClientFactory;

    private CosStoragePluginProperties properties;

    private CosFileStorageProvider provider;

    @BeforeEach
    void setUp() {
        when(cosClientFactory.getClient()).thenReturn(cos);
        properties = new CosStoragePluginProperties();
        properties.setDefaultBucket("engineering-public");
        properties.setBucketRouting(Map.of(
                "rent", "engineering-user-upload",
                "merchant-license", "engineering-private"));
        properties.setCdnDomains(Map.of(
                "engineering-user-upload", "https://cdn-user.example.com"));
        properties.setBucketAccessModes(Map.of(
                "engineering-private", "signed"));
        properties.setPublicUrlMode(CosPublicUrlMode.CDN);
        properties.setSignedUrlExpireSeconds(3600);

        CosBucketResolver bucketResolver = new CosBucketResolver(properties);
        provider = new CosFileStorageProvider(cosClientFactory, bucketResolver, properties);
    }

    @Test
    void shouldStoreToRoutedBucketWithCdnUrl() throws Exception {
        byte[] content = "cos-demo".getBytes(StandardCharsets.UTF_8);
        StoredFileRef ref = provider.store(FileStorageUploadContext.builder()
                .originalFilename("photo.jpg")
                .contentType("image/jpeg")
                .size(content.length)
                .inputStream(new ByteArrayInputStream(content))
                .bizType("rent")
                .uploaderUserId(1L)
                .build());

        assertThat(ref.providerId()).isEqualTo("cos");
        assertThat(ref.storageBucket()).isEqualTo("engineering-user-upload");
        assertThat(ref.storageKey()).startsWith("rent/");
        assertThat(ref.storageKey()).endsWith(".jpg");
        assertThat(ref.accessUrl()).startsWith("https://cdn-user.example.com/rent/");

        ArgumentCaptor<String> bucketCaptor = ArgumentCaptor.forClass(String.class);
        verify(cos).putObject(
                bucketCaptor.capture(),
                eq(ref.storageKey()),
                any(InputStream.class),
                any(ObjectMetadata.class));
        assertThat(bucketCaptor.getValue()).isEqualTo("engineering-user-upload");
    }

    @Test
    void shouldStorePrivateBucketWithSignedUrl() throws Exception {
        when(cos.generatePresignedUrl(eq("engineering-private"), any(String.class), any()))
                .thenReturn(URI.create("https://cos.example.com/signed-object").toURL());

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
        assertThat(ref.accessUrl()).isEqualTo("https://cos.example.com/signed-object");
    }

    @Test
    void shouldOpenAndDeleteUsingPersistedBucket() throws Exception {
        StoredFileRef ref = new StoredFileRef(
                "cos", "rent/2026/06/23/demo.txt", "https://cdn-user.example.com/rent/2026/06/23/demo.txt",
                "engineering-user-upload");

        COSObject cosObject = mock(COSObject.class);
        COSObjectInputStream content = new COSObjectInputStream(
                new ByteArrayInputStream("hello-cos".getBytes(StandardCharsets.UTF_8)),
                mock(HttpRequestBase.class));
        when(cosObject.getObjectContent()).thenReturn(content);
        when(cos.getObject("engineering-user-upload", ref.storageKey())).thenReturn(cosObject);

        try (InputStream inputStream = provider.open(ref)) {
            assertThat(StreamUtils.copyToString(inputStream, StandardCharsets.UTF_8)).isEqualTo("hello-cos");
        }

        provider.delete(ref);
        verify(cos).deleteObject("engineering-user-upload", ref.storageKey());
    }
}
