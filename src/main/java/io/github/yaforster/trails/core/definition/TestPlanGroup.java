package io.github.yaforster.trails.core.definition;

import java.math.BigDecimal;
import java.util.List;

public record TestPlanGroup(String label, BigDecimal xCoordinate, BigDecimal yCoordinate, BigDecimal widthPixels,
        BigDecimal heightPixels, List<Long> actionReferenceIds) {
    public TestPlanGroup {
        actionReferenceIds = List.copyOf(actionReferenceIds);
    }

}
