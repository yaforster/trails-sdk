package io.github.yaforster.trails.adapter.api.rest.testplan;

import io.github.yaforster.trails.adapter.api.rest.model.ActionDefinitionDTO;

record IndexedAction(ActionDefinitionDTO action, int index) {
}
