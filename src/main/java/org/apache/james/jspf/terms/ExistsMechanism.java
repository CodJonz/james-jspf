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


package org.apache.james.jspf.terms;

import org.apache.james.jspf.core.SPF1Data;
import org.apache.james.jspf.exceptions.PermErrorException;
import org.apache.james.jspf.exceptions.TempErrorException;
import org.apache.james.jspf.macro.MacroExpand;
import org.apache.james.jspf.parser.SPF1Parser;

import java.util.List;

/**
 * This class represent the exists mechanism
 * 
 */
public class ExistsMechanism extends GenericMechanism {

    /**
     * ABNF: exists = "exists" ":" domain-spec
     */
    public static final String REGEX = "[eE][xX][iI][sS][tT][sS]" + "\\:"
            + SPF1Parser.DOMAIN_SPEC_REGEX;

    /**
     * 
     * @see org.apache.james.jspf.core.GenericMechanism#run(org.apache.james.jspf.core.SPF1Data)
     */
    public boolean run(SPF1Data spfData) throws PermErrorException,
            TempErrorException {
        List aRecords;

        // update currentDepth
        spfData.setCurrentDepth(spfData.getCurrentDepth() + 1);

        String host = expandHost(spfData);
        try {
            host = new MacroExpand(spfData, log).expandDomain(host);
        } catch (Exception e) {
            throw new PermErrorException(e.getMessage());
        }

        try {
            aRecords = spfData.getDnsProbe().getARecords(host);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
       
        if (aRecords != null) {
            // check for maximum lookup limit       
            if (aRecords.size() > spfData.getMaxDepth()) {
                throw new PermErrorException("Maximum Records checked");
            } else if (aRecords.size() > 0) {
                return true;
            }
        }

        // No match found
        return false;
    }

}
