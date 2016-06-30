Natural Language Understanding Course Project Report
===
Yiding Tian 115033910027
---

**NOTE:**If you want to view a more readable version, you can refer to [this URL](https://www.zybuluo.com/killa/note/422295).

## Introduction
Shallow discourse parsing is to parse a piece of text into a set of discourse relations within one sentence, or between two sentences. The word "Shallow" means that the relation between two discourse unit is not connected. There are two types of relations: explicit relation and implicit relation.


## Develop Environment
### Operating System
Windows 10 + Ubuntu 16.04
Firstly I used Windows 10, then I found that there are some mistakes exist on windows. So I changed to Ubuntu 16.04 on my VMWare virtual machine.

### Programming Language And Runtime
Java 8/Java SE Development Kit 8u74(jkd_1.8.0_74)

### IDE
Eclipse Mars


## Dataset
### Training Set
Labeled data of *Wall Street Journal* (wsj) offered by **CoNLL Shared Task**. It contains tagged relations from 1756 wsj raw texts distributing from group 02 to group 21. All the data are packaged into two files: `pdtb-parses.json` and `pdtb-data.json` in **JSON** format.

### Testing Set
wsj files from group 22. All the data are given in two formats: raw text and json file including parsed data(pdtb-parses-dev.json). In this project, we only used the raw text.


## Method
In the reference paper offered by this project, I found a project named [pdtb-parser](https://github.com/WING-NUS/pdtb-parser) useful.[2] So I forked his code and do my project on this forked one. The project used a library named [PDTB(Penn Discourse Treebank)](https://www.seas.upenn.edu/~pdtb/)[2].

### Training
During analyzing the training code of `pdtb-parser`, I found that the training data it used is exactly the training data that we need to use. The only difference is the format of files. So we can use the trained model immediately.

### Annotation
I analyzed the code of `pdtb-parser`, understood the usage of his annotation components. Then I implemented my own annotation code on raw text. I implemented the whole flow in package `me.killa.CoNLL` and in class `ShallowDiscourseParser`.

Before annotation, I need to transfer the format of raw text files into what the system needs. And after annotation, I need to change the format of output files into the required format.

The final output file is `data/output.json`.

Here is the core code of my implementation:

``` java
    File rawDataDir = new File("data/raw");

        if(rawDataDir.exists()){
            if(rawDataDir.isDirectory()){
                File[] files = rawDataDir.listFiles();

                log.info("Start to tranfer file format...");

                for(File file : files){
                    if(file.isFile() && !file.getName().endsWith(".txt")){
                        File txtFile = new File(file.getPath() + ".txt");

                        if(!txtFile.exists()){
                            log.info("File \"" + file.getName() + ".txt\" generating...");
                            txtFile.createNewFile();

                            @SuppressWarnings("resource")
                            BufferedReader bfr = new BufferedReader(new FileReader(file));
                            BufferedWriter bfw = new BufferedWriter(new FileWriter(txtFile));
                            String buffer = null;

                            buffer = bfr.readLine();
                            buffer = bfr.readLine();

                            while(buffer != null){
                                buffer = bfr.readLine();
                                if(buffer != null){
                                    bfw.append(buffer);
                                    bfw.newLine();
                                }
                            }

                            bfw.close();
                        }
                        else{
                            log.info("File \"" + file.getName() + ".txt\" exists, skipping...");
                        }
                    }
                }
            }
        }

        File inputFile = new File("data/raw");
        if (inputFile.exists()) {
            if (inputFile.isDirectory()) {
                doBatchParsing(inputFile);
            } else {
                OUTPUT_FOLDER_NAME = inputFile.getParentFile().getAbsolutePath() + "/" + OUTPUT_FOLDER_NAME;
                new File(OUTPUT_FOLDER_NAME).mkdir();
                log.info("Parsing file " + inputFile);
                parseFile(inputFile, true);
            }
        } else {
            log.error("File " + inputFile + " does not exists. ");
        }

        File outputDir = new File("data/raw/output");

        if(outputDir.isDirectory()){
            int ID = 35708;
            File[] files = outputDir.listFiles();
            File outputFile = new File("data/output.json");

            if(outputFile.exists()){
                log.info("Output file exists, skipping...");
            }
            else{
                outputFile.createNewFile();

                BufferedWriter bfw = new BufferedWriter(new FileWriter(outputFile));

                for(File file : files){
                    if(file.isFile() && file.getName().endsWith(".pipe")){
                        log.info("Analyzing file \"" + file.getName() + "\"...");
                        boolean hasImplict = false;
                        @SuppressWarnings("resource")
                        BufferedReader bfr = new BufferedReader(new FileReader(file));
                        String result = bfr.readLine();
                        JSONObject jo = new JSONObject();

                        JSONObject arg1 = new JSONObject();

                        arg1.element("CharacterSpanList", new JSONArray());
                        arg1.element("RawText", "");
                        arg1.element("TokenList", new JSONArray());

                        JSONObject arg2 = new JSONObject();

                        arg2.element("CharacterSpanList", new JSONArray());
                        arg2.element("RawText", "");
                        arg2.element("TokenList", new JSONArray());

                        JSONObject connective = new JSONObject();

                        connective.element("CharacterSpanList", new JSONArray());
                        connective.element("RawText", "");

                        jo.element("Arg1", arg1);
                        jo.element("Arg2", arg2);
                        jo.element("Connective", connective);
                        jo.element("DocID", file.getName().split("\\.")[0]);
                        jo.element("ID", ID);
                        jo.element("Sense", new JSONArray());
                        jo.element("Type", "");

                        while(result != null){
                            String[] results = result.split("\\|");

                            if(results[0].equals("Implicit")){
                                hasImplict = true;
                                jo.element("Type", results[0]);
                                jo.accumulate("Sense", results[11]);

                                String[] arg1CharSpanList = results[22].split(";");

                                for(String span : arg1CharSpanList){
                                    if(!span.isEmpty()){
                                        JSONArray ja = new JSONArray();
                                        String[] spans = span.split("\\.\\.");

                                        ja.add(Integer.parseInt(spans[0]));
                                        ja.add(Integer.parseInt(spans[1]));

                                        ((JSONObject)jo.get("Arg1")).accumulate("CharacterSpanList", ja);
                                    }
                                }

                                ((JSONObject)jo.get("Arg1")).element("RawText", results[24]);


                                String[] arg2CharSpanList = results[32].split(";");

                                log.info(results[32]);
                                for(String span : arg2CharSpanList){
                                    if(!span.isEmpty()){
                                        JSONArray ja = new JSONArray();
                                        String[] spans = span.split("\\.\\.");

                                        ja.add(Integer.parseInt(spans[0]));
                                        ja.add(Integer.parseInt(spans[1]));

                                        ((JSONObject)jo.get("Arg2")).accumulate("CharacterSpanList", ja);
                                    }
                                }

                                ((JSONObject)jo.get("Arg2")).element("RawText", results[34]);

                            }
                            result = bfr.readLine();
                        }

                        if(hasImplict){
                            ID ++;

                            bfw.append(jo.toString());
                            bfw.newLine();
                        }
                    }
                }

                bfw.close();
            }
        }
        else{
            log.error("Output is not a directory!");
        }
```

## Conclusion
In this project, I learned a lot of things about PDTB and built my theory of Natural Language Processing. NLP is a very interesting task, and I wish I could learn more about NLP in the future.

## Reference
[1] Prasad, Rashmi, Eleni Miltsakaki, Nikhil Dinesh, Alan Lee, Aravind Joshi, Livio Robaldo, and Bonnie L. Webber. "The penn discourse treebank 2.0 annotation manual." (2007).
[2] Lin, Ziheng, Hwee Tou Ng, and Min-Yen Kan. "A PDTB-styled end-to-end discourse parser." Natural Language Engineering 20, no. 02 (2014): 151-184.
