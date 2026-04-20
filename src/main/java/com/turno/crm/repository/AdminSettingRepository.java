package com.turno.crm.repository;

import com.turno.crm.model.entity.AdminSetting;
import com.turno.crm.model.enums.AdminSettingType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AdminSettingRepository extends JpaRepository<AdminSetting, Long> {

    Optional<AdminSetting> findBySettingTypeAndSettingKey(AdminSettingType type, String key);

    List<AdminSetting> findBySettingType(AdminSettingType type);
}
