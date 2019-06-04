package uk.gov.ons.validation.serverless;

import com.amazonaws.services.lambda.runtime.Context;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;


import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;
import static org.powermock.api.mockito.PowerMockito.*;

import uk.gov.ons.validation.service.ProcessWranglerQuestionData;
import uk.gov.ons.validation.util.PropertiesUtil;
import uk.gov.ons.validation.entity.WranglerRequest;


@RunWith(PowerMockRunner.class)
@PrepareForTest({WranglerLambdaHandler.class, PropertiesUtil.class})
@PowerMockIgnore("javax.management.*")
public class WranglerLambdaHandlerTest {

    private WranglerLambdaHandler handler = spy(new WranglerLambdaHandler());

    @Mock
    private Context context;
    @Mock
    private ProcessWranglerQuestionData processData;

    @Before
    public void init() throws Exception {
        initMocks(this);
        doReturn(processData).when(handler, "newProcessQuestionData");
        mockStatic(PropertiesUtil.class);
        when(PropertiesUtil.getProperty("WRANGLER_NAME")).thenReturn("wrangler-lambda");
    }

    @Test
    public void testAcceptedResponse() throws Exception {
        doReturn(processData).when(handler, "newProcessQuestionData");
        String result = handler.handleRequest(WranglerRequest.builder().build(), context);
        assertThat(result, equalTo("Accepted"));

    }

    @Test
    public void testFailedResponse() throws Exception {
        doThrow(new Exception()).when(processData).processQuestionAndDerivedData(Mockito.any());
        String result = handler.handleRequest(WranglerRequest.builder().build(), context);
        assertThat(result, equalTo("Failed"));
    }

}
