package com.mtfm.deadman.core.config.jackson;

import tools.jackson.databind.module.SimpleModule;
import tools.jackson.databind.ser.std.ToStringSerializer;

/**
 * Jackson 模块工厂。
 */
public final class JacksonModules {

    private JacksonModules() {
    }

    /**
     * 将 {@link Long} / long 序列化为字符串，避免前端 JS 精度丢失（超过 2^53-1）。
     */
    public static SimpleModule jsSafeLongModule() {
        SimpleModule module = new SimpleModule("JsSafeLongModule");
        module.addSerializer(Long.class, ToStringSerializer.instance);
        module.addSerializer(Long.TYPE, ToStringSerializer.instance);
        return module;
    }
}
