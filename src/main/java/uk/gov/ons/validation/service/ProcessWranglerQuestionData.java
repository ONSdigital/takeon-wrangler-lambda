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
import uk.gov.ons.validation.converter.HandleAdditionalClassConverter;
import uk.gov.ons.validation.converter.HandlerRequestConverter;
import uk.gov.ons.validation.entity.*;
import lombok.extern.log4j.Log4j2;
import uk.gov.ons.validation.util.PropertiesUtil;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

import static java.lang.String.format;

@Log4j2
public class ProcessWranglerQuestionData {

    private static final String SEND_MESSAGE = "Attempting to invoke %s with the json string %s.";
    private static final String QUES_DER_MESSAGE = "Match Found for both Question and Derived Question. " +
            "Question Code is %s Derived Question code is %s Question Code value %s " +
            "and Derived Question code value is %s.";
    private static final String VALIDATION_NAME = "QvDQ";

    private static final String INITIAL_DATA_INGESTION_INFO = "Survey is %s for the period %s, " +
            " Reference %s and instanceId %s.";

    private static final String publish_message = "Publishing message to topic: %s";

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
        log.info(format(INITIAL_DATA_INGESTION_INFO, request.getSurvey(),
                request.getPeriod(), request.getReference(),
                request.getInstance()));


        List<ValidationResult> validationResults = new ArrayList<>();
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
                        validationResults.add(invokeConfig.getWranglerData());
                        break;
                    }
                }
            }
        }

        PostRequest additionalRequest = new HandlerRequestConverter().convertToWranglerRequest(request);
        additionalRequest.setValidationResults(validationResults);
        additionalRequest.setValidationName(VALIDATION_NAME);
        HandlerAdditionalRequest finalRequest = new HandleAdditionalClassConverter().convertToAdditionalWranglerRequest(additionalRequest);
        callPersistenceLambda(finalRequest);
        // BPM (Business Process Management) response goes here. This method puts
        sendBpmResponse("B123-P456-M789", "QvsDQ", PropertiesUtil.getProperty(Constants.TOPIC_ARN));
    }

    /**
     * git add Call Validation Lambda i.e. VET
     *
     * @param config InvokeConfig
     * @throws JsonProcessingException
     */
    private void callValidationLambda(InvokeConfig config) throws IOException {
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
            String rawJson = retrieveRawJson(result);
            ObjectMapper mapper = new ObjectMapper();
            ValidationResult validationResult = mapper.readValue(rawJson, ValidationResult.class);
            validationResult.setQuestionCode(config.getFinalQuestCode());
            config.setWranglerData(validationResult);
        } catch (JsonProcessingException e) {
            log.error("An exception occurred while attempting to prepare " +
                    "and send the request to Validation lambda.", e);
            throw e;
        } catch (UnsupportedEncodingException ex) {
            log.error("An exception occurred while attempting to prepare " +
                    "and send the request to Validation lambda.", ex);
        }
    }

    /**
     * Retrieves Json from PayLoad
     *
     * @param result InvokeResult
     * @return String rawJson
     * @throws UnsupportedEncodingException
     */
    private String retrieveRawJson(InvokeResult result) throws UnsupportedEncodingException {

        String rawJson = null;
        if (result != null) {
            ByteBuffer byteBuffer = result.getPayload();
            rawJson = new String(byteBuffer.array(), "UTF-8");
            log.info(format("PayLoad String is %s", rawJson));
        }
        return rawJson;
    }

    /**
     * git add Call Validation Lambda i.e. VET
     *
     * @param dataElement HandlerAdditionalRequest
     * @throws IOException
     */
    private void callPersistenceLambda(HandlerAdditionalRequest dataElement) throws IOException {
        try {
            log.info("Before calling  Response Persistence Lambda");
            String requestJson = new ObjectMapper().writeValueAsString(dataElement);
            log.info("Final request JSON {}", requestJson);
            String wranglerName = PropertiesUtil.getProperty(Constants.WRANGLER_NAME_RESPONSE_PERSISTENCE);
            InvokeRequest invokeRequest = newInvokeRequest();
            invokeRequest.withFunctionName(wranglerName).withPayload(requestJson);
            InvokeResult result = buildAWSLambdaClient().invoke(invokeRequest);
            log.info(format("Status after calling Response Persistence Lambda %s", result.getStatusCode()));
            String rawJson = retrieveRawJson(result);
            log.info(format("Raw Json after calling Response Persistence Lambda %s", rawJson));
        } catch (JsonProcessingException e) {
            log.error("An exception occurred while attempting to process JSON " +
                    "and send the request to Validation lambda.", e);
            throw e;
        } catch (UnsupportedEncodingException ex) {
            log.error("An exception occurred while attempting to encode, prepare " +
                    "and send the request to Validation lambda.", ex);
            throw ex;
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

    private String constructBpmResponse(String bpmInstance, String validationName) throws JsonProcessingException {
        MessageResponse message = MessageResponse.builder().bpmInstance(bpmInstance).validationName(validationName).build();
        return new ObjectMapper().writeValueAsString(message);
    }

    private void sendBpmResponse(String bpmInstance, String validationName, String topicArn) throws Exception {
        String messageToSend;
        try {
            messageToSend = constructBpmResponse(bpmInstance, validationName);
        } catch (JsonProcessingException e) {
            log.error("An exception occurred while attempting to process Bpm Response message", e);
            throw e;
        }
        try {
            log.info(format(publish_message, topicArn));
            PublishRequest publishRequest = new PublishRequest(topicArn, messageToSend);
            AmazonSNSClientBuilder.defaultClient().publish(publishRequest);
        } catch (Exception e) {
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
