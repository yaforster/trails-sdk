package io.github.yaforster.trails.adapter.api.rest.common;

import io.github.yaforster.trails.adapter.api.rest.model.LinkDTO;
import io.github.yaforster.trails.core.PagedResult;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.IntFunction;
import org.springframework.http.HttpMethod;

/** Builds page navigation without deciding a resource's URI or authorization policy. */
public final class PaginationLinks {
    private PaginationLinks() {}

    public static Map<String, LinkDTO> forPage(PagedResult<?> page, IntFunction<String> pageUri) {
        int lastPage = Math.max(page.totalPages() - 1, 0);
        Map<String, LinkDTO> links = new LinkedHashMap<>();
        links.put("self", link(pageUri.apply(page.page())));
        links.put("first", link(pageUri.apply(0)));
        links.put("last", link(pageUri.apply(lastPage)));
        if (page.page() > 0) {
            links.put("prev", link(pageUri.apply(page.page() - 1)));
        }
        if (page.page() < lastPage) {
            links.put("next", link(pageUri.apply(page.page() + 1)));
        }
        return links;
    }

    private static LinkDTO link(String uri) {
        return new LinkDTO().href(uri)
                            .method(HttpMethod.GET.name())
                            .templated(false);
    }
}
