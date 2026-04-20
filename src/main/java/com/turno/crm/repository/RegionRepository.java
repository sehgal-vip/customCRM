package com.turno.crm.repository;

import com.turno.crm.model.entity.Region;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RegionRepository extends JpaRepository<Region, Long> {

    List<Region> findByActiveTrue();
}
