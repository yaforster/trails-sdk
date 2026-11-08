package io.github.yaforster.trails.core.test;

import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class TestStepTest {

    /**
     * Tests the behavior of the getTestPathsFromThisStep method for a single isolated TestStep without any child steps.
     */
    @Test
    void getTestPathsFromThisStep_singleStep() {
        TestStep singleStep = new TestStep(null, Collections.emptyList());

        List<List<TestStep>> result = singleStep.getTestPathsFromThisStep();

        assertEquals(1, result.size());
        assertEquals(1, result.get(0)
                              .size());
        assertEquals(singleStep, result.get(0)
                                       .get(0));
    }

    /**
     * Tests the getTestPathsFromThisStep method for a chain of TestSteps.
     */
    @Test
    void getTestPathsFromThisStep_stepChain() {
        TestStep step3 = new TestStep(null, Collections.emptyList());
        TestStep step2 = new TestStep(null, List.of(step3));
        TestStep step1 = new TestStep(null, List.of(step2));

        List<List<TestStep>> result = step1.getTestPathsFromThisStep();

        assertEquals(1, result.size());
        assertEquals(3, result.get(0)
                              .size());
        assertEquals(step1, result.get(0)
                                  .get(0));
        assertEquals(step2, result.get(0)
                                  .get(1));
        assertEquals(step3, result.get(0)
                                  .get(2));
    }

    /**
     * Tests the getTestPathsFromThisStep method for a TestStep with multiple child TestSteps.
     */
    @Test
    void getTestPathsFromThisStep_multipleChildSteps() {
        TestStep leafStep1 = new TestStep(null, Collections.emptyList());
        TestStep leafStep2 = new TestStep(null, Collections.emptyList());
        TestStep parentStep = new TestStep(null, List.of(leafStep1, leafStep2));

        List<List<TestStep>> result = parentStep.getTestPathsFromThisStep();

        assertEquals(2, result.size());

        assertEquals(2, result.get(0)
                              .size());
        assertEquals(parentStep, result.get(0)
                                       .get(0));
        assertEquals(leafStep1, result.get(0)
                                      .get(1));

        assertEquals(2, result.get(1)
                              .size());
        assertEquals(parentStep, result.get(1)
                                       .get(0));
        assertEquals(leafStep2, result.get(1)
                                      .get(1));
    }

    /**
     * Tests the getTestPathsFromThisStep method for a TestStep with a complex branching structure.
     */
    @Test
    void getTestPathsFromThisStep_complexStructure() {
        TestStep leafStep1 = new TestStep(null, Collections.emptyList());
        TestStep leafStep2 = new TestStep(null, Collections.emptyList());
        TestStep intermediateStep = new TestStep(null, List.of(leafStep1));
        TestStep rootStep = new TestStep(null, List.of(intermediateStep, leafStep2));

        List<List<TestStep>> result = rootStep.getTestPathsFromThisStep();

        assertEquals(2, result.size());

        assertEquals(3, result.get(0)
                              .size());
        assertEquals(rootStep, result.get(0)
                                     .get(0));
        assertEquals(intermediateStep, result.get(0)
                                             .get(1));
        assertEquals(leafStep1, result.get(0)
                                      .get(2));

        assertEquals(2, result.get(1)
                              .size());
        assertEquals(rootStep, result.get(1)
                                     .get(0));
        assertEquals(leafStep2, result.get(1)
                                      .get(1));
    }

}
