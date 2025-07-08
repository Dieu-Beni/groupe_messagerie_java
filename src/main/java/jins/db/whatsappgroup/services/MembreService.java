package jins.db.whatsappgroup.services;

import jins.db.whatsappgroup.models.Membre;

public interface MembreService {
    Membre createMembre(String pseudo, String password);
    void updateMembre(Membre membre);
    boolean deleteMembre(Membre membre);
    Membre findMembreByPseudo(String pseudo);
}
