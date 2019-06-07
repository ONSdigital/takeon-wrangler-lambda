package uk.gov.ons.validation.entity;

import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
public class PostRequest extends KeyMetaData {

    private List<ValidationResult> validationResults;

    @Builder
    public PostRequest(String reference, String period, String survey, String instance, String validationName,
                       List<ValidationResult> validationResults) {
        super(reference, period, survey, instance, validationName);
        this.validationResults = validationResults;
    }

}
