package com.contactmanager.backend.service;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.contactmanager.backend.entity.Contact;
import com.contactmanager.backend.repository.ContactRepository;
import com.contactmanager.backend.repository.UserRepository;

@Service
public class ContactService {

    private final ContactRepository contactRepository;
    private final UserRepository userRepository;

    public ContactService(ContactRepository contactRepository, UserRepository userRepository) {
        this.contactRepository = contactRepository;
        this.userRepository = userRepository;
    }

   public Contact saveContact(Long userId, Contact contact) {

    contact.setOwner(userRepository.getReferenceById(userId));

    contact.getEmailAddresses().forEach(email -> email.setContact(contact));

    contact.getPhoneNumbers().forEach(phone -> phone.setContact(contact));

    return contactRepository.save(contact);
}

    public Page<Contact> getContacts(Long userId, String search, String title, Pageable pageable) {
        return contactRepository.findContacts(userId, search.trim(), title.trim(), pageable);
    }
    public Page<Contact> getContactsSortedByEmail(Long userId, String search, String title, Pageable pageable) {
        return contactRepository.findContactsSortedByEmail(userId, search.trim(), title.trim(), pageable);
    }
    public List<String> getContactTitles(Long userId) {
        return contactRepository.findDistinctTitles(userId);
    }
    public Contact getContactById(Long userId, Long id) {
    return contactRepository.findByIdAndOwnerId(id, userId).orElse(null);
}
public boolean deleteContact(Long userId, Long id) {
    Contact contact = getContactById(userId, id);
    if (contact == null) return false;
    contactRepository.delete(contact);
    return true;
}
@Transactional
public Contact updateContact(Long userId, Long id, Contact updatedContact) {

    Contact existing = contactRepository.findByIdAndOwnerId(id, userId).orElse(null);

    if (existing == null)
        return null;

    existing.setFirstName(updatedContact.getFirstName());
    existing.setLastName(updatedContact.getLastName());
    existing.setTitle(updatedContact.getTitle());

    existing.getEmailAddresses().clear();
    existing.getPhoneNumbers().clear();

    updatedContact.getEmailAddresses().forEach(email -> {
        email.setContact(existing);
        existing.getEmailAddresses().add(email);
    });

    updatedContact.getPhoneNumbers().forEach(phone -> {
        phone.setContact(existing);
        existing.getPhoneNumbers().add(phone);
    });

    return contactRepository.save(existing);
}
}
