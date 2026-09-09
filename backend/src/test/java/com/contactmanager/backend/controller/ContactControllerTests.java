package com.contactmanager.backend.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;

import com.contactmanager.backend.entity.Contact;
import com.contactmanager.backend.service.AuthenticatedUser;
import com.contactmanager.backend.service.ContactService;

class ContactControllerTests {

    private ContactService contactService;
    private ContactController controller;
    private Authentication authentication;

    @BeforeEach
    void setUp() {
        contactService = mock(ContactService.class);
        controller = new ContactController(contactService);
        AuthenticatedUser principal = new AuthenticatedUser(7L, "user@example.com", "hash");
        authentication = UsernamePasswordAuthenticationToken.authenticated(
                principal, null, principal.getAuthorities());
    }

    @Test
    void listingIsOwnerScopedAndSanitizesPaginationAndSort() {
        Page<Contact> page = Page.empty(PageRequest.of(0, 100));
        when(contactService.getContacts(any(), any(), any(), any())).thenReturn(page);

        assertThat(controller.getContacts(authentication, -2, 500, "Ada", "Work", "unsupported"))
                .isSameAs(page);
        verify(contactService).getContacts(7L, "Ada", "Work",
                PageRequest.of(0, 100, org.springframework.data.domain.Sort.by(
                        org.springframework.data.domain.Sort.Order.asc("firstName").ignoreCase(),
                        org.springframework.data.domain.Sort.Order.asc("id"))));
    }

    @Test
    void emailSortUsesDedicatedOwnerScopedQuery() {
        Page<Contact> page = Page.empty(PageRequest.of(0, 9));
        when(contactService.getContactsSortedByEmail(any(), any(), any(), any())).thenReturn(page);

        assertThat(controller.getContacts(authentication, 0, 9, "Ada", "Work", "email"))
                .isSameAs(page);
        verify(contactService).getContactsSortedByEmail(
                7L, "Ada", "Work", PageRequest.of(0, 9));
    }

    @Test
    void retrievesOwnedContactAndReturnsNotFoundForUnavailableContact() {
        Contact contact = new Contact();
        when(contactService.getContactById(7L, 10L)).thenReturn(contact);

        assertThat(controller.getContact(authentication, 10L).getBody()).isSameAs(contact);
        assertThat(controller.getContact(authentication, 11L).getStatusCode().value()).isEqualTo(404);
    }

    @Test
    void createsContactForAuthenticatedOwnerAndIgnoresClientId() {
        Contact contact = new Contact();
        contact.setId(99L);
        when(contactService.saveContact(7L, contact)).thenReturn(contact);

        assertThat(controller.createContact(authentication, contact)).isSameAs(contact);
        assertThat(contact.getId()).isNull();
        verify(contactService).saveContact(7L, contact);
    }

    @Test
    void updateAndDeleteExposeSuccessAndOwnerScopedNotFoundContracts() {
        Contact contact = new Contact();
        when(contactService.updateContact(7L, 10L, contact)).thenReturn(contact);
        when(contactService.deleteContact(7L, 10L)).thenReturn(true);

        assertThat(controller.updateContact(authentication, 10L, contact).getStatusCode().value()).isEqualTo(200);
        assertThat(controller.updateContact(authentication, 11L, contact).getStatusCode().value()).isEqualTo(404);
        assertThat(controller.deleteContact(authentication, 10L).getStatusCode().value()).isEqualTo(200);
        assertThat(controller.deleteContact(authentication, 11L).getStatusCode().value()).isEqualTo(404);
    }

    @Test
    void rejectsRequestsWithoutAuthenticatedApplicationPrincipal() {
        Authentication otherPrincipal = UsernamePasswordAuthenticationToken.authenticated("user", null, java.util.List.of());

        assertThatThrownBy(() -> controller.getContact(otherPrincipal, 10L))
                .isInstanceOf(IllegalStateException.class);
    }
}
