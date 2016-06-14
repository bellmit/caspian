package com.emc.caspian.ccs.policyEngine;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

import java.util.Arrays;

import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.core.SecurityContext;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import com.emc.caspain.ccs.common.webfilters.KeystonePrincipal;
import com.emc.caspian.ccs.common.policyengine.PolicyEngine;
import com.emc.caspian.fabric.config.Configuration;

/**
 * PolicyEngine related tests
 *
 * @author shrids
 *
 */
public class PolicyEngineTest {

    private static PolicyEngine policyEngine;

    @Mock
    ContainerRequestContext context = Mockito.mock(ContainerRequestContext.class);

    @Mock
    SecurityContext secContext;

    @Mock
    KeystonePrincipal userPrincipal;


    @BeforeClass
    public static void beforClass() throws Exception {
        Configuration.load("src/test/resources/registry.conf");

        policyEngine = new PolicyEngine();
    }

    /**
     * @throws java.lang.Exception
     */
    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);

        when(secContext.getUserPrincipal()).thenReturn(userPrincipal);
        when(userPrincipal.getRoles()).thenReturn(Arrays.asList("accounting","operator"));
        when(context.getSecurityContext()).thenReturn(secContext);
    }

    @Test
    public final void testIsValidSimpleRule() {
        assertTrue(policyEngine.isValid("get_image", context));
    }

    @Test
    public final void testIsValidSimpleRule1() {
        assertFalse(policyEngine.isValid("get_images", context));
    }

    @Test
    public final void testIsValidReferenceRule() {
        assertFalse(policyEngine.isValid("delete_image", context));
    }

    @Test
    public final void testIsValidReferenceRule1() {
        assertTrue(policyEngine.isValid("modify_image", context));
    }

    @Test
    public final void testIsValidNestedRule() {
        assertFalse(policyEngine.isValid("add_member", context));
    }
}
