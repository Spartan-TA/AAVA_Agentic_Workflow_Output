package com.wems.safety.dto;

import lombok.Data;

@Data
public class OshaSummaryDto {
    private int totalIncidents;
    private int recordableIncidents;
    private int daysAwayFromWork;
    private int injuries;
    private int nearMisses;
    private int propertyDamages;
    private int environmentalIncidents;
    private int securityIncidents;
}
