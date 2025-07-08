package jins.db.whatsappgroup.repository;

import jins.db.whatsappgroup.models.Membre;
import jins.db.whatsappgroup.models.Message;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import javax.persistence.TypedQuery;
import java.util.List;

public class MessageRepository {
    EntityManager entityManager;
    public MessageRepository(EntityManager entityManager) {
        this.entityManager = entityManager;
    }
    public void saveMessage(Message message) {
        try {
            entityManager.getTransaction().begin();
            entityManager.persist(message);
            entityManager.getTransaction().commit();
        }catch (Exception e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
        }

    }
    public List<Message> findAllMessages() {
        return entityManager.createQuery("FROM Message ORDER BY dateEnvoi ASC ", Message.class).getResultList();
    }
    public List<Message> findAllMessagesForMember(String pseudo) {
        TypedQuery<Message> query = entityManager.createQuery("SELECT ms FROM Message ms WHERE ms.membre.pseudo = :pseudo", Message.class);
        query.setParameter("pseudo", pseudo);
        return query.getResultList();
    }
}
