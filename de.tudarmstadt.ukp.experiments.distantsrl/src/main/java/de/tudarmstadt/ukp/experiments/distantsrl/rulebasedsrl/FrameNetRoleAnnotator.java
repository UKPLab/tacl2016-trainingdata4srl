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

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashSet;
import java.util.List;

import org.apache.uima.UimaContext;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.fit.component.JCasAnnotator_ImplBase;
import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.apache.uima.fit.descriptor.TypeCapability;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.cas.FSArray;
import org.apache.uima.resource.ResourceInitializationException;
import org.apache.uima.util.Level;

import de.tudarmstadt.ukp.dkpro.core.api.ner.type.NamedEntity;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Sentence;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token;
import de.tudarmstadt.ukp.dkpro.core.api.semantics.type.SemanticArgument;
import de.tudarmstadt.ukp.dkpro.core.api.semantics.type.SemanticField;
import de.tudarmstadt.ukp.dkpro.core.api.semantics.type.SemanticPredicate;
import de.tudarmstadt.ukp.dkpro.core.api.syntax.type.constituent.Constituent;
import de.tudarmstadt.ukp.dkpro.core.api.syntax.type.dependency.Dependency;
import de.tudarmstadt.ukp.dkpro.wsd.type.Sense;
import de.tudarmstadt.ukp.dkpro.wsd.type.WSDItem;


/**
 * @author Judith Eckle-Kohler
 *
 */
@TypeCapability(
		inputs = {"de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token",
		        "de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Sentence",
				"de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Lemma",
				"de.tudarmstadt.ukp.dkpro.core.lexmorph.type.POS",
				"de.tudarmstadt.ukp.dkpro.core.api.ner.type.NamedEntity;",
				"de.tudarmstadt.ukp.dkpro.core.semantics.type.SemanticField",
				"de.tudarmstadt.ukp.dkpro.core.api.syntax.type.constituent.Constituent",
				"de.tudarmstadt.ukp.dkpro.core.api.syntax.type.dependency.Dependency",
				"de.tudarmstadt.ukp.dkpro.wsd.type.WSDItem", // the targets annotated with the FrameNet sense
				"de.tudarmstadt.ukp.dkpro.wsd.type.Sense"}, // the FrameNet sense
		outputs = {"de.tudarmstadt.ukp.dkpro.core.api.semantics.type.SemanticPredicate",
				"de.tudarmstadt.ukp.dkpro.core.api.semantics.type.SemanticArgument"})


public class FrameNetRoleAnnotator extends JCasAnnotator_ImplBase
{
	
    public static final String PARAM_EVAL_MODE = "evalMode";
    @ConfigurationParameter(name = PARAM_EVAL_MODE, mandatory=true, description="")
    protected boolean evalMode;
    
    public static final String PARAM_OUTPUT_FILE = "File";
    @ConfigurationParameter(name = PARAM_OUTPUT_FILE, mandatory=true, description="Output path, where result file should be stored")
    protected File outputFile;

    public static final String PARAM_SEMLINK_LOCATION = "mappingLoc";
    @ConfigurationParameter(name = PARAM_SEMLINK_LOCATION, mandatory=true, description="Location of SemLink mappings")
    protected String mappingLoc;
    
    private Integer allSemArgs = 0; 
    // all semantic arguments that belong to a verbal predicate, regardless of SemLink mapping
    
    private Integer correctlyPredictedRoles = 0; 
    // predictedRoles that are correct, i.e. there is at least one FN role in the roleset retrieved from Semlink that matches the predicted role
    
    private Integer predictedRoles = 0; 
    // the number of predicted roles for which a SemLink mapping exists, i.e. only those role/frame combinations that can be evaluated
    
    private HashSet<String> setOfPredictedRoles = new HashSet<String>();


    private RoleInducer roleInducer;
    private BufferedWriter dataWriter;	// File writer for predicted and gold VerbNet roles

    
	@Override
	public void initialize(UimaContext context) throws ResourceInitializationException {
		super.initialize(context);
		try {
			dataWriter = new BufferedWriter(new FileWriter(outputFile));
		} catch (IOException e) {
			e.printStackTrace();
		}

		roleInducer = new RoleInducer(mappingLoc);
		getLogger().log(Level.INFO, "Read full Semlink Mapping");
		getLogger().log(Level.INFO, "Read mapping of FN frame to VN classes from Semlink");		
	}


	@Override
	public void process(JCas jcas) throws AnalysisEngineProcessException {
		for (Sentence sentence : JCasUtil.select(jcas, Sentence.class)) {
			System.out.println("\n" +"sentence text: " +sentence.getCoveredText());
			HashSet<String> setOfRolesInSentence = new HashSet<String>();
			
			List<Dependency> deps = JCasUtil.selectCovered(jcas, Dependency.class, sentence);	
			getLogger().log(Level.INFO, "no of dependencies: " +deps.size());
			for (int i = 0; i < deps.size(); i++) {
				Dependency dep = deps.get(i);	
				
				String depNeTag = null;
				String target = null;
				String fnFrame = null;				
				Token dependent = dep.getDependent();
				Token governor = dep.getGovernor();	
				
				if (governor.getPos().getType().getShortName().equals("V")) {
					
				String dependentPos = dependent.getPos().getType().getShortName();
				//System.out.println("dependentPos: " +dependentPos);				
				List<NamedEntity> neTags = JCasUtil.selectCovering(NamedEntity.class, dependent);
				//System.out.println("no of ne tags: " +neTags.size());
				if (!neTags.isEmpty()) {
					depNeTag = neTags.get(0).getValue().toLowerCase();
					if (depNeTag.equals("organization")) {
						depNeTag = "group";
					}
				} else {
					depNeTag = "notANamedEntity";
				}
				List<SemanticField> depSemanticFields = JCasUtil.selectCovered(jcas, SemanticField.class, dependent.getBegin(), dependent.getEnd());	
				String depSemField = depSemanticFields.get(0).getValue();
				List<SemanticField> govSemanticFields = JCasUtil.selectCovered(jcas, SemanticField.class, governor.getBegin(), governor.getEnd());	
				String govSemField = govSemanticFields.get(0).getValue();
				
				if (evalMode) {
					List<SemanticArgument> semArgs = JCasUtil.selectCovering(jcas, SemanticArgument.class, dependent.getBegin(), dependent.getEnd());
					if (!semArgs.isEmpty()) {
						for (int j = 0; j < semArgs.size(); j++) {
							String[] roleString = semArgs.get(j).getRole().split("%");
							if (roleString[1].matches(".*\\.v")) {
								setOfRolesInSentence.add(semArgs.get(j).getRole()); // count only roles that belong to verbal predicates
							}
						}						
						String semRoleInfo = semArgs.get(0).getRole();	//System.out.println("semRoleInfo: " +semRoleInfo);					
						String[] roleInfos = semRoleInfo.split("%");
						String goldRoleFn = roleInfos[0];
						target = roleInfos[1].replaceAll("\\.v", "");
						System.out.println("target lemma: " +target);
						fnFrame = roleInfos[3];						
						System.out.println("fnFrame: " +fnFrame);
						if (governor.getLemma().getValue().equals(target)) {
							String predictedRoleLabel = roleInducer.labelDependent(dep.getDependencyType(), dependentPos, depSemField, depNeTag,
									govSemField); 
							
							HashSet<String> setOfVnRoles = new HashSet<String>();
							String[] vnRoles = predictedRoleLabel.split("%");
							for (String role : vnRoles) {
								setOfVnRoles.add(role);
								setOfPredictedRoles.add(role);
							}
							System.out.println("predictedRoleLabel: " +predictedRoleLabel);
							String roleLabelMapped = roleInducer.vn2fnLabel(predictedRoleLabel, fnFrame); // e.g. Agent%Pivot -> "Agent%Pivot%%" +roleSet.toString();
							
							if (!predictedRoleLabel.equals("noRoleLabel")) {	
								String[] roleMapping = roleLabelMapped.split("%%");
								String fnRoleLabel = roleMapping[1];							
								if (!fnRoleLabel.contains("noSemlinkEntry")) {	
									predictedRoles++;
					
									if (fnRoleLabel.contains(goldRoleFn)) {
										correctlyPredictedRoles++;
									} 
	
									System.out.println("dependency: " +dep.getDependencyType()
											+"\t governor:" +governor.getLemma().getValue() +"/" +govSemField
											+"\t dependent:" +dependent.getCoveredText() +"/"+dependentPos +"/" +depSemField +"/" +depNeTag					
											+"\t role:"  + predictedRoleLabel +"/" +fnRoleLabel
											+"\t gold:" +goldRoleFn
											+"\t goldSpan:" +semArgs.get(0).getCoveredText()
											);
									try {
										dataWriter.write("dependency:" +dep.getDependencyType()
												+"\t governor:" +governor.getLemma().getValue() +"/" +govSemField
												+"\t dependent:" +dependent.getCoveredText() +"/"+dependentPos +"/" +depSemField +"/" +depNeTag										
												+"\t role:"  + predictedRoleLabel +"/" +fnRoleLabel
												+"\t gold:" +goldRoleFn
												+"\t goldSpan:" +semArgs.get(0).getCoveredText()
												+"\n");
										dataWriter.flush();
									} catch (IOException e) {
										e.printStackTrace();
									}
								}
							}

						}	else {
							System.out.println("governor and target do not match \t" +governor.getCoveredText() +" " +target);
						}
					} else {
						System.out.println("semArgs is empty");
					}										
				} else { // prediction mode
					try {
						/*
						 * WSDItem
   sofa: _InitialView
   begin: 77
   end: 85
   subjectOfDisambiguation: "achieve"
   constituents: FSArray
   id: "achieve#2870"
   pos: "VERB"
[achieved]
Sense
   sofa: _InitialView
   begin: 77
   end: 85
   id: "achieve#2870%%achieve%Accomplishment%12306"
   confidence: 0.0
   description: <null>

						 */
		    		List<WSDItem> wsdItems = JCasUtil.selectCovered(jcas, WSDItem.class, sentence);
		    		// Assumption: Each sentence has only one target verb with sense annotation
		    		if (wsdItems.get(0).getSubjectOfDisambiguation().trim().contains(" ")) { // this is a phrasal verb!
						String[] targetString = wsdItems.get(0).getSubjectOfDisambiguation().trim().split(" ");
						target = targetString[0];
		    		} else {
		    			target = wsdItems.get(0).getSubjectOfDisambiguation().trim();
					}
					System.out.println("target: " +target);
					
					List<Sense> senseList = JCasUtil.selectCovered(jcas, Sense.class, sentence);
					//String fnSenseInfo = wsdResults.get(0).getSenses(0).getId();
					String fnSenseInfo = senseList.get(0).getId();
					String[] allInfos = fnSenseInfo.split("%%");
					String[] senseInfos = allInfos[1].split("%");
					fnFrame = senseInfos[1].trim();
					// format: id: "achieve#2870%%achieve%Accomplishment%12306"
					// OLD format: launch#1655_Key.xml~~launch~Shoot_projectiles~5667
					// OLD format of fnSenseInfo: want~Desiring~6412
					System.out.println("fnSenseInfo = senseList.get(0).getId(): " +fnSenseInfo);
					System.out.println("fnFrame: " +fnFrame);
					if (governor.getLemma().getValue().equals(target)) {
						String predictedRoleLabel = roleInducer.labelDependent(dep.getDependencyType(), dependentPos, depSemField, depNeTag,
								govSemField);
						
						HashSet<String> setOfVnRoles = new HashSet<String>();
						String[] vnRoles = predictedRoleLabel.split("%");
						for (String role : vnRoles) {
							setOfVnRoles.add(role);
							setOfPredictedRoles.add(role);
						}
						String roleLabelMapped = roleInducer.vn2fnLabel(predictedRoleLabel, fnFrame); // e.g. Agent%Pivot -> "Agent%Pivot%%" +roleSet.toString();
						
						String[] roleMapping = roleLabelMapped.split("%%");
						String fnRoleLabel = roleMapping[1];
						
						if (!predictedRoleLabel.equals("noRoleLabel")) {							
							List<Constituent> constituents = JCasUtil.selectCovering(jcas, Constituent.class, dependent.getBegin(), dependent.getEnd());
							if (!constituents.isEmpty()) {
								//predictedRoles++;
								Constituent constit = constituents.get(constituents.size()-1);
								String phraseType = constituents.get(constituents.size()-1).getConstituentType(); System.out.println("phraseType: " +phraseType);
								SemanticArgument semArg = new SemanticArgument(jcas,constit.getBegin(),constit.getEnd());
								semArg.setRole(fnRoleLabel); System.out.println("predicted FN roleLabel: " +fnRoleLabel);
								semArg.addToIndexes();
								
								SemanticPredicate semPred = new SemanticPredicate(jcas,senseList.get(0).getBegin(),senseList.get(0).getEnd());
								semPred.setCategory(fnFrame);
								// alternative:
								// semPred.setCategory(senseList.get(0).getId());
								
								FSArray argumentsFSArray = new FSArray(jcas,1);
								argumentsFSArray.set(0,semArg); // FSArray index starts with 0
								
								semPred.setArguments(argumentsFSArray);
								semPred.addToIndexes();

							}
							System.out.println("dependency: " +dep.getDependencyType()
									+"\t governor:" +governor.getLemma().getValue() +"/" +govSemField
									+"\t dependent:" +dependent.getCoveredText() +"/"+dependentPos +"/" +depSemField +"/" +depNeTag					
									+"\t role:"  + predictedRoleLabel +"/" +fnRoleLabel
									);
							try {
								dataWriter.write("dependency:" +dep.getDependencyType()
										+"\t governor:" +governor.getLemma().getValue() +"/" +govSemField
										+"\t dependent:" +dependent.getCoveredText() +"/"+dependentPos +"/" +depSemField +"/" +depNeTag										
										+"\t role:"  + predictedRoleLabel +"/" +fnRoleLabel
										+"\n");
								dataWriter.flush();
							} catch (IOException e) {
								e.printStackTrace();
							}
						}
					}	
					} catch (IndexOutOfBoundsException e1) {
						System.out.println("sth wrong with annotation of WSDItem, WSDResult or Constituent");
					}	catch (NullPointerException e2) {
						System.out.println("sth wrong with annotation of WSDItem, WSDResult or Constituent");	
					}
				}
				} // if governor equals V
			}	
			allSemArgs = allSemArgs + setOfRolesInSentence.size();
			System.out.println("allSemArgs now: " +allSemArgs);
			System.out.println("semantic roles in sentence: " +setOfRolesInSentence.toString());
		}		
	}
	
	
	@Override
	public void collectionProcessComplete() throws AnalysisEngineProcessException {
		
		if (evalMode) {
			Long precision = ((long)correctlyPredictedRoles/(long)predictedRoles);
		
			System.out.println("Correctly predicted roles:\t" +correctlyPredictedRoles +"\t precision \t" +precision);

			System.out.println("Total Number of gold semantic arguments that belong to a verbal predicate:\t" +allSemArgs);
		}
		System.out.println("Total Number of automatically labeled arguments:\t" + predictedRoles);
		System.out.println("set of predicted roles:\t" + setOfPredictedRoles.toString());
		
		// ****************
		 
		try {
			if (evalMode) {		
				Long precision = ((long)correctlyPredictedRoles/(long)predictedRoles);
				dataWriter.write("Correctly predicted roles:\t" +correctlyPredictedRoles +"\t precision \t" +precision);
				dataWriter.write("Total Number of gold semantic arguments that belong to a verbal predicate:\t" +allSemArgs +"\n");
			}
			dataWriter.write("Total Number of automatically labeled arguments:\t" + predictedRoles +"\n");
			dataWriter.write("set of predicted roles:\t" + setOfPredictedRoles.toString() +"\n");
			dataWriter.flush();
		} catch (IOException e) {
			e.printStackTrace();
		}


	}

	
	/*
	 * 
	 * 00 	adj.all 	all adjective clusters
01 	adj.pert 	relational adjectives (pertainyms)
02 	adv.all 	all adverbs
03 	noun.Tops 	unique beginner for nouns
04 	noun.act 	nouns denoting acts or actions
05 	noun.animal 	nouns denoting animals
06 	noun.artifact 	nouns denoting man-made objects
07 	noun.attribute 	nouns denoting attributes of people and objects
08 	noun.body 	nouns denoting body parts
09 	noun.cognition 	nouns denoting cognitive processes and contents
10 	noun.communication 	nouns denoting communicative processes and contents
11 	noun.event 	nouns denoting natural events
12 	noun.feeling 	nouns denoting feelings and emotions
13 	noun.food 	nouns denoting foods and drinks
14 	noun.group 	nouns denoting groupings of people or objects
15 	noun.location 	nouns denoting spatial position
16 	noun.motive 	nouns denoting goals
17 	noun.object 	nouns denoting natural objects (not man-made)
18 	noun.person 	nouns denoting people
19 	noun.phenomenon 	nouns denoting natural phenomena
20 	noun.plant 	nouns denoting plants
21 	noun.possession 	nouns denoting possession and transfer of possession
22 	noun.process 	nouns denoting natural processes
23 	noun.quantity 	nouns denoting quantities and units of measure
24 	noun.relation 	nouns denoting relations between people or things or ideas
25 	noun.shape 	nouns denoting two and three dimensional shapes
26 	noun.state 	nouns denoting stable states of affairs
27 	noun.substance 	nouns denoting substances
28 	noun.time 	nouns denoting time and temporal relations
29 	verb.body 	verbs of grooming, dressing and bodily care
30 	verb.change 	verbs of size, temperature change, intensifying, etc.
31 	verb.cognition 	verbs of thinking, judging, analyzing, doubting
32 	verb.communication 	verbs of telling, asking, ordering, singing
33 	verb.competition 	verbs of fighting, athletic activities
34 	verb.consumption 	verbs of eating and drinking
35 	verb.contact 	verbs of touching, hitting, tying, digging
36 	verb.creation 	verbs of sewing, baking, painting, performing
37 	verb.emotion 	verbs of feeling
38 	verb.motion 	verbs of walking, flying, swimming
39 	verb.perception 	verbs of seeing, hearing, feeling
40 	verb.possession 	verbs of buying, selling, owning
41 	verb.social 	verbs of political and social activities and events
42 	verb.stative 	verbs of being, having, spatial relations
43 	verb.weather 	verbs of raining, snowing, thawing, thundering
44 	adj.ppl 	participial adjectives 
	 */


}
