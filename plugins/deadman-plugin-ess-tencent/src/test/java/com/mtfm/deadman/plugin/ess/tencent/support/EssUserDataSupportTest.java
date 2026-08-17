package com.mtfm.deadman.plugin.ess.tencent.support;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import com.mtfm.deadman.plugin.ess.tencent.constant.EssBizTypes;

/**
 * UserData 约定编解码单测。
 */
class EssUserDataSupportTest {

    @Test
    void shouldEncodeAndParse() {
        String userData = EssUserDataSupport.encode(EssBizTypes.SIGN_ORDER, "SGN100");
        assertEquals("SIGN_ORDER|SGN100", userData);
        assertEquals(EssBizTypes.SIGN_ORDER, EssUserDataSupport.parseBizType(userData));
        assertEquals("SGN100", EssUserDataSupport.parseBizRef(userData));
        assertTrue(EssUserDataSupport.matchesBizType(userData, EssBizTypes.SIGN_ORDER));
        assertFalse(EssUserDataSupport.matchesBizType(userData, "OTHER"));
    }

    @Test
    void shouldFallbackBizRefWhenNoSeparator() {
        assertNull(EssUserDataSupport.parseBizType("SGN100"));
        assertEquals("SGN100", EssUserDataSupport.parseBizRef("SGN100"));
    }
}
