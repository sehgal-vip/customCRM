package com.turno.crm.repository;

import com.turno.crm.model.entity.Operator;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface OperatorRepository extends JpaRepository<Operator, Long>, JpaSpecificationExecutor<Operator> {

    Optional<Operator> findByCompanyNameAndPhone(String companyName, String phone);

    @Query(value = "SELECT * FROM operators WHERE company_name ILIKE '%' || :q || '%' OR phone ILIKE '%' || :q || '%' OR email ILIKE '%' || :q || '%'", nativeQuery = true)
    List<Operator> search(@Param("q") String query);
}
