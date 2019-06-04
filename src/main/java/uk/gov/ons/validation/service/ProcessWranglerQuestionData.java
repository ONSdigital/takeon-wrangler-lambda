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
import uk.gov.ons.validation.entity.ValidationRequestData;
import uk.gov.ons.validation.util.PropertiesUtil;

import java.util.List;

import static java.lang.String.format;

@Log4j2
public class ProcessWranglerQuestionData {

    private static final String SEND_MESSAGE = "Attempting to invoke %s with the json string %s.";
    private static final String QUES_DER_MESSAGE = "Match Found for both Question and Derived Question. " +
            "Question Code is %s Derived Question code is %s Question Code value %s " +
            "and Derived Question code value is %s.";

    /**
     * Process Question data containing Question code and their values from Data Preparation Lambda
     *
     * @param request WranglerRequest
     * @throws Exception
     */
    public void processQuestionAndDerivedData(WranglerRequest request) throws Exception {

        //1. Building Validation Config
        ValidationConfig validationData = new ValidationConfig();
        List<ValidationConfig> configuration = validationData.getValidationConfiguration();

        List<QuestionInputData> responses = request.getResponses();
        log.info("Request Data {} ", responses);

        if (configuration != null && configuration.size() > 0) {
            for (ValidationConfig config : configuration) {
                InvokeConfig invokeConfig = newInvokeConfig();
                for (QuestionInputData inputData : responses) {
                    invokeConfig.processQuestionCode(inputData, config.getQuestionCode());
                    invokeConfig.processDerivedQuestCode(inputData, config.getDerivedQuestionCode());
                    if (invokeConfig.isBothFound()) {
                        log.info(format(QUES_DER_MESSAGE, invokeConfig.getFinalQuestCode(),
                                invokeConfig.getFinalDerivedQuestCode(), invokeConfig.getFinalQuestCodeValue(),
                                invokeConfig.getFinalDerivedQuestValue()));
                        callValidationLambda(invokeConfig);
                        break;
                    }
                }
            }
        }
    }

    /**
     *git add Call Validation Lambda i.e. VET
     *
     * @param config InvokeConfig
     * @throws JsonProcessingException
     */
    private void callValidationLambda(InvokeConfig config) throws JsonProcessingException {
        try {
            log.info("Before Calling Validation Lambda");
            ValidationRequestData dataElement = ValidationRequestData.builder()
                    .primaryValue(config.getFinalQuestCodeValue())
                    .comparisonValue(config.getFinalDerivedQuestValue())
                    .build();
            String requestJson = new ObjectMapper().writeValueAsString(dataElement);
            String wranglerName = PropertiesUtil.getProperty(Constants.WRANGLER_NAME);
            log.info(format(SEND_MESSAGE, wranglerName, requestJson));
            InvokeRequest invokeRequest = newInvokeRequest();
            invokeRequest.withFunctionName(wranglerName).withPayload(requestJson);
            InvokeResult result = buildAWSLambdaClient().invoke(invokeRequest);
            log.info(format("Status after calling Validation Lambda %s", result.getStatusCode()));
        } catch (JsonProcessingException e) {
            log.error("An exception occurred while attempting to prepare " +
                    "and send the request to Validation lambda.", e);
            throw e;
        }
    }

    /**
     * Build AWS Lambda Client
     *
     * @return AWSLambdaAsync
     */
    private AWSLambdaAsync buildAWSLambdaClient() {
        return AWSLambdaAsyncClient.asyncBuilder().withRegion(Regions.EU_WEST_2).build();
    }

    /**
     * Create new InvokeRequest for Lambda
     *
     * @return InvokeRequest
     */
    private InvokeRequest newInvokeRequest() {
        return new InvokeRequest();
    }

    /**
     * Create new Invoke Config to process Question and Derived data
     *
     * @return InvokeConfig
     */
    private InvokeConfig newInvokeConfig() {
        return new InvokeConfig();
    }
}
