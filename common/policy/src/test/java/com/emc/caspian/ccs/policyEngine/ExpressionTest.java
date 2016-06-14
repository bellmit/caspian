package com.emc.caspian.ccs.policyEngine;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

import java.util.Arrays;

import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.core.SecurityContext;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.runners.MockitoJUnitRunner;

import com.emc.caspain.ccs.common.webfilters.KeystonePrincipal;
import com.emc.caspian.ccs.common.policyengine.parser.PolicyExpression;
import com.emc.caspian.ccs.common.policyengine.parser.PolicyTokenizer;

@RunWith(MockitoJUnitRunner.class)
public class ExpressionTest {
    private PolicyExpression expression;

    @Mock
    ContainerRequestContext context = Mockito.mock(ContainerRequestContext.class);

    @Mock
    SecurityContext secContext;

    @Mock
    KeystonePrincipal userPrincipal;

    @Before
    public final void setup() {
        MockitoAnnotations.initMocks(this);

        when(secContext.getUserPrincipal()).thenReturn(userPrincipal);
        when(userPrincipal.getRoles()).thenReturn(Arrays.asList("operator"));
        when(context.getSecurityContext()).thenReturn(secContext);
    }

    @Test
    public final void testEvaluate() {
        PolicyTokenizer tokenizer = new PolicyTokenizer("not role:admin and role:operator");
        expression = new PolicyExpression(tokenizer.parse());
        assertEquals(true, expression.evaluate(context));
    }

    @Test
    public final void testEvaluateWithBraces() {
        PolicyTokenizer tokenizer = new PolicyTokenizer("(not role:admin and (not role:testuser or not role:operator))");
        expression = new PolicyExpression(tokenizer.parse());
        assertEquals(true, expression.evaluate(context));
    }

    @Test
    public final void testEvaluateWithBraces1() {
        PolicyTokenizer tokenizer = new PolicyTokenizer("(not role:admin and (role:testuser or not role:operator))");
        expression = new PolicyExpression(tokenizer.parse());
        assertEquals(false, expression.evaluate(context));
    }

    @Test
    public final void testEvaluateWithBraces2() {
        PolicyTokenizer tokenizer = new PolicyTokenizer("(project_id:%(project_id)s and not(role:testuser or not role:operator))");
        expression = new PolicyExpression(tokenizer.parse());
        assertEquals(true, expression.evaluate(context));
    }

    @Test
    public final void testEvaluateWithBraces3() {
        PolicyTokenizer tokenizer = new PolicyTokenizer("(not role:admin and not(role:testuser or not role:operator))");
        expression = new PolicyExpression(tokenizer.parse());
        assertEquals(true, expression.evaluate(context));
    }

    @Test
    public final void testEvaluateEmptyString() {
        PolicyTokenizer tokenizer = new PolicyTokenizer("");
        expression = new PolicyExpression(tokenizer.parse());
        assertEquals(true, expression.evaluate(null));
    }

    @Test
    public final void testEvaluateEmptySpaces() {
        PolicyTokenizer tokenizer = new PolicyTokenizer("   ");
        expression = new PolicyExpression(tokenizer.parse());
        assertEquals(true, expression.evaluate(null));
    }

    @Test(expected = IllegalArgumentException.class)
    public final void testEvaluateParenthesisMismatch() {
        PolicyTokenizer tokenizer = new PolicyTokenizer("not role:accounting or not role:admin and not( role:operator");
        expression = new PolicyExpression(tokenizer.parse());
        expression.evaluate(context);
    }
}
