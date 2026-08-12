package vn.edu.eaut.recruitflow.model;

import java.util.Collections;
import java.util.List;

/** Immutable pagination value passed from services/controllers to JSP views. */
public class PageResult<T> {
    private final List<T> items;
    private final int currentPage;
    private final int pageSize;
    private final long totalItems;
    private final int totalPages;

    public PageResult(List<T> items, int currentPage, int pageSize, long totalItems) {
        this.items = items == null ? Collections.emptyList() : List.copyOf(items);
        this.currentPage = Math.max(1, currentPage);
        this.pageSize = Math.max(1, pageSize);
        this.totalItems = Math.max(0, totalItems);
        this.totalPages = this.totalItems == 0 ? 0 : (int) ((this.totalItems + this.pageSize - 1) / this.pageSize);
    }

    public List<T> getItems() { return items; }
    public int getCurrentPage() { return currentPage; }
    public int getPageSize() { return pageSize; }
    public long getTotalItems() { return totalItems; }
    public int getTotalPages() { return totalPages; }
    public boolean isHasPrevious() { return currentPage > 1; }
    public boolean isHasNext() { return currentPage < totalPages; }
}
