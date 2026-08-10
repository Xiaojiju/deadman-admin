package com.mtfm.deadman.plugin.pay.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.List;

import org.junit.jupiter.api.Test;

import com.mtfm.deadman.common.exception.BusinessException;

/**
 * 转账拆单单元测试。
 */
class TransferServiceSplitTest {

    @Test
    void splitExactMultiple() {
        List<Long> parts = TransferService.splitAmount(60_000L, 20_000L);
        assertEquals(List.of(20_000L, 20_000L, 20_000L), parts);
    }

    @Test
    void splitWithRemainder() {
        List<Long> parts = TransferService.splitAmount(45_000L, 20_000L);
        assertEquals(List.of(20_000L, 20_000L, 5_000L), parts);
    }

    @Test
    void splitWithinSingleLimit() {
        List<Long> parts = TransferService.splitAmount(100L, 20_000L);
        assertEquals(List.of(100L), parts);
    }

    @Test
    void splitInvalidAmount() {
        assertThrows(BusinessException.class, () -> TransferService.splitAmount(0L, 20_000L));
    }
}
