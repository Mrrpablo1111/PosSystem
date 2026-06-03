// package com.sh.sh.pos.system.model;

// import java.time.Instant;
// import java.time.LocalDateTime;

// import jakarta.persistence.Column;
// import jakarta.persistence.Entity;
// import jakarta.persistence.GeneratedValue;
// import jakarta.persistence.GenerationType;
// import jakarta.persistence.Id;
// import jakarta.persistence.Index;
// import jakarta.persistence.JoinColumn;
// import jakarta.persistence.ManyToOne;
// import jakarta.persistence.PrePersist;
// import jakarta.persistence.Table;
// import lombok.AllArgsConstructor;
// import lombok.Builder;
// import lombok.Getter;
// import lombok.NoArgsConstructor;
// import lombok.Setter;

// @Entity
// @Table(name = "password_reset_tokens",indexes = @Index(name = "idx_token", columnList = "token"))
// @Getter
// @Setter
// @NoArgsConstructor
// @AllArgsConstructor
// @Builder
// public class PasswordResetToken {
//     @Id
//     @GeneratedValue(strategy = GenerationType.IDENTITY)
//     private Long id;

//     @Column(nullable = false, unique = true)
//     private String token;

//     @Builder.Default
//     @Column(nullable = false)
//     private boolean used = false;

//     @ManyToOne
//     @JoinColumn(name = "user_id", nullable = false)
//     private User user;

//     @Column(nullable = false)
//     private Instant expiryDate;

   
//     @Column(nullable = false, updatable  =false)
//     private Instant createdAt;

//     public boolean isExpired() {
//         return expiryDate != null && expiryDate.isBefore(Instant.now());
//     }

//     public boolean isValid() {
//         return !used && !isExpired();
//     }

//     @PrePersist
//     public void prePersist(){
//         this.createdAt = Instant.now();
//     }
// }
