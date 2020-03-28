package com.llewkcor.ares.core.claim.session;

import com.llewkcor.ares.core.claim.data.ClaimType;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.UUID;

@AllArgsConstructor
public final class ClaimSession {
    @Getter UUID uniqueId;
    @Getter UUID networkId;
    @Getter ClaimSessionType sessionType;
    @Getter ClaimType claimType;
}