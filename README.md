# Generating Training Data for Semantic Role Labeling


This repository contains the code for automated labeling of FrameNet roles in arbitrary text as described in section 4 of our TACL paper.

Please use the following citation:

```
@inproceedings{hartmannEtalTACL2016,
author = {Silvana Hartmann and Judith Eckle-Kohler and Iryna Gurevych},
	title = {Generating Training Data for Semantic Role Labeling based on Label Transfer from Linked Lexical Resources},
	month = may,
	year = {2016},
	journal = {Transactions of the Association for Computational Linguistics},
	number = {1},
	pages = {(to appear)},
	volume = {4}
}
```

> **Abstract:** We present a new approach for generating role-labeled training data using Linked Lexical Resources, i.e., integrated lexical resources that combine several resources (e.g., WordNet, FrameNet, Wiktionary) by linking them on the sense or on the role level. Unlike resource-based supervision in relation extraction, we focus on complex linguistic annotations, more specifically FrameNet senses and roles. The automatically labeled training data (http://www.ukp.tu-darmstadt.de/knowledge-based-srl/) are evaluated on four corpora from different domains for the tasks of word sense disambiguation and semantic role classification. Results show that classifiers trained on our generated data equal those resulting from a standard supervised setting.

Contact person: Silvana Hartmann, hartmann@ukp.informatik.tu-darmstadt.de

http://www.ukp.tu-darmstadt.de/

http://www.tu-darmstadt.de/

Don't hesitate to send us an e-mail or report an issue, if something is broken (and it shouldn't be) or if you have further questions.


> This repository contains experimental software and is published for the sole purpose of giving additional background details on the respective publication. 

## Project structure

 * The package `rulebasedsrl` contains the runnable pipelines (one for English and one for German) which make use of the [DKPro Core](https://dkpro.github.io/dkpro-core/) framework. 
 * To run the pipelines, you need to obtain the English or German Wacky corpus which is already tokenized, POS-tagged and lemmatized. You also need the SemLink resource which is read by `RoleMappingUtil`.
 * The pipelines should be understood as just one particular example of applying the rule-based labeling - when applying it to other corpora, the reader needs to be replaced and components for tokenizing, POS-tagging and lemmatizing need to be added to the pipelines.

## Requirements

* Java 1.7 and higher
* Maven
* tested on 64-bit Linux versions and Windows 7
* recommended: 16 GB RAM
