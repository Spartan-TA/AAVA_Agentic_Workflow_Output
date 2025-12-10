package com.warehouse.ems.service;

import com.warehouse.ems.dto.CertificationDto;
import java.util.List;

public interface CertificationService {
    CertificationDto getCertificationById(Long id);
    List<CertificationDto> getAllCertifications();
    CertificationDto createCertification(CertificationDto certificationDto);
    List<CertificationDto> getExpiringCertifications();
    void deleteCertification(Long id);
}
