package jins.db.whatsappgroup.models;

import lombok.Getter;
import lombok.Setter;


import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
public class Membre {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String pseudo;

    @Column(nullable = false)
    private String password;

    private boolean isBanned = false;

    @OneToMany(mappedBy = "membre", cascade = CascadeType.ALL)
    private List<Message> messages = new ArrayList<>();

}
