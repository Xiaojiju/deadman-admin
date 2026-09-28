package com.mtfm.deadman.plugin.identity.support;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDate;

import org.junit.jupiter.api.Test;

import com.mtfm.deadman.common.exception.BusinessException;
import com.mtfm.deadman.plugin.identity.spi.FaceRecognitionClient;
import com.mtfm.deadman.plugin.identity.spi.FaceRecognitionResult;

/**
 * 身份证校验与活体优先的比对门槛。
 */
class IdentityRulesTest {

    @Test
    void shouldParseMainlandIdCard() {
        IdCardSupport.ParsedIdCard parsed = IdCardSupport.parse("11010519491231002X", null, null);
        assertThat(parsed.birthDate()).isEqualTo(LocalDate.of(1949, 12, 31));
        assertThat(parsed.gender()).isEqualTo(2);
        assertThat(IdCardSupport.mask(parsed.idCardNo())).isEqualTo("110105********002X");
    }

    @Test
    void shouldRejectBirthMismatch() {
        assertThatThrownBy(() -> IdCardSupport.parse("11010519491231002X", LocalDate.of(2000, 1, 1), 2))
            .isInstanceOf(BusinessException.class);
    }

    @Test
    void shouldSkipCompareWhenLivenessFails() {
        FaceRecognitionClient client = new FaceRecognitionClient() {
            @Override
            public FaceRecognitionResult detectLive(byte[] faceImage) {
                return new FaceRecognitionResult(10F, "live-1");
            }

            @Override
            public FaceRecognitionResult compare(byte[] idCardImage, byte[] faceImage) {
                throw new IllegalStateException("活体未通过时不应比对");
            }
        };
        FaceCheckOutcome outcome = IdentityFaceGate.check(client, new byte[] {1}, new byte[] {2}, 40F, 70F);
        assertThat(outcome.passed()).isFalse();
        assertThat(outcome.matchScore()).isNull();
    }

    @Test
    void shouldPassOnlyWhenBothScoresReachThreshold() {
        FaceRecognitionClient client = new FaceRecognitionClient() {
            @Override
            public FaceRecognitionResult detectLive(byte[] faceImage) {
                return new FaceRecognitionResult(90F, "live-2");
            }

            @Override
            public FaceRecognitionResult compare(byte[] idCardImage, byte[] faceImage) {
                return new FaceRecognitionResult(70F, "cmp-2");
            }
        };
        FaceCheckOutcome passed = IdentityFaceGate.check(client, new byte[] {1}, new byte[] {2}, 40F, 70F);
        assertThat(passed.passed()).isTrue();
        assertThat(passed.matchScore()).isEqualTo(70F);
    }
}
