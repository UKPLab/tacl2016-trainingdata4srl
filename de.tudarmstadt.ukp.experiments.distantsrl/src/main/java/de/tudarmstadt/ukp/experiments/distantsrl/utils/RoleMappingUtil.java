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
package de.tudarmstadt.ukp.experiments.distantsrl.utils;


import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.XMLEvent;

import org.apache.commons.io.IOUtils;

/**
 * Read role mapping files
 * @author Silvana Hartmann
 *
 */
public class RoleMappingUtil {

    public static void writeSemLinkVNFn(String infile, String outfile){
        HashMap<String, HashMap<String,List<String>>> roleMappingMap = readSemLinkVnFn(infile);
        FileWriter fw = null;
        try {
            fw = new FileWriter(new File(outfile));
            for (String k: roleMappingMap.keySet()){
                String frame = k.split("%")[0];
                String vnc = k.split("%")[1];
                for (String vnrole : roleMappingMap.get(k).keySet()){
                    List<String> fnroles = roleMappingMap.get(k).get(vnrole);
                    for (String fnrole: fnroles){
                        fw.write(frame +"\t"+fnrole + "\t" + vnc +"\t" + vnrole +"\n");
                    }
                }
            }
        }catch (IOException e){
            } finally {
            IOUtils.closeQuietly(fw);
        }
    }
    /**
     * Read FrameNet to Verbnet role mapping from semlink
     * @param filepath
     * @return
     */
    public static HashMap<String, HashMap<String, List<String>>> readSemLinkVnFn(String filepath) {
        // Map vnclass_fnframe to map of arguments 
        
        HashMap<String, HashMap<String,List<String>>> roleMappingMap = new HashMap<String, HashMap<String,List<String>>>();
        FileInputStream is = null;
        try {
            is = new FileInputStream(new File(filepath));
            XMLInputFactory xmlInputFactory = XMLInputFactory.newInstance();
            xmlInputFactory.setProperty(XMLInputFactory.IS_NAMESPACE_AWARE, false);
            XMLEventReader xmlEventReader = xmlInputFactory.createXMLEventReader(is);
            JAXBContext context = JAXBContext.newInstance(SemLinkFnVnRoleMapping.class);
            Unmarshaller unmarshaller = context.createUnmarshaller();
            XMLEvent e = null;
            while ((e = xmlEventReader.peek()) != null){
                SemLinkFnVnRoleMapping a = unmarshaller.unmarshal(xmlEventReader,SemLinkFnVnRoleMapping.class).getValue();
                List<SemLinkVncls> mappings = a.mappings;
                for (SemLinkVncls mapping:mappings){
                    // key is combination of fn frame and vn class label 
                    String key = mapping.fnframe + "%"+ mapping.vnclass;
                    List<SemLinkRole> rolemappings = mapping.rolesList;
                    // value is map of vn role label to fn role label(s)
                    HashMap<String,List<String>> roleMap = new HashMap<String, List<String>>();
                    for (SemLinkRole m : rolemappings){
                        if (!roleMap.containsKey(m.vnrole)){
                            roleMap.put(m.vnrole,new ArrayList<String>());
                        } 
                        roleMap.get(m.vnrole).add(m.fnrole);
                    }
                    roleMappingMap.put(key, roleMap);
                }
                xmlEventReader.next();
            }
        } catch (XMLStreamException e){
            System.err.println("Error parsing xml of: " + filepath);
            e.printStackTrace();
        } catch (JAXBException e){
            System.err.println("Error parsing xml of: " + filepath);
            e.printStackTrace();
        } catch (FileNotFoundException e) {
            System.err.println("File could not be found: " + filepath);
        } finally {
            IOUtils.closeQuietly(is);
        }
        return roleMappingMap;
    }
    
    /**
     * Read FrameNet frame to VerbNet class mapping from semlink
     * @param filepath
     * @return
     */
    public static HashMap<String, List<String>> readSemLinkClassMapping(String filepath) {
        // Map fnframe to vnclass
        
        HashMap<String,List<String>> classMap = new HashMap<String,List<String>>();
        FileInputStream is = null;
        try {
            is = new FileInputStream(new File(filepath));
            XMLInputFactory xmlInputFactory = XMLInputFactory.newInstance();
            xmlInputFactory.setProperty(XMLInputFactory.IS_NAMESPACE_AWARE, false);
            XMLEventReader xmlEventReader = xmlInputFactory.createXMLEventReader(is);
            JAXBContext context = JAXBContext.newInstance(SemLinkFnVnRoleMapping.class);
            Unmarshaller unmarshaller = context.createUnmarshaller();
            XMLEvent e = null;
            while ((e = xmlEventReader.peek()) != null){
                SemLinkFnVnRoleMapping a = unmarshaller.unmarshal(xmlEventReader,SemLinkFnVnRoleMapping.class).getValue();
                List<SemLinkVncls> mappings = a.mappings;
                for (SemLinkVncls mapping:mappings){
                    
                    if (!classMap.containsKey(mapping.fnframe)){
                        classMap.put(mapping.fnframe,new ArrayList<String>());
                        classMap.get(mapping.fnframe).add(mapping.vnclass);
                    } else {
                        classMap.get(mapping.fnframe).add(mapping.vnclass);
                    }
                }
                xmlEventReader.next();
            }
        } catch (XMLStreamException e){
            System.err.println("Error parsing xml of: " + filepath);
            e.printStackTrace();
        } catch (JAXBException e){
            System.err.println("Error parsing xml of: " + filepath);
            e.printStackTrace();
        } catch (FileNotFoundException e) {
            System.err.println("File could not be found: " + filepath);
        } finally {
            IOUtils.closeQuietly(is);
        }
        return classMap;
    }

    
    @XmlRootElement
    static class SemLinkFnVnRoleMapping {

        @XmlAttribute
        public String name;
        
        @XmlElement (name = "vncls")
        public List<SemLinkVncls> mappings;

    }
    
    static class SemLinkVncls {
        @XmlAttribute (name = "class")
        public String vnclass;
        
        @XmlAttribute
        public String fnframe;
        
        // several role instances via role
        @XmlElementWrapper(name="roles")
        @XmlElement(name="role")
        public List<SemLinkRole> rolesList;
    }
    
    static class SemLinkRole {
        @XmlAttribute
        public String fnrole;
        
        @XmlAttribute
        public String vnrole;
    }
        
}
