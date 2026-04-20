package com.turno.crm.repository;

import com.turno.crm.model.entity.TaxonomyItem;
import com.turno.crm.model.enums.TaxonomyType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TaxonomyItemRepository extends JpaRepository<TaxonomyItem, Long> {

    List<TaxonomyItem> findByTaxonomyTypeAndActiveTrue(TaxonomyType type);

    List<TaxonomyItem> findByTaxonomyType(TaxonomyType type);

    Optional<TaxonomyItem> findByTaxonomyTypeAndValue(TaxonomyType type, String value);
}
