package com.llewkcor.ares.core.network.data;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
public enum NetworkPermission {
    ADMIN("Administrator", "Members with this permission have every permission", false),
    KICK_MEMBERS("Kick Members", "Grants the ability to kick players who aren't Admins", false),
    INVITE_MEMBERS("Invite Members", "Grants the ability to invite players", false),
    MODIFY_CLAIMS("Claim/Unclaim Land", "Grants the ability to claim and unclaim land", false),
    ACCESS_LAND("Access Claims", "Grants the ability to access claims such as Doors and Fencegates", true),
    VIEW_SNITCHES("View Snitch Notifications", "Grants access to all snitch notifications", true),
    MODIFY_SNITCHES("Create/Delete Snitches", "Grants the ability to create and delete snitches", false);

    @Getter public String displayName;
    @Getter public String description;
    @Getter public boolean defaultValue;
}