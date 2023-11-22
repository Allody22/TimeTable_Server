package ru.nsu.server.payload.requests;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RoomRequest {

    @NotBlank
    private String name;

    @NotBlank
    private String type;

    private int capacity;
}
