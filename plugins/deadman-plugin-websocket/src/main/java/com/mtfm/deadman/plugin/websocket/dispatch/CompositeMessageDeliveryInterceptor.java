package com.mtfm.deadman.plugin.websocket.dispatch;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 组合所有 {@link MessageDeliveryInterceptor} Bean 并依次回调。
 */
@Component
@RequiredArgsConstructor
public class CompositeMessageDeliveryInterceptor implements MessageDeliveryInterceptor {

    private final List<MessageDeliveryInterceptor> interceptors;

    @Override
    public void afterDelivery(MessageDeliveryContext context) {
        for (MessageDeliveryInterceptor interceptor : interceptors) {
            if (interceptor != this) {
                interceptor.afterDelivery(context);
            }
        }
    }
}
