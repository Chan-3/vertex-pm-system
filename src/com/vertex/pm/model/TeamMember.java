package com.vertex.pm.model;

public class TeamMember {
    private final String memberId;
    private String name;
    private String role;
    private String email;

    public TeamMember(String memberId, String name, String role, String email) {
        this.memberId = memberId;
        this.name = name;
        this.role = role;
        this.email = email;
    }

    public String getMemberId() {
        return memberId;
    }

    public String getName() {
        return name;
    }

    public String getRole() {
        return role;
    }

    public String getEmail() {
        return email;
    }
}
