package io.github.yaforster.trails.core;

import java.util.List;
import java.util.Objects;
import java.util.function.Function;

public record PagedResult<T>(List<T> items, int page, int size, long totalItems) {

    public PagedResult {
        items = List.copyOf(items);
        if (page < 0) {
            throw new IllegalArgumentException("Page must not be negative.");
        }
        if (size < 1) {
            throw new IllegalArgumentException("Page size must be positive.");
        }
        if (totalItems < items.size()) {
            throw new IllegalArgumentException("Total items must not be smaller than the page items.");
        }
    }

    public int totalPages() {
        long fullPages = totalItems / size;
        long trailingPage = 0;
        if (totalItems % size != 0) {
            trailingPage = 1;
        }
        return Math.toIntExact(fullPages + trailingPage);
    }

    public static <T> PagedResult<T> singlePage(List<T> items) {
        return new PagedResult<>(items, 0, Math.max(items.size(), 1), items.size());
    }

    public List<T> getContent() {
        return items;
    }

    public int getNumber() {
        return page;
    }

    public int getSize() {
        return size;
    }

    public long getTotalElements() {
        return totalItems;
    }

    public int getTotalPages() {
        return totalPages();
    }

    public boolean hasNext() {
        return page + 1 < totalPages();
    }

    public <R> PagedResult<R> map(Function<T, R> mapper) {
        Objects.requireNonNull(mapper, "Mapper must not be null.");
        return new PagedResult<>(items.stream()
                                      .map(mapper)
                                      .toList(),
                page, size, totalItems);
    }

}
