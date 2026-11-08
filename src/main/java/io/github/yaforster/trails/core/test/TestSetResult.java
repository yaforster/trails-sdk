package io.github.yaforster.trails.core.test;

public record TestSetResult(TestCaseResult testCaseResult, int totalRunTime, Browser browserToRunIn,
        String testPlanLabel) {

}
