package ru.nsu.server.payload.response;

import lombok.AllArgsConstructor;
import lombok.Data;

import javax.validation.constraints.NotNull;
import java.util.List;

@Data
@AllArgsConstructor
@NotNull
public class VariantsWithVariantsSize {

    private int variantsSize;

    List<PotentialVariants> potentialVariants;
}
