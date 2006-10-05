/****************************************************************
 * Licensed to the Apache Software Foundation (ASF) under one   *
 * or more contributor license agreements.  See the NOTICE file *
 * distributed with this work for additional information        *
 * regarding copyright ownership.  The ASF licenses this file   *
 * to you under the Apache License, Version 2.0 (the            *
 * "License"); you may not use this file except in compliance   *
 * with the License.  You may obtain a copy of the License at   *
 *                                                              *
 *   http://www.apache.org/licenses/LICENSE-2.0                 *
 *                                                              *
 * Unless required by applicable law or agreed to in writing,   *
 * software distributed under the License is distributed on an  *
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY       *
 * KIND, either express or implied.  See the License for the    *
 * specific language governing permissions and limitations      *
 * under the License.                                           *
 ****************************************************************/

package org.apache.james.jspf;

import org.apache.james.jspf.core.Logger;
import org.apache.james.jspf.core.SPF1Constants;
import org.apache.james.jspf.core.SPF1Data;
import org.apache.james.jspf.core.SPF1Record;
import org.apache.james.jspf.core.SPFChecker;
import org.apache.james.jspf.exceptions.NeutralException;
import org.apache.james.jspf.exceptions.NoneException;
import org.apache.james.jspf.exceptions.PermErrorException;
import org.apache.james.jspf.exceptions.TempErrorException;
import org.apache.james.jspf.macro.MacroExpand;
import org.apache.james.jspf.policies.AbstractNestedPolicy;

final class DefaultExplanationPolicy extends AbstractNestedPolicy {
    /**
     * 
     */
    private Logger log;
    private String defExplanation;

    /**
     * @param spf
     */
    DefaultExplanationPolicy(Logger log, String explanation) {
        this.log = log;
        this.defExplanation = explanation;
    }

    protected SPF1Record getSPFRecordPostFilter(String currentDomain, SPF1Record spfRecord) throws PermErrorException, TempErrorException, NoneException, NeutralException {
        // Default explanation policy
        spfRecord.getModifiers().add(new SPFChecker() {
            public void checkSPF(SPF1Data spfData) throws PermErrorException, NoneException, TempErrorException, NeutralException {
                
                if (SPF1Constants.FAIL.equals(spfData.getCurrentResult())) {  
                    if (spfData.getExplanation()==null || spfData.getExplanation().equals("")) {
                        String explanation;
                        if (defExplanation == null) {
                            explanation = SPF1Utils.DEFAULT_EXPLANATION;
                        } else {
                            explanation = defExplanation;
                        }
                        try {
                            spfData.setExplanation(new MacroExpand(spfData, log)
                                    .expandExplanation(SPF1Utils.DEFAULT_EXPLANATION));
                        } catch (PermErrorException e) {
                            // Should never happen !
                            log.debug("Invalid defaulfExplanation: " + explanation);
                        }
                    }
                }
            }
            
            public String toString() {
                if (defExplanation == null) {
                    return "defaultExplanation";
                } else {
                    return "defaultExplanation="+defExplanation;
                }
            }
        });
        return spfRecord;
    }
}