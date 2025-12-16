package com.warehouse.management.common.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.util.List;

/**
 * Standard paginated API response wrapper.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Paginated API response wrapper")
public class PageResponse<T> {
    @Schema(example = "0")
    private int pageNumber;
    @Schema(example = "10")
    private int pageSize;
    @Schema(example = "1")
    private int totalPages;
    @Schema(example = "1")
    private long totalElements;
    private List<T> content;
}
