package com.playares.core.network.data;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
public enum NetworkPermission {
    ADMIN("Administrator", "Members with this permission have every permission", false),
    ACCESS_CHAT("Access Chat", "Grants the ability to receive/send network messages", true),
    KICK_MEMBERS("Kick Members", "Grants the ability to kick players who aren't Admins", false),
    INVITE_MEMBERS("Invite Members", "Grants the ability to invite players", false),
    MODIFY_CLAIMS("Claim/Unclaim Land", "Grants the ability to claim and unclaim land", false),
    ACCESS_LAND("Access Claims", "Grants the ability to access claims", true),
    VIEW_SNITCHES("View Snitch Notifications", "Grants access to all snitch notifications", true),
    MODIFY_SNITCHES("Create Snitches", "Grants the ability to create snitches", false),
    ACCESS_FACTORY("Access Factories", "Grants access to all factories", false),
    MODIFY_FACTORY("Create Factories", "Grants the ability to create factories", false),
    MODIFY_BASTION("Create Bastions", "Grants the ability to create bastions", false),
    MODIFY_ACID("Create Acid", "Grants the ability to create acid blocks", false);

    @Getter public String displayName;
    @Getter public String description;
    @Getter public boolean defaultValue;
}