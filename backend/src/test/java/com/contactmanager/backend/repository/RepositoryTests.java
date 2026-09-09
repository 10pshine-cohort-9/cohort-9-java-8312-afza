package com.contactmanager.backend.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

import com.contactmanager.backend.entity.Contact;
import com.contactmanager.backend.entity.EmailAddress;
import com.contactmanager.backend.entity.User;
import com.contactmanager.backend.entity.User.IdentifierType;

@DataJpaTest
class RepositoryTests {

    @Autowired
    private ContactRepository contactRepository;

    @Autowired
    private UserRepository userRepository;

    private User ownerA;
    private User ownerB;

    @BeforeEach
    void setUp() {
        ownerA = userRepository.save(new User("Owner", "A", "owner-a@example.com", IdentifierType.EMAIL, "hash"));
        ownerB = userRepository.save(new User("Owner", "B", "owner-b@example.com", IdentifierType.EMAIL, "hash"));
        contactRepository.save(contact(ownerA, "Ada", "Lovelace", "Engineer", "zeta@example.com"));
        contactRepository.save(contact(ownerA, "Grace", "Hopper", "Admiral", "alpha@example.com"));
        contactRepository.save(contact(ownerA, "Alan", "Turing", "Engineer", "middle@example.com"));
        contactRepository.save(contact(ownerB, "Ada", "Byron", "Writer", "other@example.com"));
        contactRepository.flush();
    }

    @Test
    void lookupByRegistrationIdentifierIsExactAndOwnerContactLookupIsIsolated() {
        assertThat(userRepository.findByIdentifier("owner-a@example.com")).contains(ownerA);
        assertThat(userRepository.existsByIdentifier("owner-a@example.com")).isTrue();

        Contact ownerAContact = contactRepository.findContacts(
                ownerA.getId(), "Lovelace", "", PageRequest.of(0, 10)).getContent().getFirst();
        assertThat(contactRepository.findByIdAndOwnerId(ownerAContact.getId(), ownerA.getId())).contains(ownerAContact);
        assertThat(contactRepository.findByIdAndOwnerId(ownerAContact.getId(), ownerB.getId())).isEmpty();
    }

    @Test
    void searchesFirstAndLastNameWithinOwnerAndSupportsPagination() {
        Page<Contact> firstName = contactRepository.findContacts(
                ownerA.getId(), "ada", "", PageRequest.of(0, 10));
        Page<Contact> lastName = contactRepository.findContacts(
                ownerA.getId(), "HOPPER", "", PageRequest.of(0, 10));
        Page<Contact> firstPage = contactRepository.findContacts(
                ownerA.getId(), "", "", PageRequest.of(0, 2, Sort.by("firstName")));

        assertThat(firstName.getContent()).extracting(Contact::getLastName).containsExactly("Lovelace");
        assertThat(lastName.getContent()).extracting(Contact::getFirstName).containsExactly("Grace");
        assertThat(firstPage.getContent()).hasSize(2);
        assertThat(firstPage.getTotalElements()).isEqualTo(3);
        assertThat(firstPage.getTotalPages()).isEqualTo(2);
    }

    @Test
    void filtersTitlesAndReturnsOnlyOwnersDistinctTitles() {
        Page<Contact> engineers = contactRepository.findContacts(
                ownerA.getId(), "", "Engineer", PageRequest.of(0, 10));

        assertThat(engineers.getContent()).extracting(Contact::getFirstName)
                .containsExactlyInAnyOrder("Ada", "Alan");
        assertThat(contactRepository.findDistinctTitles(ownerA.getId()))
                .containsExactly("Admiral", "Engineer");
        assertThat(contactRepository.findDistinctTitles(ownerB.getId())).containsExactly("Writer");
    }

    @Test
    void customEmailSortIsOwnerScopedAndAlphabetical() {
        Page<Contact> contacts = contactRepository.findContactsSortedByEmail(
                ownerA.getId(), "", "", PageRequest.of(0, 10));

        assertThat(contacts.getContent()).extracting(Contact::getFirstName)
                .containsExactly("Grace", "Alan", "Ada");
        assertThat(contacts.getTotalElements()).isEqualTo(3);
    }

    private Contact contact(User owner, String firstName, String lastName, String title, String emailValue) {
        Contact contact = new Contact();
        contact.setOwner(owner);
        contact.setFirstName(firstName);
        contact.setLastName(lastName);
        contact.setTitle(title);
        EmailAddress email = new EmailAddress();
        email.setEmail(emailValue);
        email.setLabel("Work");
        email.setContact(contact);
        contact.setEmailAddresses(List.of(email));
        return contact;
    }
}
