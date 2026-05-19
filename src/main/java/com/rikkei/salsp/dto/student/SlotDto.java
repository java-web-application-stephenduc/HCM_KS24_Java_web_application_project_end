package com.rikkei.salsp.dto.student;

import java.time.LocalTime;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Lớp `SlotDto` thuộc hệ thống Smart Academic Lab Support Platform (SALSP).
 */
@Getter
@Setter
@NoArgsConstructor
public class SlotDto {

    private LocalTime startTime;
    private LocalTime endTime;
    private boolean available;
}

