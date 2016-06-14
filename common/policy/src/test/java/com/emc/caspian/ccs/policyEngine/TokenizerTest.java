package com.emc.caspian.ccs.policyEngine;

import com.emc.caspian.ccs.common.policyengine.parser.FunctionToken;
import com.emc.caspian.ccs.common.policyengine.parser.Functions;
import com.emc.caspian.ccs.common.policyengine.parser.PolicyTokenizer;
import com.emc.caspian.ccs.common.policyengine.parser.Token;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.assertEquals;

public class TokenizerTest {

    private PolicyTokenizer tokenizer;

    @Test
    public final void testParse() {
        tokenizer = new PolicyTokenizer("not role:accounting and not role:operator");
        List<Token> tokens = tokenizer.parse();
        assertEquals(1, tokens.get(0).getType());
        assertEquals(2, tokens.get(1).getType());
        FunctionToken funcToken = (FunctionToken) tokens.get(1);
        assertEquals("role", funcToken.getFunction().getSymbol());
        assertEquals(1, tokens.get(2).getType());
        assertEquals(1, tokens.get(3).getType());
        assertEquals(2, tokens.get(4).getType());
    }

    @Test
    public final void testParseWithBraces() {
        tokenizer = new PolicyTokenizer("(not role:accounting and (not role:testuser) or not role:operator)");
        List<Token> tokens = tokenizer.parse();
        assertEquals(Token.OPEN_BRACE_TOKEN, tokens.get(0).getType());
        assertEquals(Token.OPERATOR_TOKEN, tokens.get(1).getType());
        FunctionToken funcToken = (FunctionToken) tokens.get(2);
        assertEquals("role", funcToken.getFunction().getSymbol());
        assertEquals(Token.FUNCTION_TOKEN, tokens.get(2).getType());
        assertEquals(Token.OPERATOR_TOKEN, tokens.get(3).getType());
        assertEquals(Token.OPEN_BRACE_TOKEN, tokens.get(4).getType());
        assertEquals(Token.CLOSE_BRACE_TOKEN, tokens.get(11).getType());
    }

    @Test
    public final void testParseWithProjectID() {
        tokenizer = new PolicyTokenizer("(not role:accounting and not( project_id:%(project_id)s or not role:operator))");
        List<Token> tokens = tokenizer.parse();
        assertEquals(Token.FUNCTION_TOKEN, tokens.get(6).getType());
        assertEquals("project_id", ((FunctionToken)tokens.get(6)).getFunction().getSymbol());

    }

    @Test
    public final void testParseWithERROR() {
        tokenizer = new PolicyTokenizer("(not testRole:role and not( project_id:%(project_id)s or not role:operator))");
        List<Token> tokens = tokenizer.parse();
        assertEquals(Token.FUNCTION_TOKEN, tokens.get(0).getType());
        assertEquals(Functions.POLICY_FUNCTION_ALL, ((FunctionToken)tokens.get(0)).getFunction().getSymbol());

    }

}
