package com.kodelabs.formflow.modules.auth.domain.port.in.result;

import com.kodelabs.formflow.modules.auth.domain.model.User;
import com.kodelabs.formflow.modules.auth.domain.model.UserRole;

/** What the logged-in user sees/edits about their own account. */
public record MeResult(String firstName, String lastName, String email, UserRole role, String avatarUrl) {
    public static MeResult from(User user) {
        return new MeResult(user.getFirstName(), user.getLastName(), user.getEmail(), user.getRole(), user.getAvatarUrl());
    }
}
