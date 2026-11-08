package io.github.yaforster.trails.adapter.api.rest.testplan;

import tools.jackson.databind.annotation.JsonDeserialize;

@JsonDeserialize(using = ResizeViewportDetailsRawDeserializer.class)
abstract class ResizeViewportDetailsDTOMixin {

}
