package uk.gov.ons.validation.service;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.log4j.Log4j2;
import uk.gov.ons.validation.entity.QuestionInputData;
import uk.gov.ons.validation.entity.ValidationResult;

import static java.lang.String.format;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Log4j2
public class InvokeConfig {

    private String finalQuestCode;
    private String finalDerivedQuestCode;
    private String finalQuestCodeValue;
    private String finalDerivedQuestValue;
    private boolean isQuestionCodeFound;
    private boolean isDerivedQuestFound;
    private ValidationResult wranglerData;

    private static final String MATCH_FOUND_QUEST = "Matching object found for Question and its code is %s.";
    private static final String MATCH_FOUND_DERIVED_QUEST = "Matching object found for Derived Question and its code is %s.";

    /**
     * @param inputData
     * @param configQuestionCode
     */
    public void processQuestionCode(QuestionInputData inputData, String configQuestionCode) {
        if (inputData.getQuestionCode().equals(configQuestionCode)) {
            this.isQuestionCodeFound = true;
            this.finalQuestCode = inputData.getQuestionCode();
            this.finalQuestCodeValue = inputData.getResponse();
            log.info(format(MATCH_FOUND_QUEST, finalQuestCode));
        }

    }

    /**
     * @param inputData
     * @param configDerivedQuestCode
     */
    public void processDerivedQuestCode(QuestionInputData inputData, String configDerivedQuestCode) {
        if (inputData.getQuestionCode().equals(configDerivedQuestCode)) {
            this.isDerivedQuestFound = true;
            this.finalDerivedQuestCode = inputData.getQuestionCode();
            this.finalDerivedQuestValue = inputData.getResponse();
            log.info(format(MATCH_FOUND_DERIVED_QUEST, finalDerivedQuestCode));

        }

    }

    /**
     * Method to find both Question and derived question code
     */
    public boolean isBothFound() {

        return this.isDerivedQuestFound && this.isQuestionCodeFound;

    }
}
