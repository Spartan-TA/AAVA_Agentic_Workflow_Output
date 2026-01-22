package com.warehouse.ems.asset;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityNotFoundException;
import java.util.List;

/**
 * Service for managing equipment/assets, including CRUD and condition tracking.
 */
@Service
public class EquipmentService {
    private final EquipmentRepository equipmentRepository;

    @Autowired
    public EquipmentService(EquipmentRepository equipmentRepository) {
        this.equipmentRepository = equipmentRepository;
    }

    /**
     * Create a new equipment.
     */
    @Transactional
    public Equipment createEquipment(Equipment equipment) {
        if (equipmentRepository.existsBySerialNumber(equipment.getSerialNumber())) {
            throw new IllegalArgumentException("Serial number already exists: " + equipment.getSerialNumber());
        }
        return equipmentRepository.save(equipment);
    }

    /**
     * Get all equipment.
     */
    public List<Equipment> getAllEquipment() {
        return equipmentRepository.findAll();
    }

    /**
     * Get equipment by ID.
     */
    public Equipment getEquipment(Long id) {
        return equipmentRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Equipment not found with id: " + id));
    }

    /**
     * Update equipment details.
     */
    @Transactional
    public Equipment updateEquipment(Long id, Equipment updated) {
        Equipment eq = getEquipment(id);
        eq.setType(updated.getType());
        eq.setSerialNumber(updated.getSerialNumber());
        eq.setCondition(updated.getCondition());
        eq.setLastMaintenanceDate(updated.getLastMaintenanceDate());
        return equipmentRepository.save(eq);
    }

    /**
     * Delete equipment.
     */
    @Transactional
    public void deleteEquipment(Long id) {
        if (!equipmentRepository.existsById(id)) {
            throw new EntityNotFoundException("Equipment not found with id: " + id);
        }
        equipmentRepository.deleteById(id);
    }
}
