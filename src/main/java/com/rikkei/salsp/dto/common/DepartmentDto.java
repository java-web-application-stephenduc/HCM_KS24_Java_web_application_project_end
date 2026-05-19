package com.rikkei.salsp.dto.common;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Lớp `DepartmentDto` thuộc hệ thống Smart Academic Lab Support Platform (SALSP).
 */
@Getter
@Setter
@NoArgsConstructor
public class DepartmentDto {

    private Long id;
    private String name;
    private String code;
}

