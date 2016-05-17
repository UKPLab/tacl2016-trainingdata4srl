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

import static org.apache.uima.fit.factory.AnalysisEngineFactory.createEngineDescription;
import static org.apache.uima.fit.factory.CollectionReaderFactory.createReaderDescription;
import static org.apache.uima.fit.factory.ExternalResourceFactory.createExternalResourceDescription;

import java.io.File;
import java.io.IOException;

import org.apache.uima.UIMAException;
import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.collection.CollectionReaderDescription;
import org.apache.uima.fit.component.CasDumpWriter;
import org.apache.uima.fit.pipeline.SimplePipeline;

import de.tudarmstadt.ukp.dkpro.core.api.io.ResourceCollectionReaderBase;
import de.tudarmstadt.ukp.dkpro.core.api.resources.CompressionMethod;
import de.tudarmstadt.ukp.dkpro.core.api.resources.ResourceObjectProviderBase;
import de.tudarmstadt.ukp.dkpro.core.io.imscwb.ImsCwbReader;
import de.tudarmstadt.ukp.dkpro.core.io.xmi.XmiWriter;
import de.tudarmstadt.ukp.dkpro.core.stanfordnlp.StanfordNamedEntityRecognizer;
import de.tudarmstadt.ukp.dkpro.core.stanfordnlp.StanfordParser;
import de.tudarmstadt.ukp.uby.resource.UbyResource;
import de.tudarmstadt.ukp.uby.resource.UbySemanticFieldResource;
import de.tudarmstadt.ukp.uby.uima.annotator.UbySemanticFieldAnnotator;


/**
 * @author Judith Eckle-Kohler
 * 
 * pipeline for the rule-based annotation of FrameNet semantic roles given
 * pre-annotated data with POS, lemma, parsing and WSD annotations
 *
 */
public class FrameNetSrlPipeline {
	
    public static String semLinkLocation= "/home/user/Data/SemLink";

    public static String sourceLocationBase= "/home/user/Data/wacky_ukwac_1_0/wacky19/";

	public static String outputFileBase= "/home/user/srl_xmis/compressed_wacky01/";

	public static void main(String[] args)
		    throws UIMAException, IOException
		{
						
			CollectionReaderDescription reader = createReaderDescription(
                    ImsCwbReader.class,
                    ResourceCollectionReaderBase.PARAM_SOURCE_LOCATION,     new File(sourceLocationBase).getAbsolutePath(),
                    ResourceCollectionReaderBase.PARAM_PATTERNS, new String[] { "[+]UKWAC*.xml" },
                    ResourceCollectionReaderBase.PARAM_LANGUAGE, "en",
                    ImsCwbReader.PARAM_POS_MAPPING_LOCATION, "src/main/resources/en-tagger.map"
					);
			
			AnalysisEngineDescription ner = createEngineDescription(StanfordNamedEntityRecognizer.class);

		    AnalysisEngineDescription semanticFieldAnnotator = 
	    	        createEngineDescription(UbySemanticFieldAnnotator.class,
	    	                UbySemanticFieldAnnotator.PARAM_UBY_SEMANTIC_FIELD_RESOURCE,
	    	                createExternalResourceDescription(UbySemanticFieldResource.class,
	    	                		UbySemanticFieldResource.PARAM_LANGUAGE, "en",
	    	                		UbySemanticFieldResource.RES_UBY,	
	    	                		createExternalResourceDescription(UbyResource.class,
	    	                		UbyResource.PARAM_MODEL_LOCATION, ResourceObjectProviderBase.NOT_REQUIRED,
	    	                		UbyResource.PARAM_URL, "localhost/uby_medium_0_7_0",
	    	                		UbyResource.PARAM_DRIVER, "com.mysql.jdbc.Driver",
	    	                		UbyResource.PARAM_DIALECT, "mysql",
	    	                		UbyResource.PARAM_USERNAME, "root",
	    	                		UbyResource.PARAM_PASSWORD, "pass"			    	                        
	    	                        )));

            AnalysisEngineDescription parser = createEngineDescription(StanfordParser.class,
                    StanfordParser.PARAM_VARIANT, "wsj-rnn",
                    StanfordParser.PARAM_LANGUAGE, "en",
                    StanfordParser.PARAM_READ_POS, true,
                    StanfordParser.PARAM_WRITE_POS, false
                    );

		    AnalysisEngineDescription semanticRoleAnnotator = 
		    		createEngineDescription(FrameNetRoleAnnotator.class,
		    				FrameNetRoleAnnotator.PARAM_EVAL_MODE, false,
		    				FrameNetRoleAnnotator.PARAM_OUTPUT_FILE, outputFileBase+"/srlLogFile.log",
		    				FrameNetRoleAnnotator.PARAM_SEMLINK_LOCATION,semLinkLocation);			    
		    
		    AnalysisEngineDescription srlwriter = createEngineDescription(XmiWriter.class,
            		XmiWriter.PARAM_TARGET_LOCATION, outputFileBase+"/xmi_rl_input/",
            		XmiWriter.PARAM_USE_DOCUMENT_ID,true,
            		XmiWriter.PARAM_ESCAPE_DOCUMENT_ID,false,
            		XmiWriter.PARAM_COMPRESSION ,CompressionMethod.BZIP2);
		    
//			    AnalysisEngineDescription dumpWriter = createEngineDescription(
//                        CasDumpWriter.class,
//                        CasDumpWriter.PARAM_OUTPUT_FILE, "target/roleAnnotationOutput");			    
						
			SimplePipeline.runPipeline(reader, 
					ner,
					parser,
					semanticFieldAnnotator, 
					semanticRoleAnnotator,
					srlwriter
					//dumpWriter
					);
		}
											

}
