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
    public PostRequest(String period, String reference,  String survey, String instance, String validationName,
                       List<ValidationResult> validationResults) {
        super(period, reference,  survey, instance, validationName);
        this.validationResults = validationResults;
    }

}
