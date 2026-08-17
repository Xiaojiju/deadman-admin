package com.mtfm.deadman.plugin.pay.service;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.Test;

import com.mtfm.deadman.common.exception.BusinessException;
import com.mtfm.deadman.plugin.pay.spi.profitsharing.ProfitSharingCreateRequest;
import com.mtfm.deadman.plugin.pay.spi.profitsharing.ProfitSharingCreateResult;
import com.mtfm.deadman.plugin.pay.spi.profitsharing.ProfitSharingFinishRequest;
import com.mtfm.deadman.plugin.pay.spi.profitsharing.ProfitSharingProvider;
import com.mtfm.deadman.plugin.pay.spi.profitsharing.ProfitSharingQueryResult;
import com.mtfm.deadman.plugin.pay.spi.profitsharing.ProfitSharingReceiver;
import com.mtfm.deadman.plugin.pay.spi.profitsharing.ProfitSharingReturnRequest;
import com.mtfm.deadman.plugin.pay.spi.profitsharing.ProfitSharingReturnResult;

/**
 * 分账合规校验：禁止 PERSONAL_OPENID、禁止全额分账。
 */
class ProfitSharingServiceComplianceTest {

    @Test
    void rejectPersonalOpenidReceiver() {
        ProfitSharingService service = new ProfitSharingService(List.of(new NoopProvider()));
        BusinessException ex = assertThrows(
                BusinessException.class,
                () -> service.create(
                        "noop",
                        new ProfitSharingCreateRequest(
                                "1900000001",
                                "tx1",
                                "PSO1",
                                List.of(new ProfitSharingReceiver(
                                        "PERSONAL_OPENID",
                                        "oXXX",
                                        100,
                                        "员工",
                                        ProfitSharingReceiver.PURPOSE_STAFF_TRANSIT)),
                                false),
                        "1900000000",
                        10000));
        assertTrue(ex.getMessage().contains("MERCHANT_ID") || ex.getMessage().contains("PERSONAL_OPENID"));
    }

    @Test
    void rejectFullAmountProfitSharing() {
        ProfitSharingService service = new ProfitSharingService(List.of(new NoopProvider()));
        BusinessException ex = assertThrows(
                BusinessException.class,
                () -> service.create(
                        "noop",
                        new ProfitSharingCreateRequest(
                                "1900000001",
                                "tx1",
                                "PSO1",
                                List.of(new ProfitSharingReceiver(
                                        ProfitSharingReceiver.TYPE_MERCHANT_ID,
                                        "1900000000",
                                        10000,
                                        "全额",
                                        ProfitSharingReceiver.PURPOSE_PLATFORM_FEE)),
                                false),
                        "1900000000",
                        10000));
        assertTrue(ex.getMessage().contains("货款"));
    }

    private static final class NoopProvider implements ProfitSharingProvider {
        @Override
        public String providerId() {
            return "noop";
        }

        @Override
        public ProfitSharingCreateResult create(ProfitSharingCreateRequest request) {
            return new ProfitSharingCreateResult(
                    request.subMchid(), request.transactionId(), request.outOrderNo(), "oid", "OK");
        }

        @Override
        public ProfitSharingQueryResult query(String subMchid, String transactionId, String outOrderNo) {
            return null;
        }

        @Override
        public ProfitSharingQueryResult finish(ProfitSharingFinishRequest request) {
            return null;
        }

        @Override
        public ProfitSharingReturnResult returnOrder(ProfitSharingReturnRequest request) {
            return null;
        }

        @Override
        public void addReceiver(String appId, String type, String account, String relationType) {}
    }
}
