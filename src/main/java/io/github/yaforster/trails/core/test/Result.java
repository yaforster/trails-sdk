package io.github.yaforster.trails.core.test;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.experimental.SuperBuilder;

@SuperBuilder
@Getter
@AllArgsConstructor
public abstract class Result {

    private Long actionID;
    private String label;
    private String resultMessage;
    private String base64Screenshot;

}
