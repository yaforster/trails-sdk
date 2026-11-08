package io.github.yaforster.trails.core.test;

public record TestSet(TestCase testCase, DriverProvider webdriverProvider, Browser browserToRunIn,
        String testPlanLabel) {

    public static final int DIVISOR_FOR_MILLISECONDS = 1000000;

    public TestSetResult runTests() {
        long startTime = System.nanoTime();
        TestCaseResult result = testCase.run(webdriverProvider, browserToRunIn);
        long runtime = (System.nanoTime() - startTime) / DIVISOR_FOR_MILLISECONDS;
        return new TestSetResult(result, Math.round(runtime), browserToRunIn, testPlanLabel);
    }
}
