package com.rikkei.salsp.dto;

import java.time.LocalTime;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class SlotDto {

    private LocalTime startTime;
    private LocalTime endTime;
    private boolean available;
}

