/**
 * Copyright (c) 2015 EMC Corporation
 * All Rights Reserved
 *
 * This software contains the intellectual property of EMC Corporation
 * or is licensed to EMC Corporation from third parties.  Use of this
 * software and the intellectual property contained therein is expressly
 * limited to the terms and conditions of the License Agreement under which
 * it is provided by or on behalf of EMC.
 */
package com.emc.caspian.ccs.common.policyengine;

import com.emc.caspian.fabric.util.Validate;
import org.apache.commons.lang.StringUtils;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.type.TypeReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * This class is holder for all the polices defined in policy.json.
 *
 * @author shrids
 *
 */
public final class PolicyHolder {

    private final Map<String, String> ruleMap;

    public PolicyHolder(Path policyPath) {
        ruleMap = fetchRuleMap(policyPath);
    }

    private final Map<String, String> fetchRuleMap(Path policyPath) {
        try {
            if (Files.exists(policyPath)) { // policy.json file Exists

                final ObjectMapper mapper = new ObjectMapper();
                TypeReference<Map<String, String>> typeRef = new TypeReference<Map<String, String>>() {
                };
                Map<String, String> policies = mapper.readValue(policyPath.toFile(), typeRef);
                // compute expanded rules
                return expandedRuleMap(policies);
            }
            _log.error("Error Policy json file does not exist @ {}", policyPath.toAbsolutePath());
            throw new RuntimeException("Policy.json file does not exist");
        } catch (IOException e) {
            _log.error("Error during parsing of policy file ", e);
            throw new RuntimeException("Policy not configured");
        }
    }

    /**
     * Given the policy name fetch the expanded rule. i.e. fetch the rule by expanding all the inner
     * rules.
     *
     * @param policyName
     * @return
     */
    public String fetchExpandedRule(String policyName) {
        Validate.isNotNullOrEmpty(policyName, "policyName");
        return ruleMap.get(policyName);
    }

    /**
     * This method is used to create an expandedRuleMap.
     * @param ruleMap
     * @return
     */
    private final Map<String, String> expandedRuleMap(Map<String, String> ruleMap) {
        for (Entry<String, String> ruleEntry : ruleMap.entrySet()) {
            String rule = ruleEntry.getValue();
            if (rule.contains("rule:")) {
                ruleEntry.setValue(replaceSubRules(rule, ruleMap));
            }
        }
        return ruleMap;
    }

    /*
     * Helper function to replace sub rules within a given rule.
     */
    private String replaceSubRules(String rule, Map<String, String> ruleMap) {
        Set<String> subRuleNames = fetchSubRules(rule);
        for (String subRuleName : subRuleNames) {
            String subRule = ruleMap.get(subRuleName);
            if (StringUtils.isBlank(subRule)) {
                _log.error("The following rule is not defined. RuleName: {} ", subRuleName);
                throw new IllegalArgumentException("The following policy rule is not defined" + subRuleName);
            }
            rule = rule.replaceAll("rule:" + subRuleName, " ( " + subRule + " ) ");
        }

        if (rule.contains("rule:"))
            rule = replaceSubRules(rule, ruleMap);
        return rule;
    }

    /*
     * Helper function to fetch a Set of subrules in a given rule.
     */
    private Set<String> fetchSubRules(String rule) {
        Set<String> matches = new HashSet<String>(5);
        Matcher m = Pattern.compile("rule:(\\w+)").matcher(rule);
        while (m.find()) {
            matches.add(m.group(1));
        }
        System.out.println(matches.toString());
        return matches;
    }

    private static final Logger _log = LoggerFactory.getLogger(PolicyHolder.class);

}
