package com.amisha.moviebooking.dto;

import java.util.List;

public class BulkCancelResponse {

    private int cancelled;
    private int skipped;
    private List<Long> skippedIds;   // already-cancelled or not-owned bookings

    public BulkCancelResponse(int cancelled, int skipped, List<Long> skippedIds) {
        this.cancelled = cancelled;
        this.skipped = skipped;
        this.skippedIds = skippedIds;
    }

    public int getCancelled() { return cancelled; }
    public int getSkipped() { return skipped; }
    public List<Long> getSkippedIds() { return skippedIds; }
}
