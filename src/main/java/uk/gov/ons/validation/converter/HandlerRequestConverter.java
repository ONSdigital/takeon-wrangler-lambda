package uk.gov.ons.validation.converter;

import uk.gov.ons.validation.entity.WranglerRequest;
import uk.gov.ons.validation.entity.KeyMetaData;
import uk.gov.ons.validation.entity.PostRequest;

public class HandlerRequestConverter {

    public PostRequest convertToWranglerRequest(WranglerRequest request) {
        return PostRequest.builder()
                .instance(request.getInstance())
                .survey(request.getSurvey())
                .period(request.getPeriod())
                .reference(request.getReference())
                .build();
    }
}
