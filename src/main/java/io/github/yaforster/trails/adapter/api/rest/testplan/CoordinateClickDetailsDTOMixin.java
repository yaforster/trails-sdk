package io.github.yaforster.trails.adapter.api.rest.testplan;

import tools.jackson.databind.annotation.JsonDeserialize;

@JsonDeserialize(using = CoordinateClickDetailsRawDeserializer.class)
abstract class CoordinateClickDetailsDTOMixin {

}
