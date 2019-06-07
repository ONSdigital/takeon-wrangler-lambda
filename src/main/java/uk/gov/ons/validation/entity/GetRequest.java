package uk.gov.ons.validation.entity;

import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class GetRequest extends KeyMetaData {

    @Builder
    public GetRequest(String reference, String period, String survey, String instance, String validationName) {
        super(reference, period, survey, instance, validationName);
    }

}
