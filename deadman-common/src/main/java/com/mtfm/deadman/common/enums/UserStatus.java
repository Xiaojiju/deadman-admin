package com.mtfm.deadman.common.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum UserStatus {

    DISABLED(0),
    ACTIVE(1);

    private final int value;
}
