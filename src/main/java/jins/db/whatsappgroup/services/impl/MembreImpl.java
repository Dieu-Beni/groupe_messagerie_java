package jins.db.whatsappgroup.services.impl;

import jins.db.whatsappgroup.models.Membre;
import jins.db.whatsappgroup.repository.MembreRepository;
import jins.db.whatsappgroup.services.MembreService;

import javax.persistence.EntityManager;

public class MembreImpl implements MembreService {
    private final MembreRepository membreRepository;

    public MembreImpl(EntityManager entityManager) {
        this.membreRepository = new MembreRepository(entityManager);
    }

    @Override
    public Membre createMembre(String pseudo, String password) {
        Membre membre = new Membre();
        try {
            membre.setPseudo(pseudo);
            membre.setPassword(password);
            Membre m = membreRepository.findMembreByPseudo(pseudo);
            if (m == null) {
                membreRepository.SaveMembre(membre);
            }else {
                return null;
            }
        }catch(Exception e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
            return null;
        }

        return membre;
    }

    @Override
    public void updateMembre(Membre membre) {
        try {
            membreRepository.UpdateMembre(membre);
        }catch (Exception e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
        }
    }

    @Override
    public boolean deleteMembre(Membre membre) {
        return membreRepository.deleteMembre(membre);
    }

    @Override
    public Membre findMembreByPseudo(String pseudo) {
        return membreRepository.findMembreByPseudo(pseudo);
    }
}
