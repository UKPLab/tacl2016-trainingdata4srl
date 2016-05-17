/*******************************************************************************
 * Copyright 2016
 * Ubiquitous Knowledge Processing (UKP) Lab
 * Technische Universität Darmstadt
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
package de.tudarmstadt.ukp.experiments.distantsrl.rulebasedsrl;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;

/**
 * @author Judith Eckle-Kohler
 *
 */
public class RoleInducer {
	
    private HashMap<String, HashMap<String, List<String>>> semlinkMapping;
    private HashMap<String, List<String>> classMapping;

	public RoleInducer(String mappingLoc) {
		semlinkMapping = 
				de.tudarmstadt.ukp.experiments.distantsrl.utils.RoleMappingUtil.readSemLinkVnFn(mappingLoc + "VN-FNRoleMapping.txt");
		
		classMapping = 
				de.tudarmstadt.ukp.experiments.distantsrl.utils.RoleMappingUtil.readSemLinkClassMapping(mappingLoc + "VN-FNRoleMapping.txt");
	}
	
	public String labelDependent(String depType, String dependentPos, 
			String depSemField, String depNeTag, String govSemField) {
		// predicate info has the form:
		// 
		// SEMANTIC ROLE : Speaker%say.v%751%Statement
		// semlink mapping has the form:
		// fnframe%vnclass -> (vnrole -> listOfFnroles)
		
		String roleLabel = null;
		
		// semantic fields of arguments:
		if (depSemField.equals("location") || depNeTag.equals("location")) {
			roleLabel = "Location%InitialLocation";
		} else if (depSemField.equals("time")) {
			roleLabel = "Time";
		} else if (depSemField.equals("quantity")) {
			roleLabel = "Value%Asset";
						
// **********************************************************************************
			
		} else if (depType.equals("prep_with") 
				&& !depSemField.equals("person") && !depSemField.equals("group")
				&& !depNeTag.equals("person") && !depNeTag.equals("group")) {
			roleLabel = "Instrument";
			
		} else if (depType.equals("prep_to") 
				&& !depSemField.equals("person") && !depSemField.equals("group")
				&& !depNeTag.equals("person") && !depNeTag.equals("group")) {
			roleLabel = "Destination";			
		} else if (depType.equals("prep_to")) {
			roleLabel = "Patient%Co-Patient%Beneficiary"; 
			
		} else if (depType.equals("prep_against")) {
			roleLabel = "Recipient";

		} else if (depType.equals("prep_after")) {
			roleLabel = "Time";
		} else if (depType.equals("prepc_after")) {
			roleLabel = "Time";

		} else if (depType.equals("prep_before") && dependentPos.equals("CARD")) {
			roleLabel = "Time";			
		} else if (depType.equals("prepc_before")) {
			roleLabel = "Time";

		} else if (depType.equals("prep_at") && govSemField.equals("perception") ) {
			roleLabel = "Stimulus";	
		} else if (depType.equals("prep_at") && govSemField.equals("possession") ) {
			roleLabel = "Value%Asset";	
		} else if (depType.equals("prep_at") && dependentPos.equals("CARD") ) {
			roleLabel = "Value%Asset";	
			
		} else if (depType.equals("prep_at")) {
			roleLabel = "Location";

		} else if (depType.equals("prep_under") && !govSemField.equals("motion") && !govSemField.equals("stative")) {
			roleLabel = "Theme%Co-Theme%Topic";
			
		} else if (depType.equals("prep_as") && (govSemField.equals("communication") || govSemField.equals("cognition"))) {
			roleLabel = "Attribute%Predicate";

		} else if (depType.equals("prep_for") 
				&& !depSemField.equals("person") && !depSemField.equals("group")
				&& !depNeTag.equals("person") && !depNeTag.equals("group")) {
			roleLabel = "Cause";
		} else if (depType.equals("prep_for")) {
			roleLabel = "Patient%Co-Patient%Beneficiary"; 
			
		} else if (depType.equals("prep_on") && govSemField.equals("body")) {
			roleLabel = "Location";
		} else if (depType.equals("prep_on") && govSemField.equals("contact")) {
			roleLabel = "Destination";
		} else if (depType.equals("prep_on") && govSemField.equals("communication")) {
			roleLabel = "Time";
		} else if (depType.equals("prep_on") && !govSemField.equals("motion") && !govSemField.equals("stative")) {
			roleLabel = "Theme%Co-Theme%Topic";		
		} else if (depType.equals("prep_on")) {
			roleLabel = "Location";
			
			
		} else if (depType.equals("prep_from") && govSemField.equals("motion")) {
			roleLabel = "Source";

		} else if (depType.equals("prep_away_from")) {
			roleLabel = "Source";

		} else if (depType.equals("prep_through") && govSemField.equals("motion")) {
			roleLabel = "Location";

		} else if (depType.equals("prep_below") && govSemField.equals("motion")) {
			roleLabel = "Destination";
			
		} else if (depType.equals("prep_into") && govSemField.equals("motion")) {
			roleLabel = "Destination";
		} else if (depType.equals("prep_into")) {
			roleLabel = "Location";

		} else if (depType.equals("prepc_on") && govSemField.equals("cognition")) {
			roleLabel = "Theme%Co-Theme%Topic";

		} else if (depType.equals("prep_over") && govSemField.equals("communication")) {
			roleLabel = "Theme%Co-Theme%Topic";

		} else if (depType.equals("prep_in") && dependentPos.equals("NN") && !govSemField.equals("motion")) {
			roleLabel = "Theme%Co-Theme%Topic";
		} else if (depType.equals("prep_in") && !govSemField.equals("motion") && !govSemField.equals("stative")) {
			roleLabel = "Theme%Co-Theme%Topic";		
		} else if (depType.equals("prep_in") && dependentPos.equals("CARD")) {
			roleLabel = "Time";
		} else if (depType.equals("prep_in") && dependentPos.equals("NP")) {
			roleLabel = "Location";
		} else if (depType.equals("prep_in")) {
			roleLabel = "Location";
			
		} else if (depType.equals("prep_prior_to") && dependentPos.equals("CARD")) {
			roleLabel = "Time";
		} else if (depType.equals("prep_during")) {
			roleLabel = "Time";
			
		} else if (depType.equals("prep_of") && (govSemField.equals("communication") || govSemField.equals("cognition"))) {
			roleLabel = "Theme%Co-Theme%Topic";
		} else if (depType.equals("prepc_of")) {
			roleLabel = "Cause";

		} else if (depType.equals("prep_over") && govSemField.equals("motion")) {
			roleLabel = "Location";

		} else if (depType.equals("nsubj")) {			
			if (govSemField.equals("emotion") || govSemField.equals("perception") || govSemField.equals("cognition")) {
				roleLabel = "Experiencer%Pivot";
			} else {
				roleLabel = "Agent%Co-Agent%Pivot";
			}
		} else if (depType.equals("agent")) {
			roleLabel = "Agent%Co-Agent%Pivot";
		
			
		} else if (depType.equals("nsubjpass")) {
			roleLabel = "Patient%Co-Patient%Beneficiary"; 	
		} else if (depType.equals("csubj")) {
			roleLabel = "Theme%Co-Theme%Topic";									
		} else if (depType.equals("iobj")) {
			if (govSemField.equals("communication")) {
				roleLabel = "Recipient";
			} else {
				roleLabel = "Patient%Co-Patient%Beneficiary"; 
			}
			
		} else if (depType.equals("dobj") && (govSemField.equals("motion") || govSemField.equals("change"))) {
			roleLabel = "Extent";
		} else if (depType.equals("dobj") && govSemField.equals("possession")) {
			roleLabel = "Goal";			
		} else if (depType.equals("dobj") && govSemField.equals("communication")) {
			roleLabel = "Predicate";			
			
		} else if (depType.equals("dobj")) {
			roleLabel = "Patient%Co-Patient%Beneficiary"; 
		} else if (depType.equals("obj")) {
			roleLabel = "Theme%Co-Theme%Topic";
			
		} else if (depType.equals("ccomp")) {
			roleLabel = "Theme%Co-Theme%Topic";
		} else if (depType.equals("xcomp")) {
			roleLabel = "Theme%Co-Theme%Topic";
		} else if (depType.equals("csubj")) {
			roleLabel = "Theme%Co-Theme%Topic";
		} else {
			// this covers different cases:
			// - the dependency type is not relevant for argument classification: 
			// dep, mark, vmod, advcl, tmod, advmod, conj_and, prep, discourse, parataxis
			// - the dependency type is relevant, but there is no rule yet that covers it, e.g. prep_x
			roleLabel = "noRoleLabel";
		}		
		return roleLabel;		
	}

	public String vn2fnLabel(String roleLabel, String fnFrame) {
		String predictedLabel = roleLabel;
		HashMap<String, List<String>> roleMap = new HashMap<String, List<String>>();
		HashSet<String> roleSet = new HashSet<String>();
		List<String> vnClasses = new LinkedList<String>();

		if (!roleLabel.equals("noRoleLabel")) {
			String[] vnRoles = roleLabel.split("%");
			for (String role : vnRoles) {
				// lookup Semlink mapping
				if (classMapping.containsKey(fnFrame)) {
					vnClasses = classMapping.get(fnFrame); //System.out.println("fnFrame is mapped to vnClasses: " +vnClasses);
					for (String vnClass : vnClasses) {
						String roleMapKey = fnFrame+"%"+vnClass;
						roleMap = semlinkMapping.get(roleMapKey); //System.out.println("roleMap for fnFrame: " +roleMap);
						if (roleMap.containsKey(role)) {
							roleSet.addAll(roleMap.get(role));
						}
					}
				}
				if (!roleSet.isEmpty()) { // there is a Semlink mapping for this frame / role combination
					predictedLabel = roleLabel+"%%" +roleSet.toString();
				} else {
					predictedLabel = roleLabel+"%%noSemlinkEntry";
				}
			}
		} 
		return predictedLabel;
	}




}
