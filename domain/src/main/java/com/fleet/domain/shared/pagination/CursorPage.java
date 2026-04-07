package com.fleet.domain.shared.pagination;

import java.util.List;

/**
 * A cursor-based page of results.
 *
 * <p>Cursor-based pagination is more stable than offset-based for real-time data:
 * inserting or deleting rows won't shift pages and cause skipped/duplicated results.</p>
 *
 * <p>The {@code nextCursor} is an opaque string (typically a Base64-encoded last-row ID
 * or timestamp) that the caller passes back in the next request. It is {@code null}
 * when there are no more pages.</p>
 *
 * @param <T>        the type of items in the page
 * @param items      the items in this page
 * @param nextCursor opaque cursor for fetching the next page, or {@code null} if last page
 * @param hasMore    {@code true} if more items exist beyond this page
 */
public record CursorPage<T>(List<T> items, String nextCursor, boolean hasMore) {

    /** Factory for the last page (no more results). */
    public static <T> CursorPage<T> lastPage(List<T> items) {
        return new CursorPage<>(items, null, false);
    }

    /** Factory for an intermediate page with a cursor for the next fetch. */
    public static <T> CursorPage<T> of(List<T> items, String nextCursor) {
        return new CursorPage<>(items, nextCursor, true);
    }

    /**
     * Transforms each item in this page using the given mapping function,
     * preserving the cursor and hasMore metadata.
     */
    public <R> CursorPage<R> map(java.util.function.Function<T, R> mapper) {
        return new CursorPage<>(
                items.stream().map(mapper).toList(),
                nextCursor,
                hasMore);
    }
}
