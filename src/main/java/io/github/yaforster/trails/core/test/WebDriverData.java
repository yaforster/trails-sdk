package io.github.yaforster.trails.core.test;

import java.net.URL;

public record WebDriverData(DriverProvider webdriverProvider, URL webAppURL, Browser browserName) {

}
