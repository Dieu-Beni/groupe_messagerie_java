package jins.db.whatsappgroup.repository;

import jins.db.whatsappgroup.models.Membre;

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;
import javax.persistence.TypedQuery;
import java.util.List;

public class MembreRepository {
    private final EntityManager entityManager;
    public MembreRepository(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    public void SaveMembre(Membre membre) {
        entityManager.getTransaction().begin();
        entityManager.persist(membre);
        entityManager.getTransaction().commit();
    }

    public void saveMembre(Membre membre) {
        EntityTransaction transaction = null;
        try {
            transaction = entityManager.getTransaction();
            transaction.begin();
            entityManager.persist(membre);
            transaction.commit();
        } catch (Exception e) {
            if (transaction != null && transaction.isActive()) {
                transaction.rollback();
            }
            System.out.println(e.getMessage());
            e.printStackTrace();
        }
    }


    public List<Membre> findAllMembre() {
        TypedQuery<Membre> query = entityManager.createQuery("SELECT m FROM Membre m", Membre.class);
        return query.getResultList();
    }

    public Membre findMembreById(int id) {
        return entityManager.find(Membre.class, id);
    }

    public Membre findMembreByPseudo(String pseudo) {
        try {
            TypedQuery<Membre> query = entityManager.createQuery("SELECT m FROM Membre m WHERE m.pseudo = :pseudo",Membre.class);
            query.setParameter("pseudo", pseudo);
            return query.getSingleResult();
        }catch (Exception e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
            return null;
        }

    }

    public boolean deleteMembre(Membre membre) {
        try {
            entityManager.getTransaction().begin();

            if (!entityManager.contains(membre)) {
                membre = entityManager.merge(membre); // Reattacher l'entité si elle est détachée
            }

            entityManager.remove(membre);
            entityManager.getTransaction().commit();
            return true;
        } catch (Exception e) {
            System.out.println("Erreur lors de la suppression : " + e.getMessage());
            e.printStackTrace();
            entityManager.getTransaction().rollback(); // Très important en cas d'erreur
            return false;
        }
    }


    public void UpdateMembre(Membre membre) {
        try {
            entityManager.getTransaction().begin();
            entityManager.merge(membre);
            entityManager.getTransaction().commit();
        } catch (Exception e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
        }
    }
}
