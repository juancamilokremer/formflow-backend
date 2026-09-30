package com.kodelabs.formflow.modules.auth.domain.port.in.command;

public record AcceptInvitationCommand(String token, String firstName, String lastName, String password) {}
