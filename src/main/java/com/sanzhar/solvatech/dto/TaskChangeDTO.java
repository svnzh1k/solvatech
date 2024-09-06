package com.sanzhar.solvatech.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;


@Getter
@Setter
public class TaskChangeDTO {

    @NotBlank(message="Заголовок не может быть пустым")
    private String title;

    @NotBlank(message="описание не может быть пустым")
    private String description;

    @NotNull
    private Boolean changeState;
}
