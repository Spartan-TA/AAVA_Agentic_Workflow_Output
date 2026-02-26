package com.warehouse.employee.service;

import com.warehouse.employee.entity.SelfServicePortal;
import com.warehouse.employee.repository.SelfServicePortalRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class SelfServicePortalService {
    @Autowired
    private SelfServicePortalRepository selfServicePortalRepository;

    public List<SelfServicePortal> getAllSelfServicePortals() {
        return selfServicePortalRepository.findAll();
    }

    public Optional<SelfServicePortal> getSelfServicePortalById(Long id) {
        return selfServicePortalRepository.findById(id);
    }

    public SelfServicePortal saveSelfServicePortal(SelfServicePortal selfServicePortal) {
        return selfServicePortalRepository.save(selfServicePortal);
    }

    public void deleteSelfServicePortal(Long id) {
        selfServicePortalRepository.deleteById(id);
    }
}
