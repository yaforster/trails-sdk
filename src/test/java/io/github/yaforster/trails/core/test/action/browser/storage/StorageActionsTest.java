package io.github.yaforster.trails.core.test.action.browser.storage;

import io.github.yaforster.trails.core.test.Result;
import io.github.yaforster.trails.core.test.TestExecutionContexts;
import io.github.yaforster.trails.core.test.action.value.FixedValueInstruction;
import io.github.yaforster.trails.core.test.result.ValidationFailure;
import io.github.yaforster.trails.core.test.result.Success;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertInstanceOf;

class StorageActionsTest {

    @Test
    void cookieCheck_returnsSuccess_whenCookieContainsExpectedValue() {
        Result result = CheckCookieAction.builder()
                                         .actionID(1L)
                                         .label("cookie")
                                         .nextActions(List.of())
                                         .cookieName("session")
                                         .computationInstruction(new FixedValueInstruction("token"))
                                         .build()
                                         .execute(TestExecutionContexts.withBrowser(
                                                 new TestExecutionContexts.FakeBrowserSession().withCookieValue(
                                                         "my token")));

        assertInstanceOf(Success.class, result);
    }

    @Test
    void cookieCheck_returnsValidationFailure_whenCookieIsMissing() {
        Result result = CheckCookieAction.builder()
                                         .actionID(1L)
                                         .label("cookie")
                                         .nextActions(List.of())
                                         .cookieName("session")
                                         .computationInstruction(new FixedValueInstruction("token"))
                                         .build()
                                         .execute(TestExecutionContexts.withBrowser(
                                                 new TestExecutionContexts.FakeBrowserSession()));

        assertInstanceOf(ValidationFailure.class, result);
    }

    @Test
    void localStorageCheck_returnsSuccess_whenValueContainsExpectedText() {
        Result result = CheckLocalStorageAction.builder()
                                               .actionID(2L)
                                               .label("local")
                                               .nextActions(List.of())
                                               .localStorageItemKey("state")
                                               .computationInstruction(new FixedValueInstruction("ready"))
                                               .build()
                                               .execute(TestExecutionContexts.withBrowser(
                                                       new TestExecutionContexts.FakeBrowserSession().withLocalStorageValue(
                                                               "is-ready")));

        assertInstanceOf(Success.class, result);
    }

    @Test
    void localStorageCheck_returnsValidationFailure_whenValueIsBlank() {
        Result result = CheckLocalStorageAction.builder()
                                               .actionID(2L)
                                               .label("local")
                                               .nextActions(List.of())
                                               .localStorageItemKey("state")
                                               .computationInstruction(new FixedValueInstruction("ready"))
                                               .build()
                                               .execute(TestExecutionContexts.withBrowser(
                                                       new TestExecutionContexts.FakeBrowserSession().withLocalStorageValue(
                                                               "")));

        assertInstanceOf(ValidationFailure.class, result);
    }

    @Test
    void sessionStorageCheck_returnsSuccess_whenValueContainsExpectedText() {
        Result result = CheckSessionStorageAction.builder()
                                                 .actionID(3L)
                                                 .label("session")
                                                 .nextActions(List.of())
                                                 .sessionStorageItemKey("state")
                                                 .computationInstruction(new FixedValueInstruction("ready"))
                                                 .build()
                                                 .execute(TestExecutionContexts.withBrowser(
                                                         new TestExecutionContexts.FakeBrowserSession().withSessionStorageValue(
                                                                 "is-ready")));

        assertInstanceOf(Success.class, result);
    }

    @Test
    void sessionStorageCheck_returnsValidationFailure_whenValueDoesNotMatch() {
        Result result = CheckSessionStorageAction.builder()
                                                 .actionID(3L)
                                                 .label("session")
                                                 .nextActions(List.of())
                                                 .sessionStorageItemKey("state")
                                                 .computationInstruction(new FixedValueInstruction("ready"))
                                                 .build()
                                                 .execute(TestExecutionContexts.withBrowser(
                                                         new TestExecutionContexts.FakeBrowserSession().withSessionStorageValue(
                                                                 "waiting")));

        assertInstanceOf(ValidationFailure.class, result);
    }

}
