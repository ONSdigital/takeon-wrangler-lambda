package uk.gov.ons.validation.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WranglerResponse {
    //Future UseCase i.e. response from this Lambda passed to another Lambda
    private WranglerResponseData responses;

}
