package com.contactmanager.backend.service;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.contactmanager.backend.entity.Contact;
import com.contactmanager.backend.entity.EmailAddress;
import com.contactmanager.backend.entity.PhoneNumber;
import com.contactmanager.backend.entity.User;
import com.contactmanager.backend.entity.User.IdentifierType;
import com.contactmanager.backend.repository.ContactRepository;
import com.contactmanager.backend.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
class ContactServiceTests {

    @Mock
    private ContactRepository contactRepository;
    @Mock
    private UserRepository userRepository;

    private ContactService service;

    @BeforeEach
    void setUp() {
        service = new ContactService(contactRepository, userRepository);
    }

    @Test
    void saveAssignsAuthenticatedOwnerAndChildRelationships() {
        User owner = new User("Test", "User", "test@example.com", IdentifierType.EMAIL, "hash");
        Contact contact = new Contact();
        EmailAddress email = email("work@example.com", "Work");
        EmailAddress personalEmail = email("me@example.com", "Personal");
        PhoneNumber phone = phone("+923001234567", "Personal");
        PhoneNumber homePhone = phone("02112345678", "Home");
        contact.getEmailAddresses().add(email);
        contact.getEmailAddresses().add(personalEmail);
        contact.getPhoneNumbers().add(phone);
        contact.getPhoneNumbers().add(homePhone);
        when(userRepository.getReferenceById(7L)).thenReturn(owner);
        when(contactRepository.save(contact)).thenReturn(contact);

        Contact saved = service.saveContact(7L, contact);

        assertSame(owner, saved.getOwner());
        assertSame(contact, email.getContact());
        assertSame(contact, personalEmail.getContact());
        assertSame(contact, phone.getContact());
        assertSame(contact, homePhone.getContact());
        assertEquals("Work", email.getLabel());
        assertEquals("Home", homePhone.getLabel());
    }

    @Test
    void updateReturnsMissingWhenContactIsNotOwnedByUser() {
        when(contactRepository.findByIdAndOwnerId(22L, 7L)).thenReturn(Optional.empty());

        assertNull(service.updateContact(7L, 22L, new Contact()));
        verify(contactRepository, never()).save(org.mockito.ArgumentMatchers.any());
    }

    @Test
    void getReturnsOwnedContact() {
        Contact contact = new Contact();
        when(contactRepository.findByIdAndOwnerId(22L, 7L)).thenReturn(Optional.of(contact));

        assertSame(contact, service.getContactById(7L, 22L));
    }

    @Test
    void getDoesNotRevealAnotherUsersContact() {
        when(contactRepository.findByIdAndOwnerId(22L, 7L)).thenReturn(Optional.empty());

        assertNull(service.getContactById(7L, 22L));
    }

    @Test
    void listAndSearchAreScopedToAuthenticatedOwner() {
        PageRequest pageable = PageRequest.of(0, 9);
        when(contactRepository.findContacts(7L, "alex", "Work", pageable)).thenReturn(Page.empty(pageable));

        service.getContacts(7L, " alex ", " Work ", pageable);

        verify(contactRepository).findContacts(7L, "alex", "Work", pageable);
    }

    @Test
    void updateReplacesMultipleLabeledEmailsAndPhonesWithoutChangingOwner() {
        User owner = new User("Test", "User", "test@example.com", IdentifierType.EMAIL, "hash");
        Contact existing = new Contact();
        existing.setOwner(owner);
        existing.getEmailAddresses().add(email("old@example.com", "Other"));
        existing.getPhoneNumbers().add(phone("11111111", "Other"));

        Contact update = new Contact();
        update.getEmailAddresses().add(email("work@example.com", "Work"));
        update.getEmailAddresses().add(email("me@example.com", "Personal"));
        update.getPhoneNumbers().add(phone("+923001234567", "Personal"));
        update.getPhoneNumbers().add(phone("02112345678", "Home"));

        when(contactRepository.findByIdAndOwnerId(22L, 7L)).thenReturn(Optional.of(existing));
        when(contactRepository.save(any(Contact.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Contact saved = service.updateContact(7L, 22L, update);

        assertSame(owner, saved.getOwner());
        assertEquals(2, saved.getEmailAddresses().size());
        assertEquals("Work", saved.getEmailAddresses().get(0).getLabel());
        assertEquals(2, saved.getPhoneNumbers().size());
        assertEquals("Home", saved.getPhoneNumbers().get(1).getLabel());
        assertTrue(saved.getEmailAddresses().stream().allMatch(item -> item.getContact() == saved));
        assertTrue(saved.getPhoneNumbers().stream().allMatch(item -> item.getContact() == saved));
    }

    @Test
    void deleteDoesNotRevealOrDeleteAnotherUsersContact() {
        when(contactRepository.findByIdAndOwnerId(22L, 7L)).thenReturn(Optional.empty());

        assertFalse(service.deleteContact(7L, 22L));
        verify(contactRepository, never()).delete(org.mockito.ArgumentMatchers.any());
    }

    private EmailAddress email(String value, String label) {
        EmailAddress email = new EmailAddress();
        email.setEmail(value);
        email.setLabel(label);
        return email;
    }

    private PhoneNumber phone(String value, String label) {
        PhoneNumber phone = new PhoneNumber();
        phone.setPhoneNumber(value);
        phone.setLabel(label);
        return phone;
    }
}
