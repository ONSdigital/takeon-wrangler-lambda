package uk.gov.ons.validation.serverless;
import com.amazonaws.services.lambda.AWSLambdaAsync;
import com.amazonaws.services.lambda.model.InvokeRequest;
import com.amazonaws.services.lambda.runtime.Context;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;


import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;
import static org.powermock.api.mockito.PowerMockito.*;
import uk.gov.ons.validation.util.PropertiesUtil;
import uk.gov.ons.validation.entity.WranglerRequest;


@RunWith(PowerMockRunner.class)
@PrepareForTest({WranglerLambdaHandler.class, PropertiesUtil.class})
@PowerMockIgnore("javax.management.*")
public class WranglerLambdaHandlerTest {

    private static final String EXPECTED_JSON = "{\"responses\":[{\"questionCode\":\"code\"," +
            "\"response\":\"response\"}]}";
    private WranglerLambdaHandler handler = spy(new WranglerLambdaHandler());
    @Mock
    private Context context;

    @Mock
    private AWSLambdaAsync client;

    @Mock
    private InvokeRequest invokeRequest;

    @Captor
    private ArgumentCaptor<String> jsonString;

    @Before
    public void init() throws Exception {
        initMocks(this);

        doReturn(client).when(handler, "buildAWSLambdaClient");
        when(invokeRequest.withFunctionName(anyString())).thenReturn(invokeRequest);
        doReturn(invokeRequest).when(handler, "newInvokeRequest");

        mockStatic(PropertiesUtil.class);
        when(PropertiesUtil.getProperty("WRANGLER_NAME")).thenReturn("wrangler-lambda");
    }

    @Ignore
    @Test
    public void testAcceptedResponse() {

        String result = handler.handleRequest(WranglerRequest.builder().build(), context);
        Mockito.verify(invokeRequest).withPayload(jsonString.capture());
        assertThat(result, equalTo("Accepted"));
        assertThat(jsonString.getValue(), equalTo(EXPECTED_JSON));
    }


    @Ignore
    @Test
    public void testFailedResponse() throws Exception {
        when(handler.handleRequest(WranglerRequest.builder().build(), context)).thenThrow(new Exception());
        String result = handler.handleRequest(WranglerRequest.builder().build(), null);

        assertThat(result, equalTo("Failed"));
    }

}
