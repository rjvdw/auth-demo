package dev.rdcl.auth.auth.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.NamedQueries;
import jakarta.persistence.NamedQuery;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity(name = "AuthenticatorEntity")
@Table(name = "authenticator")
@NamedQueries({
    @NamedQuery(name = "Authenticator.findByUser", query = """
            select a
            from AuthenticatorEntity a
            where user = :user
        """),
})
public class AuthenticatorEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id", nullable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false, updatable = false)
    private UserEntity user;

    @Column(name = "key_id", nullable = false)
    private byte[] keyId;

    @Column(name = "cose", nullable = false)
    private byte[] cose;

    @Column(name = "signature_count", nullable = false)
    private Long signatureCount;
}
