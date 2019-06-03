package uk.gov.ons.validation.service;

import com.amazonaws.regions.Regions;
import com.amazonaws.services.lambda.AWSLambdaAsync;
import com.amazonaws.services.lambda.AWSLambdaAsyncClient;
import com.amazonaws.services.lambda.model.InvokeRequest;
import com.amazonaws.services.lambda.model.InvokeResult;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import uk.gov.ons.validation.common.Constants;
import uk.gov.ons.validation.entity.QuestionInputData;
import uk.gov.ons.validation.entity.ValidationConfig;
import lombok.extern.log4j.Log4j2;
import uk.gov.ons.validation.entity.WranglerRequest;
import uk.gov.ons.validation.entity.WranglerResponseData;
import uk.gov.ons.validation.util.PropertiesUtil;

import java.util.ArrayList;
import java.util.List;

import static java.lang.String.format;

@Log4j2
public class ProcessWranglerQuestionData {

    private static final String SEND_MESSAGE = "Attempting to invoke %s with the json string %s.";
    private static final String QUES_DER_CODE_MESSAGE = "Match Question Code %s and Derived Question code is %s.";
    private static final String QUES_DER_VALUE_MESSAGE = "Match Question Code value %s and Derived Question code value is %s.";
    private static final String MATCH_FOUND_QUEST = "Matching object found for Question and its code is %s.";
    private static final String MATCH_FOUND_DERIVED_QUEST = "Matching object found for Derived Question and its code is %s.";


    public void processQuestionAndDerivedData(WranglerRequest request) throws Exception {

        log.info("Parsing Json Array to Java object: ");

        //1. Building Validation Config this is separate feature. So hard coding it
        List<ValidationConfig> configuration = new ArrayList<>();
        ValidationConfig config1 = new ValidationConfig();
        config1.setQuestionCode("601");
        config1.setDerivedQuestionCode("700");
        configuration.add(config1);
        ValidationConfig config2 = new ValidationConfig();
        config2.setQuestionCode("602");
        config2.setDerivedQuestionCode("701");
        configuration.add(config2);

        List<QuestionInputData> responses = request.getResponses();

        log.info("Request Data {} " , responses);


        String finalQuestCode = null;
        String finalDerivedQuestCode = null;
        String finalQuestCodeValue = null;
        String finalDerivedQuestValue = null;



        if (configuration != null && configuration.size() > 0) {
            for (ValidationConfig config : configuration) {

                boolean isQuestionCodeFound = false;
                boolean isDerivedQuestFound = false;
                for (QuestionInputData inputData : responses) {


                    if (inputData.getQuestionCode().equals(config.getQuestionCode())) {



                        isQuestionCodeFound = true;
                        finalQuestCode = inputData.getQuestionCode();
                        finalQuestCodeValue = inputData.getResponse();
                        log.info(format(MATCH_FOUND_QUEST, finalQuestCode));
                    }
                    if (inputData.getQuestionCode().equals(config.getDerivedQuestionCode())) {
                        isDerivedQuestFound = true;
                        finalDerivedQuestCode = inputData.getQuestionCode();
                        finalDerivedQuestValue = inputData.getResponse();
                        log.info(format(MATCH_FOUND_DERIVED_QUEST, finalDerivedQuestCode));

                    }
                    if (isQuestionCodeFound && isDerivedQuestFound) {
                        break;
                    }
                }
                if (isQuestionCodeFound && isDerivedQuestFound) {
                    log.info("Match Found for both Question and Derived Question");


                    log.info(format(QUES_DER_CODE_MESSAGE, finalQuestCode, finalDerivedQuestCode));
                    log.info(format(QUES_DER_VALUE_MESSAGE, finalQuestCodeValue, finalDerivedQuestValue));
                    //Call External Lambda which performs Validation
                    log.info("Before Calling Validation Lambda");
                    WranglerResponseData dataElement = WranglerResponseData.builder()
                            .primaryValue(finalQuestCodeValue)
                            .comparisonValue(finalDerivedQuestValue)
                            .build();
                    sendDataToWrangler(dataElement);

                    log.info("After Calling Validation Lambda");
                    finalQuestCode = null;
                    finalDerivedQuestCode = null;
                    finalQuestCodeValue = null;
                    finalDerivedQuestValue = null;

                }

            }

        }

    }

    private void sendDataToWrangler(WranglerResponseData data) throws JsonProcessingException {
        try {


            String requestJson = new ObjectMapper().writeValueAsString(data);
            String wranglerName = PropertiesUtil.getProperty(Constants.WRANGLER_NAME);

            log.info(format(SEND_MESSAGE, wranglerName, requestJson));

            InvokeRequest invokeRequest = newInvokeRequest();
            invokeRequest.withFunctionName(wranglerName).withPayload(requestJson);

            InvokeResult result = buildAWSLambdaClient().invoke(invokeRequest);
            log.info(format("Status after calling Validation Lambda %s", result.getStatusCode()));



        } catch (JsonProcessingException e) {
            log.error("An exception occurred while attempting to prepare and send the request to Validation lambda.", e);
            throw e;
        }
    }

    private AWSLambdaAsync buildAWSLambdaClient() {
        return AWSLambdaAsyncClient.asyncBuilder().withRegion(Regions.EU_WEST_2).build();
    }

    private InvokeRequest newInvokeRequest() {
        return new InvokeRequest();
    }
}
