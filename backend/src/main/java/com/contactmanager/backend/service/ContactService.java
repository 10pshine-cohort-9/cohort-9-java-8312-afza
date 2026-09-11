package com.contactmanager.backend.service;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.contactmanager.backend.entity.Contact;
import com.contactmanager.backend.entity.EmailAddress;
import com.contactmanager.backend.entity.PhoneNumber;
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

    Map<Long, EmailAddress> existingEmails = existing.getEmailAddresses().stream()
            .filter(email -> email.getId() != null)
            .collect(Collectors.toMap(EmailAddress::getId, Function.identity()));
    Map<Long, PhoneNumber> existingPhones = existing.getPhoneNumbers().stream()
            .filter(phone -> phone.getId() != null)
            .collect(Collectors.toMap(PhoneNumber::getId, Function.identity()));

    Set<Long> submittedEmailIds = new HashSet<>();
    Set<Long> submittedPhoneIds = new HashSet<>();

    updatedContact.getEmailAddresses().stream()
            .filter(email -> email.getId() != null)
            .forEach(email -> requireUniqueOwnedChild(
                    existingEmails, submittedEmailIds, email.getId(), "email address"));
    updatedContact.getPhoneNumbers().stream()
            .filter(phone -> phone.getId() != null)
            .forEach(phone -> requireUniqueOwnedChild(
                    existingPhones, submittedPhoneIds, phone.getId(), "phone number"));

    existing.setFirstName(updatedContact.getFirstName());
    existing.setLastName(updatedContact.getLastName());
    existing.setTitle(updatedContact.getTitle());

    existing.getEmailAddresses().clear();
    existing.getPhoneNumbers().clear();

    updatedContact.getEmailAddresses().forEach(email -> {
        EmailAddress attached = email.getId() == null ? email : existingEmails.get(email.getId());
        attached.setEmail(email.getEmail());
        attached.setLabel(email.getLabel());
        attached.setContact(existing);
        existing.getEmailAddresses().add(attached);
    });

    updatedContact.getPhoneNumbers().forEach(phone -> {
        PhoneNumber attached = phone.getId() == null ? phone : existingPhones.get(phone.getId());
        attached.setPhoneNumber(phone.getPhoneNumber());
        attached.setLabel(phone.getLabel());
        attached.setContact(existing);
        existing.getPhoneNumbers().add(attached);
    });

    return contactRepository.save(existing);
}

private <T> void requireUniqueOwnedChild(
        Map<Long, T> existingChildren, Set<Long> submittedIds, Long childId, String childType) {
    if (!submittedIds.add(childId)) {
        throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                "Submitted " + childType + " ID is repeated");
    }
    if (!existingChildren.containsKey(childId)) {
        throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                "Submitted " + childType + " does not belong to this contact");
    }
}
}
