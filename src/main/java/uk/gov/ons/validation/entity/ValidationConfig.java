package uk.gov.ons.validation.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;


@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ValidationConfig {
    private String questionCode;
    private String derivedQuestionCode;

    public List<ValidationConfig> getValidationConfiguration() {
        List<ValidationConfig> configuration = new ArrayList<>();
        ValidationConfig config1 = new ValidationConfig();
        config1.setQuestionCode("601");
        config1.setDerivedQuestionCode("700");
        configuration.add(config1);
        ValidationConfig config2 = new ValidationConfig();
        config2.setQuestionCode("602");
        config2.setDerivedQuestionCode("701");
        configuration.add(config2);
        return configuration;
    }
}
