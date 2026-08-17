package com.mtfm.deadman.plugin.ess.tencent.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.Test;
import org.springframework.context.ApplicationEventPublisher;

import com.mtfm.deadman.plugin.ess.tencent.event.EssFlowCallbackEvent;

/**
 * 电子签回调解析单测。
 */
class EssCallbackDispatchServiceTest {

    private final EssCallbackDispatchService service =
            new EssCallbackDispatchService(null, new NoopPublisher());

    /**
     * 应解析根节点 FlowId / FlowStatus / UserData。
     */
    @Test
    void shouldParseRootFields() {
        String plain = """
                {"MsgType":"FlowStatusChange","FlowId":"flow-1","FlowStatus":"ALL","UserData":"SIGN_ORDER|SGN1"}
                """;
        EssFlowCallbackEvent event = service.parseEvent(plain);
        assertEquals("FlowStatusChange", event.msgType());
        assertEquals("flow-1", event.flowId());
        assertEquals("ALL", event.flowStatus());
        assertEquals("SIGN_ORDER", event.bizType());
        assertEquals("SGN1", event.bizRef());
    }

    /**
     * 应解析 MsgData 嵌套 JSON 字符串。
     */
    @Test
    void shouldParseNestedMsgData() {
        String plain = """
                {"MsgType":"FlowStatusChange","MsgData":"{\\"FlowId\\":\\"flow-2\\",\\"FlowStatus\\":\\"PART\\",\\"UserData\\":\\"SGN9\\"}"}
                """;
        EssFlowCallbackEvent event = service.parseEvent(plain);
        assertEquals("flow-2", event.flowId());
        assertEquals("PART", event.flowStatus());
        assertNull(event.bizType());
        assertEquals("SGN9", event.bizRef());
    }

    private static final class NoopPublisher implements ApplicationEventPublisher {
        @Override
        public void publishEvent(Object event) {
            // no-op
        }
    }
}
