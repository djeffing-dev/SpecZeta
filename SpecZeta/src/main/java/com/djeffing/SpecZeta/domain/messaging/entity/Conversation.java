package com.djeffing.SpecZeta.domain.messaging.entity;

import com.djeffing.SpecZeta.domain.annonce.entity.Annonce;
import com.djeffing.SpecZeta.domain.user.entity.User;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(
        name = "conversations",
        indexes = {
                @Index(name = "idx_conv_acheteur", columnList = "acheteur_id"),
                @Index(name = "idx_conv_vendeur", columnList = "vendeur_id"),
                @Index(name = "idx_conv_last_message", columnList = "last_message_at")
        },
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_conv_annonce_acheteur",
                        columnNames = {"annonce_id", "acheteur_id"}
                )
        }
)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = "id")
@ToString(exclude = {"annonce", "acheteur", "vendeur", "messages"})
public class Conversation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "annonce_id", nullable = false)
    private Annonce annonce;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "acheteur_id", nullable = false)
    private User acheteur;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "vendeur_id", nullable = false)
    private User vendeur;

    @Column(name = "last_message_at")
    private LocalDateTime lastMessageAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @OneToMany(mappedBy = "conversation", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @OrderBy("createdAt ASC")
    @Builder.Default
    private List<Message> messages = new ArrayList<>();

    /**
     * Callback JPA exécuté avant l'INSERT initial.
     * Horodate la création et initialise {@code lastMessageAt} (utilisé pour le tri
     * des conversations dans la liste « mes discussions »).
     */
    @PrePersist
    void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        this.createdAt = now;
        if (this.lastMessageAt == null) {
            this.lastMessageAt = now;
        }
    }

    /**
     * Ajoute un message à la conversation en synchronisant la relation bidirectionnelle
     * et en rafraîchissant {@code lastMessageAt} pour faire remonter la conversation
     * en tête de liste côté client.
     *
     * @param message le message envoyé par l'acheteur ou le vendeur
     */
    public void addMessage(Message message) {
        message.setConversation(this);
        this.messages.add(message);
        this.lastMessageAt = message.getCreatedAt() != null ? message.getCreatedAt() : LocalDateTime.now();
    }
}
