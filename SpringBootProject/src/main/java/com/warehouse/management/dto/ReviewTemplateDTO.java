package com.warehouse.management.dto;

import jakarta.validation.constraints.*;
import lombok.*;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReviewTemplateDTO {
    @NotNull
    private Long id;

    @NotBlank
    @Size(max = 100)
    private String name;

    @NotNull
    @Size(min = 1)
    private List<@NotBlank String> goals;

    @NotNull
    @Size(min = 1)
    private List<@NotBlank String> competencies;
}