package uk.gov.ons.validation.serverless;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import uk.gov.ons.validation.entity.WranglerRequest;
import lombok.extern.log4j.Log4j2;
import uk.gov.ons.validation.service.ProcessWranglerQuestionData;

@Log4j2
public class WranglerLambdaHandler implements RequestHandler<WranglerRequest, String> {

    private static final String SEND_MESSAGE = "Attempting to invoke %s with the json string %s.";

    @Override
    public String handleRequest(WranglerRequest request, Context context) {

        try {
            newProcessQuestionData().processQuestionAndDerivedData(request);
        } catch (Exception e) {
            log.error("An exception was raised handling the Wrangler Lambda request.", e);
            return "Failed";
        }
        return "Accepted";
    }

    private ProcessWranglerQuestionData newProcessQuestionData() {
        return new ProcessWranglerQuestionData();
    }
}
