package uk.gov.ons.validation.service;

import com.amazonaws.regions.Regions;
import com.amazonaws.services.lambda.AWSLambdaAsync;
import com.amazonaws.services.lambda.AWSLambdaAsyncClient;
import com.amazonaws.services.lambda.model.InvokeRequest;
import com.amazonaws.services.lambda.model.InvokeResult;
import com.amazonaws.services.sns.AmazonSNSClientBuilder;
import com.amazonaws.services.sns.model.PublishRequest;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import uk.gov.ons.validation.common.Constants;
import uk.gov.ons.validation.entity.MessageResponse;
import uk.gov.ons.validation.entity.QuestionInputData;
import uk.gov.ons.validation.entity.ValidationConfig;
import lombok.extern.log4j.Log4j2;
import uk.gov.ons.validation.entity.WranglerRequest;
import uk.gov.ons.validation.entity.WranglerResponseData;
import uk.gov.ons.validation.util.PropertiesUtil;

import java.util.List;

import static java.lang.String.format;

@Log4j2
public class ProcessWranglerQuestionData {

    private static final String SEND_MESSAGE = "Attempting to invoke %s with the json string %s.";
    private static final String QUES_DER_MESSAGE = "Match Found for both Question and Derived Question. " +
            "Question Code is %s Derived Question code is %s Question Code value %s " +
            "and Derived Question code value is %s.";
    private static final String publish_message =  "Publishing message to topic: %s";

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
                    invokeConfig.processQuestionCodeFound(inputData, config.getQuestionCode());
                    invokeConfig.processDerivedQuestFound(inputData, config.getDerivedQuestionCode());
                    if (invokeConfig.isQuestionCodeFound() && invokeConfig.isDerivedQuestFound()) {
                        log.info(format(QUES_DER_MESSAGE, invokeConfig.getFinalQuestCode(),
                                invokeConfig.getFinalDerivedQuestCode(), invokeConfig.getFinalQuestCodeValue(),
                                invokeConfig.getFinalDerivedQuestValue()));
                        //Call External Lambda which performs Validation
                        callValidationLambda(invokeConfig);
                        break;
                    }
                }
            }
        }
        // BPM (Business Process Management) response goes here. This method puts 
        sendBpmResponse("B123-P456-M789", "QvsDQ", PropertiesUtil.getProperty(Constants.TOPIC_ARN));
    }

    /**
     * Call Validation Lambda i.e. VET
     *
     * @param config InvokeConfig
     * @throws JsonProcessingException
     */
    private void callValidationLambda(InvokeConfig config) throws JsonProcessingException {
        try {
            //Call External Lambda which performs Validation
            log.info("Before Calling Validation Lambda");
            WranglerResponseData dataElement = WranglerResponseData.builder()
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

    private String constructBpmResponse(String bpmInstance, String validationName) throws JsonProcessingException{
        MessageResponse message = MessageResponse.builder().bpmInstance(bpmInstance).validationName(validationName).build();
        return new ObjectMapper().writeValueAsString(message);
    }

    private void sendBpmResponse(String bpmInstance, String validationName, String topicArn) throws Exception{
        String messageToSend;
        try{
            messageToSend = constructBpmResponse(bpmInstance, validationName);
        }
        catch(JsonProcessingException e){
            log.error("An exception occured while attempting to process Bpm Response message", e);
            throw e;
        }

        try{
            log.info(format(publish_message, topicArn));
            PublishRequest publishRequest = new PublishRequest(topicArn, messageToSend);
            AmazonSNSClientBuilder.defaultClient().publish(publishRequest);
        }
        catch(Exception  e){
            log.error("An exception occured during a publish request", e);
        }

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
