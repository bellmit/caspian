/**
 *  Copyright (c) 2014 EMC Corporation
 * All Rights Reserved
 *
 * This software contains the intellectual property of EMC Corporation
 * or is licensed to EMC Corporation from third parties.  Use of this
 * software and the intellectual property contained therein is expressly
 * limited to the terms and conditions of the License Agreement under which
 * it is provided by or on behalf of EMC.
 */
package com.emc.caspian.ccs.common.policyengine.policy;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Specified the policy to be used to validate the Authorization. The value of policy is the name/s
 * of the policy to be used from the policy.json file. This can be applied to a rest resource method.
 * E.g:  @Policy("add_image")
 * @Policy({"get_image_location", "get_image"})
 *
 * @author shrids
 *TODO: this will be moved to a common package.
 */
@Documented
@Retention(RUNTIME)
@Target(METHOD)
public @interface Policy {
    String[] value();
}
