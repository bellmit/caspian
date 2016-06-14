package com.emc.caspian.ccs.datastore;

import com.emc.caspian.ccs.datastore.expressiontree.BinaryExpression;
import com.emc.caspian.ccs.datastore.expressiontree.ConstantExpression;
import com.emc.caspian.ccs.datastore.expressiontree.ParameterNameExpression;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class FilterValuesTest {

    /**
     * @throws java.lang.Exception
     */
    @Before
    public void setUp() throws Exception {
    }

    /**
     * Test method for
     * {@link com.emc.caspian.ccs.datastore.expressiontree)}
     * .
     */
    @Test
    public final void testBuild() {
        BinaryExpression exp1 = BinaryExpression.equalTo(new ParameterNameExpression("key1"),
                                                            new ConstantExpression("value11"));
        BinaryExpression exp2 = BinaryExpression.lessThan(new ParameterNameExpression("key2"),
                                                          new ConstantExpression("value12"));
        BinaryExpression exp3 = BinaryExpression.greaterThan(new ParameterNameExpression("key3"),
                                                             new ConstantExpression("value13"));

        BinaryExpression expression = BinaryExpression.or(BinaryExpression.and(exp1, exp2), exp3);
        assertEquals("( ( ( key1  =  'value11' )  AND  ( key2  <  'value12' ) )  OR  ( key3  >  'value13' ) )", expression.toString());
    }

}
