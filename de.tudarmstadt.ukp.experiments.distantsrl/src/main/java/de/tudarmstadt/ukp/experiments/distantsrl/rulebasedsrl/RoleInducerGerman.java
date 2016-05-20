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
public class RoleInducerGerman {
	
    private HashMap<String, HashMap<String, List<String>>> semlinkMapping;
    private HashMap<String, List<String>> classMapping;

	public RoleInducerGerman(String mappingLoc) {
		semlinkMapping = 
				de.tudarmstadt.ukp.experiments.distantsrl.utils.RoleMappingUtil.readSemLinkVnFn(mappingLoc + "VN-FNRoleMapping.txt");
		System.err.println(semlinkMapping.keySet().size() + " " +semlinkMapping.keySet().iterator().next() + " SMELINKMAPPING");
		
		classMapping = 
				de.tudarmstadt.ukp.experiments.distantsrl.utils.RoleMappingUtil.readSemLinkClassMapping(mappingLoc + "VN-FNRoleMapping.txt");
		System.err.println(classMapping.keySet().size() + " " +classMapping.keySet().iterator().next() + " classMAPPING");

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
		if (depSemField.equals("Ort") || depNeTag.equals("i-loc")) {
			roleLabel = "Location%InitialLocation";
		} else if (depSemField.equals("Zeit")) {
			roleLabel = "Time";
		} else if (depSemField.equals("Menge")) {
			roleLabel = "Value%Asset";
						
// **********************************************************************************
			
		} else if (depType.equals("MI") // instrumental
				) {
			roleLabel = "Instrument";
			
		} else if (depType.equals("MW") // way (directional modifier)
				) {
			roleLabel = "Destination";	
			
			
		} else if (depType.equals("OG")) { // genitive object
			roleLabel = "Theme%Co-Theme%Topic"; 


		} else if (depType.equals("OP") && govSemField.equals("Perzeption") ) { // OP = prepositional object
			roleLabel = "Stimulus";	
			
		} else if (depType.equals("NMC") ) {
			roleLabel = "Value%Asset";	
			
		} else if (depType.equals("ML")) { // locative
			roleLabel = "Location";


		} else if (depType.equals("OP") ) {
			roleLabel = "Theme%Co-Theme%Topic";
			
		} else if (depType.equals("OA2") && (govSemField.equals("Kommunikation") || govSemField.equals("Kognition"))) { // second accusative
			roleLabel = "Attribute%Predicate";

		} else if (depType.equals("SB")) {		// subject	
			if (govSemField.equals("Gefuehl") || govSemField.equals("Perzeption") || govSemField.equals("Kognition")) {
				roleLabel = "Experiencer%Pivot";
			} else {
				roleLabel = "Agent%Co-Agent%Pivot";
			}
			
		} else if (depType.equals("SBP")) { // passivised subject
			roleLabel = "Patient%Co-Patient%Beneficiary"; 	
			
		} else if (depType.equals("DA")) { // dative object
			if (govSemField.equals("Kommunikation")) {
				roleLabel = "Recipient";
			} else {
				roleLabel = "Patient%Co-Patient%Beneficiary"; 
			}
			
		} else if (depType.equals("OA") && govSemField.equals("Veraenderung")) { // OA= accusative object
			roleLabel = "Extent";
		} else if (depType.equals("OA") && govSemField.equals("Besitz")) {
			roleLabel = "Goal";			
		} else if (depType.equals("OA") && govSemField.equals("Kommunikation")) {
			roleLabel = "Predicate";			
			
		} else if (depType.equals("OA")) {
			roleLabel = "Patient%Co-Patient%Beneficiary"; 
		} else if (depType.equals("OA2")) {
			roleLabel = "Theme%Co-Theme%Topic";
			
		} else if (depType.equals("OC")) { // clausal object
			roleLabel = "Theme%Co-Theme%Topic";

		} else if (depType.equals("RS")) { // reported speech
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
				System.err.println("role lookup map" + role);
				// lookup Semlink mapping
				if (classMapping.containsKey(fnFrame)) {
					System.err.println("here frame mapped");
					vnClasses = classMapping.get(fnFrame); System.out.println("fnFrame is mapped to vnClasses: " +vnClasses);
					for (String vnClass : vnClasses) {
						String roleMapKey = fnFrame+"%"+vnClass;
						roleMap = semlinkMapping.get(roleMapKey); System.out.println("roleMap for fnFrame: " +roleMap);
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
