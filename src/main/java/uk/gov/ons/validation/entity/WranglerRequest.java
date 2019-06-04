package uk.gov.ons.validation.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WranglerRequest {

    private String period;
    private String reference;
    private String survey;
    private String instance;

    private List<QuestionInputData> responses;

}
