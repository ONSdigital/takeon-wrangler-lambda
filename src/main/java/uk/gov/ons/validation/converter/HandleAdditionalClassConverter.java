package uk.gov.ons.validation.converter;

import uk.gov.ons.validation.entity.HandlerAdditionalRequest;
import uk.gov.ons.validation.entity.PostRequest;
import uk.gov.ons.validation.entity.WranglerRequest;

public class HandleAdditionalClassConverter {

    private static final String POST_REQUEST = "POST";

    public HandlerAdditionalRequest convertToAdditionalWranglerRequest(PostRequest request) {
        return HandlerAdditionalRequest.builder()
                .input(request)
                .type(POST_REQUEST)
                .build();
    }
}
