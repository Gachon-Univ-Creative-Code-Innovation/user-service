    package com.gucci.user_service.user.domain;

    import jakarta.persistence.*;
    import lombok.*;

    import java.sql.Timestamp;

    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    @Getter
    @Setter
    @Entity
    @Table(name = "users")
    public class User {

        @Id
        @GeneratedValue(strategy = GenerationType.AUTO)
        @Column(nullable = false)
        private Long userId;

        @Column(unique = true)
        private String email;

        private String password;

        @Column(nullable = false)
        private String name;

        @Column(unique = true)
        private String nickname;

        @Column
        private String githubUrl;

        @Column
        private String profileUrl;

        @Enumerated(EnumType.STRING)
        @Column(nullable = false)
        private Role role;

        @Enumerated(EnumType.STRING)
        private SocialType socialType;

        private String socialId;

        @Column(nullable = false)
        private Timestamp createdAt;

        @PrePersist
        protected void onCreate() {
            this.createdAt = new Timestamp(System.currentTimeMillis());
        }
    }