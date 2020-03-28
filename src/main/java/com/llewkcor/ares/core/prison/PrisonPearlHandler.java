package com.llewkcor.ares.core.prison;

import lombok.Getter;

public final class PrisonPearlHandler {
    @Getter public final PrisonPearlManager manager;

    public PrisonPearlHandler(PrisonPearlManager manager) {
        this.manager = manager;
    }
}
