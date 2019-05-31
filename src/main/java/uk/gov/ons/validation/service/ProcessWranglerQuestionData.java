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

    public void processQuestionAndDerivedData(WranglerRequest request) throws Exception {

        log.info("Parsing Json Array to Java object: ");

        //1. Building Validation Config
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

        System.out.println("Request Data" + responses);

        if (responses != null) {
            for (QuestionInputData response : responses) {
                System.out.println("response" + response);

            }

        }

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
                        System.out.println("Matching object found for Question" + inputData.getQuestionCode());

                        isQuestionCodeFound = true;
                        finalQuestCode = inputData.getQuestionCode();
                        finalQuestCodeValue = inputData.getResponse();
                    }
                    if (inputData.getQuestionCode().equals(config.getDerivedQuestionCode())) {
                        isDerivedQuestFound = true;
                        finalDerivedQuestCode = inputData.getQuestionCode();
                        finalDerivedQuestValue = inputData.getResponse();
                        System.out.println("Matching object found for Derived Question" + config.getDerivedQuestionCode());
                    }
                    if (isQuestionCodeFound && isDerivedQuestFound) {
                        break;
                    }
                }
                if (isQuestionCodeFound && isDerivedQuestFound) {
                    System.out.println("Match Found for both Question and Derived Question");
                    System.out.println("Question Code " + finalQuestCode + "Derived Question Code" + finalDerivedQuestCode);
                    System.out.println("Question Code Value " + finalQuestCodeValue + "Derived Question Code Value" + finalDerivedQuestValue);
                    //Call Larissa Lambda which performs Validation
                    System.out.println("Before Calling Validation Lambda");
                    WranglerResponseData dataElement = WranglerResponseData.builder()
                            .primaryValue(finalQuestCodeValue)
                            .comparisonValue(finalDerivedQuestValue)
                            .build();
                    sendDataToWrangler(dataElement);

                    System.out.println("After Calling Validation Lambda");
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
            AWSLambdaAsync client = AWSLambdaAsyncClient.asyncBuilder().withRegion(Regions.EU_WEST_2).build();



            String requestJson = new ObjectMapper().writeValueAsString(data);
            String wranglerName = PropertiesUtil.getProperty(Constants.WRANGLER_NAME);

            log.info(format(SEND_MESSAGE, wranglerName, requestJson));
            System.out.println("jSon Request" + requestJson);
            System.out.println("Data sending to Lambda"+data);


            InvokeRequest invokeRequest = newInvokeRequest();
            invokeRequest.withFunctionName(wranglerName).withPayload(requestJson);

            InvokeResult result = buildAWSLambdaClient().invoke(invokeRequest);
            log.info("Status after calling Validation Lambda", result.getStatusCode());


        } catch (JsonProcessingException e) {
            log.error("An exception occurred while attempting to prepare and send the request to validation lambda.", e);
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
