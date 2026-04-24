package com.pranta.ecommerce.Exceptions;


import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ErrorResponseDto {

    private LocalDateTime timestamp;
    private String message;
    private String path;
    private int status;

}
