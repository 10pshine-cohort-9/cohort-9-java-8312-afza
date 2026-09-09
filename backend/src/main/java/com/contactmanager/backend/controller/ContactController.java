package com.contactmanager.backend.controller;

import java.util.List;
import java.util.Set;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import com.contactmanager.backend.service.AuthenticatedUser;

import com.contactmanager.backend.entity.Contact;
import com.contactmanager.backend.service.ContactService;
@RestController
@RequestMapping("/api/contacts")
public class ContactController {

    private static final Logger logger = LoggerFactory.getLogger(ContactController.class);

    private final ContactService contactService;

    public ContactController(ContactService contactService) {
        this.contactService = contactService;
    }

    @PostMapping
    public Contact createContact(Authentication authentication, @RequestBody Contact contact) {
        contact.setId(null);
        Long userId = principal(authentication).userId();
        Contact createdContact = contactService.saveContact(userId, contact);
        logger.info("Contact created: contactId={}, userId={}", createdContact.getId(), userId);
        return createdContact;
    }

    private static final int DEFAULT_PAGE_SIZE = 9;
    private static final int MAX_PAGE_SIZE = 100;
    private static final Set<String> ALLOWED_SORT_FIELDS = Set.of("firstName", "title", "email");

    @GetMapping
    public Page<Contact> getContacts(
            Authentication authentication,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "" + DEFAULT_PAGE_SIZE) int size,
            @RequestParam(defaultValue = "") String search,
            @RequestParam(defaultValue = "") String title,
            @RequestParam(defaultValue = "firstName") String sort) {
        int safePage = Math.max(page, 0);
        int safeSize = Math.min(Math.max(size, 1), MAX_PAGE_SIZE);
        String safeSort = ALLOWED_SORT_FIELDS.contains(sort) ? sort : "firstName";
        Long userId = principal(authentication).userId();
        if ("email".equals(safeSort)) {
            return contactService.getContactsSortedByEmail(
                    userId,
                    search,
                    title,
                    PageRequest.of(safePage, safeSize));
        }
        Sort contactSort = Sort.by(Sort.Order.asc(safeSort).ignoreCase(), Sort.Order.asc("id"));
        return contactService.getContacts(userId, search, title, PageRequest.of(safePage, safeSize, contactSort));
    }

    @GetMapping("/titles")
    public List<String> getContactTitles(Authentication authentication) {
        return contactService.getContactTitles(principal(authentication).userId());
    }
    @GetMapping("/{id}")
    public ResponseEntity<Contact> getContact(Authentication authentication, @PathVariable("id") Long id) {
        Contact contact = contactService.getContactById(principal(authentication).userId(), id);
        return contact == null ? ResponseEntity.notFound().build() : ResponseEntity.ok(contact);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteContact(Authentication authentication, @PathVariable("id") Long id) {
        Long userId = principal(authentication).userId();
        if (!contactService.deleteContact(userId, id)) {
            logger.warn("Contact deletion failed because contact was not found: contactId={}, userId={}", id, userId);
            return ResponseEntity.notFound().build();
        }
        logger.info("Contact deleted: contactId={}, userId={}", id, userId);
        return ResponseEntity.ok("Contact Deleted Successfully");
    }

    @PutMapping("/{id}")
    public ResponseEntity<Contact> updateContact(
            Authentication authentication,
            @PathVariable("id") Long id,
            @RequestBody Contact contact) {
        Long userId = principal(authentication).userId();
        Contact updatedContact = contactService.updateContact(userId, id, contact);
        if (updatedContact == null) {
            logger.warn("Contact update failed because contact was not found: contactId={}, userId={}", id, userId);
            return ResponseEntity.notFound().build();
        }
        logger.info("Contact updated: contactId={}, userId={}", id, userId);
        return ResponseEntity.ok(updatedContact);
    }

    private AuthenticatedUser principal(Authentication authentication) {
        return AuthenticationController.principal(authentication);
    }
}
