package uk.gov.ons.validation.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@NoArgsConstructor
@AllArgsConstructor
public class KeyMetaData {
    private String period;
    private String reference;
    private String survey;
    private String instance;
    private String validationName;


}
