package jins.db.whatsappgroup.models;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Getter
@Setter
public class Message {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String contenu;


    @Column(nullable = false)
    private LocalDateTime dateEnvoi = LocalDateTime.now();

    @ManyToOne
    private Membre membre;
}
