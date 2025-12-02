package com.wms.ems.onboarding;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class OnboardingService {

    // New hire provisioning workflow
    @Transactional
    public void provisionNewHire(NewHireDto dto) {
        // Provisioning logic here
    }

    // Offboarding logic
    @Transactional
    public void offboardEmployee(OffboardDto dto) {
        // Deprovisioning logic and asset collection here
    }
}
