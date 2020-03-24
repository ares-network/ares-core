package com.llewkcor.ares.core.bridge.data.session;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.UUID;

@AllArgsConstructor
public final class AccountCreateSession implements AccountSession {
    @Getter public final UUID uniqueId;
    @Getter public final UUID bukkitId;
    @Getter public final String username;
    @Getter public final long expireTime;
}
