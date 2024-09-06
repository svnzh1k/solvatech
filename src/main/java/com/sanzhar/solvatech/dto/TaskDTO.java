package com.sanzhar.solvatech.dto;


import java.time.LocalDate;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;

@Getter
public class TaskDTO {

    @NotBlank(message="Заголовок не может быть пустым")
    private String title;

    @NotBlank(message="описание не может быть пустым")
    private String description;

    @NotNull(message="пожалуйтса укажите крайний срок выполнения задачи")
    private LocalDate deadline;
}
