package com.example.training;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class CertificationService {
    @Autowired
    private CertificationRepository certificationRepository;

    public List<Certification> getAllCertifications() {
        return certificationRepository.findAll();
    }

    public Optional<Certification> getCertificationById(Long id) {
        return certificationRepository.findById(id);
    }

    public Certification saveCertification(Certification certification) {
        return certificationRepository.save(certification);
    }

    public void deleteCertification(Long id) {
        certificationRepository.deleteById(id);
    }
}