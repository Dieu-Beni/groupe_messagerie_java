package jins.db.whatsappgroup.services;

import jins.db.whatsappgroup.models.Membre;
import jins.db.whatsappgroup.models.Message;

import java.util.List;

public interface MessageService {
    public List<Message> findAllMessages();
    public List<Message> findMessagesByPseudoMembre(String pseudo);
    public Message sendMessage(String contenu, Membre membre);

}
