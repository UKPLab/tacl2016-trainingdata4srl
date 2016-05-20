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
import org.apache.uima.fit.pipeline.SimplePipeline;
import org.apache.uima.resource.ExternalResourceDescription;

import de.tudarmstadt.ukp.dkpro.core.api.io.ResourceCollectionReaderBase;
import de.tudarmstadt.ukp.dkpro.core.api.resources.CompressionMethod;
import de.tudarmstadt.ukp.dkpro.core.api.resources.ResourceObjectProviderBase;
import de.tudarmstadt.ukp.dkpro.core.io.imscwb.ImsCwbReader;
import de.tudarmstadt.ukp.dkpro.core.io.xmi.XmiReader;
import de.tudarmstadt.ukp.dkpro.core.io.xmi.XmiWriter;
import de.tudarmstadt.ukp.dkpro.core.stanfordnlp.StanfordNamedEntityRecognizer;
import de.tudarmstadt.ukp.dkpro.core.stanfordnlp.StanfordParser;
import de.tudarmstadt.ukp.dkpro.core.tokit.GermanSeparatedParticleAnnotator;
import de.tudarmstadt.ukp.uby.resource.UbyResource;
import de.tudarmstadt.ukp.uby.resource.UbySemanticFieldResource;
import de.tudarmstadt.ukp.uby.uima.annotator.UbySemanticFieldAnnotator;


/**
 * @author Judith Eckle-Kohler, Silvana Hartmann
 *
 */
public class FrameNetSrlPipelineGerman {
	

	public static final String DB_URL =  "jdbc:h2:file:embeddedUby/ubydeu070"; 
	public static final String DB_DRIVER = "org.h2.Driver";
	public static final String DB_DRIVER_NAME = "h2";
	public static final String DB_USERNAME = "sa";
	public static final String DB_PASSWORD = "";

    public static String semLinkLocation= "/home/user/Data/SemLink";

    public static String sourceLocationBase= "/home/user/Data/sdewac/";

    public static String outputFileBase= "/home/user/srl_xmis/compressed_sdewac/";


	public static void main(String[] args)
		    throws UIMAException, IOException
		{
		
//       CollectionReaderDescription reader = createReaderDescription(
//                ImsCwbReader.class,
//                ImsCwbReader.PARAM_ENCODING, "ISO-8859-1",
//                ResourceCollectionReaderBase.PARAM_SOURCE_LOCATION,     new File(sourceLocationBase).getAbsolutePath(),
//                ResourceCollectionReaderBase.PARAM_PATTERNS, new String[] { "[+]*.xml" },
//                ResourceCollectionReaderBase.PARAM_LANGUAGE, "de"
//				);
       
	    // this assumes that the following preprocessing has been performed:
	    // tokenizing, sentence splitting, POS-tagging, lemmatization, sense tagging with FrameNet senses
       CollectionReaderDescription reader = createReaderDescription(
               XmiReader.class,
               ResourceCollectionReaderBase.PARAM_SOURCE_LOCATION, sourceLocationBase,
               ResourceCollectionReaderBase.PARAM_PATTERNS, new String [] {"*.xmi.bz2"}
               );

		
       AnalysisEngineDescription separatedParticleAnnotator = createEngineDescription(GermanSeparatedParticleAnnotator.class);
	        
       AnalysisEngineDescription ner = createEngineDescription(StanfordNamedEntityRecognizer.class, 
	                StanfordNamedEntityRecognizer.PARAM_LANGUAGE, "de");
      		
       ExternalResourceDescription ubyResource = createExternalResourceDescription(
	                UbyResource.class,
	                UbyResource.PARAM_MODEL_LOCATION, ResourceObjectProviderBase.NOT_REQUIRED,
	                UbyResource.PARAM_URL, DB_URL,
	                UbyResource.PARAM_DRIVER, DB_DRIVER,
	                UbyResource.PARAM_DIALECT, DB_DRIVER_NAME,
	                UbyResource.PARAM_USERNAME, DB_USERNAME,
	                UbyResource.PARAM_PASSWORD, DB_PASSWORD);
	        
       AnalysisEngineDescription semanticFieldAnnotator = 
	    	        createEngineDescription(UbySemanticFieldAnnotator.class,
	    	                UbySemanticFieldAnnotator.PARAM_UBY_SEMANTIC_FIELD_RESOURCE,    	           
	    	                createExternalResourceDescription(UbySemanticFieldResource.class,
	    	                		UbySemanticFieldResource.PARAM_LANGUAGE, "de",
	    	                		UbySemanticFieldResource.RES_UBY, ubyResource));

	        
       AnalysisEngineDescription parser = createEngineDescription(StanfordParser.class,
               StanfordParser.PARAM_LANGUAGE, "de",
               StanfordParser.PARAM_READ_POS, true,
               StanfordParser.PARAM_WRITE_POS, false,
               StanfordParser.PARAM_WRITE_CONSTITUENT, true
               );

	
		    AnalysisEngineDescription semanticRoleAnnotator = 
    		createEngineDescription(FrameNetRoleAnnotatorGerman.class,
    				FrameNetRoleAnnotatorGerman.PARAM_OUTPUT_FILE, outputFileBase+"/srlLogFile.log",
    				FrameNetRoleAnnotatorGerman.PARAM_SEMLINK_LOCATION,semLinkLocation);
	        
		    AnalysisEngineDescription srlwriter = createEngineDescription(XmiWriter.class,
            		XmiWriter.PARAM_TARGET_LOCATION, outputFileBase+"/xmi_rl_input/", 
            		XmiWriter.PARAM_USE_DOCUMENT_ID,true,
            		XmiWriter.PARAM_ESCAPE_DOCUMENT_ID,false,
            		XmiWriter.PARAM_COMPRESSION, CompressionMethod.BZIP2
            		);
	                                  
	        SimplePipeline.runPipeline(reader, 
	        		separatedParticleAnnotator, 
	        		semanticFieldAnnotator,
	        		ner, 
	        		parser,
	        		semanticRoleAnnotator,
	        		srlwriter
	                );

	}
}
