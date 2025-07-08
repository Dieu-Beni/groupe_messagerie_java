package jins.db.whatsappgroup.services.impl;

import jins.db.whatsappgroup.models.Membre;
import jins.db.whatsappgroup.models.Message;
import jins.db.whatsappgroup.repository.MembreRepository;
import jins.db.whatsappgroup.repository.MessageRepository;
import jins.db.whatsappgroup.services.MessageService;

import javax.persistence.EntityManager;
import java.util.List;

public class MessageImpl implements MessageService {
    private final EntityManager entityManager;
    private final MessageRepository messageRepository;

    public MessageImpl(EntityManager entityManager) {
        this.entityManager = entityManager;
        this.messageRepository = new MessageRepository(entityManager);
    }
    @Override
    public List<Message> findAllMessages() {
        return messageRepository.findAllMessages();
    }

    @Override
    public List<Message> findMessagesByPseudoMembre(String pseudo) {
        return messageRepository.findAllMessagesForMember(pseudo);
    }

    @Override
    public Message sendMessage(String contenu, Membre membre) {
        Message message = new Message();
        message.setContenu(contenu);
        message.setMembre(membre);
        messageRepository.saveMessage(message);
        return message;
    }
}
