package uk.gov.ons.validation.util;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.powermock.reflect.Whitebox.setInternalState;

import uk.gov.ons.validation.exception.NoPropertyFoundException;

public class PropertiesUtilTest {

    private static final String ENV_KEY = "ENV_KEY";
    private static final String ENV_VALUE = "ENV_VALUE";

    private static final String UNKNOWN_KEY = "UNKNOWN";

    @Rule
    public ExpectedException expected = ExpectedException.none();

    private Map<String, String> environmentVars = new HashMap<>();

    @Before
    public void init() {
        environmentVars.put(ENV_KEY, ENV_VALUE);
        setInternalState(PropertiesUtil.class, "environmentVars", environmentVars);
    }

    @Test
    public void testGetPropertyEnvironmentVariable_Success() {
        assertThat(PropertiesUtil.getProperty(ENV_KEY), equalTo(ENV_VALUE));
    }

    @Test
    public void testUnknownKey() {
        expected.expect(NoPropertyFoundException.class);
        expected.expectMessage("Unable to find the property UNKNOWN");
        PropertiesUtil.getProperty(UNKNOWN_KEY);
    }
}
