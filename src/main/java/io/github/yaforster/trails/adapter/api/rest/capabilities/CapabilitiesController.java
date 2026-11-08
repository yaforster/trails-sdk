package io.github.yaforster.trails.adapter.api.rest.capabilities;

import io.github.yaforster.trails.adapter.api.rest.model.CapabilityDTO;
import io.github.yaforster.trails.adapter.api.rest.model.CapabilityNameDTO;
import io.github.yaforster.trails.app.services.CapabilitiesService;
import io.github.yaforster.trails.core.Capability;
import java.util.List;
import java.util.Objects;
import org.openapitools.api.CapabilitiesApi;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

/** Exposes the complete, backend-owned capability metadata. */
@RestController
public class CapabilitiesController implements CapabilitiesApi {

    private final CapabilitiesService capabilities;

    public CapabilitiesController(CapabilitiesService capabilities) {
        this.capabilities = Objects.requireNonNull(capabilities, "Capabilities service is required");
    }

    @Override
    public ResponseEntity<List<CapabilityDTO>> listCapabilities() {
        return ResponseEntity.ok(capabilities.capabilities()
                                             .stream()
                                             .map(this::response)
                                             .toList());
    }

    private CapabilityDTO response(Capability capability) {
        return new CapabilityDTO(CapabilityNameDTO.valueOf(capability.label()
                                                                     .name()),
                capability.description(), capability.value());
    }

}
