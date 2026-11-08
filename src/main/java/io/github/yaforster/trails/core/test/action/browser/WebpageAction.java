package io.github.yaforster.trails.core.test.action.browser;

import io.github.yaforster.trails.core.test.Action;
import io.github.yaforster.trails.core.test.TestExecutionContext;
import io.github.yaforster.trails.core.test.action.element.Locator;
import lombok.Getter;
import lombok.experimental.SuperBuilder;

@SuperBuilder
@Getter
public abstract class WebpageAction extends Action {

    protected final Locator locatorForElementToActOn;

    protected String getTechnicalFailureMessage() {
        return "Could not interact with the required web page element.";
    }

    @Override
    protected String getPageScreenshot(TestExecutionContext context) {
        return context.browser()
                      .captureScreenshot(locatorForElementToActOn);
    }

}
