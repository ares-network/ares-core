package com.llewkcor.ares.core.claim.session;

public enum ClaimSessionType {
    /**
     * Blocks are being reinforced as they are placed
     */
    FORTIFY,
    /**
     * Blocks are being reinforced by being punched with the material in hand
     */
    REINFORCE,
    /**
     * Information is being printed to all blocks right-clicked
     */
    INFO
}