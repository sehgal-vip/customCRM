package com.turno.crm.repository;

import com.turno.crm.model.entity.Contact;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ContactRepository extends JpaRepository<Contact, Long> {

    List<Contact> findByOperatorId(Long operatorId);

    boolean existsByIdAndOperatorId(Long id, Long operatorId);

    @Query(value = "SELECT * FROM contacts WHERE name ILIKE '%' || :q || '%' OR mobile ILIKE '%' || :q || '%' OR email ILIKE '%' || :q || '%'",
           nativeQuery = true)
    List<Contact> search(@Param("q") String query);
}
