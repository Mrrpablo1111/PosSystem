package com.sh.sh.pos.system.payload.request;

import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateTrackingRequestDTO {
    /**
     * Tracking number provided by the carrier after dispatch.
     * Optional — pass null or empty string to clear the tracking number.
     * Max 100 characters.
     */
    @Size(max = 100, message = "Tracking number must be at most 100 characters")
    private String trackingNo;
 
    /**
     * Carrier ID to update at the same time.
     * Optional — only updates if provided.
     * e.g. "dhl", "fedex", "ninja" matching your carrier list.
     */
    @Size(max = 50, message = "Carrier ID must be at most 50 characters")
    private String carrierId;
}
