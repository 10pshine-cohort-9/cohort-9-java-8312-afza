package com.contactmanager.backend.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.contactmanager.backend.entity.Contact;

public interface ContactRepository extends JpaRepository<Contact, Long> {

    @Query("""
            SELECT c FROM Contact c
            WHERE c.owner.id = :ownerId
            AND (:search = '' OR
                LOWER(COALESCE(c.firstName, '')) LIKE LOWER(CONCAT('%', :search, '%')) OR
                LOWER(COALESCE(c.lastName, '')) LIKE LOWER(CONCAT('%', :search, '%')) OR
                EXISTS (SELECT e.id FROM EmailAddress e WHERE e.contact = c
                    AND LOWER(e.email) LIKE LOWER(CONCAT('%', :search, '%'))) OR
                EXISTS (SELECT p.id FROM PhoneNumber p WHERE p.contact = c
                    AND p.phoneNumber LIKE CONCAT('%', :search, '%')))
            AND (:title = '' OR c.title = :title)
            """)
    Page<Contact> findContacts(
            @Param("ownerId") Long ownerId,
            @Param("search") String search,
            @Param("title") String title,
            Pageable pageable);

    @Query(value = """
            SELECT c FROM Contact c
            LEFT JOIN c.emailAddresses email
            WHERE c.owner.id = :ownerId
            AND (:search = '' OR
                LOWER(COALESCE(c.firstName, '')) LIKE LOWER(CONCAT('%', :search, '%')) OR
                LOWER(COALESCE(c.lastName, '')) LIKE LOWER(CONCAT('%', :search, '%')) OR
                EXISTS (SELECT e.id FROM EmailAddress e WHERE e.contact = c
                    AND LOWER(e.email) LIKE LOWER(CONCAT('%', :search, '%'))) OR
                EXISTS (SELECT p.id FROM PhoneNumber p WHERE p.contact = c
                    AND p.phoneNumber LIKE CONCAT('%', :search, '%')))
            AND (:title = '' OR c.title = :title)
            GROUP BY c
            ORDER BY MIN(LOWER(email.email)), c.id
            """, countQuery = """
            SELECT COUNT(c) FROM Contact c
            WHERE c.owner.id = :ownerId
            AND (:search = '' OR
                LOWER(COALESCE(c.firstName, '')) LIKE LOWER(CONCAT('%', :search, '%')) OR
                LOWER(COALESCE(c.lastName, '')) LIKE LOWER(CONCAT('%', :search, '%')) OR
                EXISTS (SELECT e.id FROM EmailAddress e WHERE e.contact = c
                    AND LOWER(e.email) LIKE LOWER(CONCAT('%', :search, '%'))) OR
                EXISTS (SELECT p.id FROM PhoneNumber p WHERE p.contact = c
                    AND p.phoneNumber LIKE CONCAT('%', :search, '%')))
            AND (:title = '' OR c.title = :title)
            """)
    Page<Contact> findContactsSortedByEmail(
            @Param("ownerId") Long ownerId,
            @Param("search") String search,
            @Param("title") String title,
            Pageable pageable);

    @Query("SELECT DISTINCT c.title FROM Contact c WHERE c.owner.id = :ownerId AND c.title IS NOT NULL AND c.title <> '' ORDER BY c.title")
    List<String> findDistinctTitles(@Param("ownerId") Long ownerId);

    java.util.Optional<Contact> findByIdAndOwnerId(Long id, Long ownerId);

}
