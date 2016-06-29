package me.killa.nlp.CoNLL2016;

import static sg.edu.nus.comp.pdtb.util.Settings.OUTPUT_FOLDER_NAME;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import sg.edu.nus.comp.pdtb.parser.ArgExtComp;
import sg.edu.nus.comp.pdtb.parser.ArgPosComp;
import sg.edu.nus.comp.pdtb.parser.Component;
import sg.edu.nus.comp.pdtb.parser.ConnComp;
import sg.edu.nus.comp.pdtb.parser.ExplicitComp;
import sg.edu.nus.comp.pdtb.parser.NonExplicitComp;
import sg.edu.nus.comp.pdtb.runners.PdtbParser;
import sg.edu.nus.comp.pdtb.runners.SpanTreeExtractor;
import sg.edu.nus.comp.pdtb.util.Corpus;
import sg.edu.nus.comp.pdtb.util.Settings;
import sg.edu.nus.comp.pdtb.util.Util;

public class ShallowDiscourseParser {

	private static Logger log = LogManager.getLogger(PdtbParser.class.toString());

	public static void main(String[] args) throws IOException {
		if (args.length < 1) {
			log.error("Please supply path to a text file or directory containging .txt files. ");
		} else {
/*******************wsj_xxxx文件格式转换, 将文件后缀加上.txt, 然后去除第一行的.START*****************************/

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
			
/*******************wsj_xxxx文件格式转换, 将文件后缀加上.txt, 然后去除第一行的.START*****************************/
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
/******************将结果文件.pipe文件转换成JSON格式的文件***************************************************/

			File outputDir = new File("data/raw/output");
			
			if(outputDir.isDirectory()){
				int ID = 35708;
				File[] files = outputDir.listFiles();
				File outputFile = new File("data/pdtb-output.json");
				
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

/******************将结果文件.pipe文件转换成JSON格式的文件***************************************************/
		}
	}

	private static void doBatchParsing(File topDirectory) throws IOException {
		String outputFolder = OUTPUT_FOLDER_NAME;
		Queue<File> directoryQueue = new LinkedList<>();
		Queue<File> fileQueue = new LinkedList<>();
		directoryQueue.add(topDirectory);
		int fileCount = 0;
		int parsedFilesCount = 0;
		while (directoryQueue.size() > 0) {
			File currentDir = directoryQueue.poll();
			log.info("Working in " + currentDir);
			File[] files = currentDir.listFiles();
			for (File file : files) {
				if (!file.getName().startsWith(".") && !file.isHidden()) {
					if (file.isDirectory()) {
						log.info("Adding directory " + file + " to queue.");
						directoryQueue.add(file);
					} else {
						if (file.getName().endsWith(".txt")) {
							OUTPUT_FOLDER_NAME = file.getParentFile().getAbsolutePath() + "/" + outputFolder;
							new File(OUTPUT_FOLDER_NAME).mkdir();

							if (!(new File(Settings.OUTPUT_FOLDER_NAME + file.getName() + ".pipe").exists())) {
								fileQueue.add(file);
								++fileCount;
							} else {
								++fileCount;
								++parsedFilesCount;
								log.info("Pipe aldready exists, skipping " + file);
							}
						}
					}
				}
			}
		}

		log.info("");
		log.info("Files to process " + parsedFilesCount + "/" + fileCount + " - "
				+ String.format("%.2f", (100.0 * parsedFilesCount / fileCount)) + "%");

		while (fileQueue.size() > 0) {
			++parsedFilesCount;
			File file = fileQueue.poll();
			log.info("Parsing file: " + file);
			OUTPUT_FOLDER_NAME = file.getParentFile().getAbsolutePath() + "/" + outputFolder;
			PdtbParser.parseFile(file, true);
			log.info(String.format("Done %.2f", (100.0 * parsedFilesCount / fileCount)) + "%");
		}
	}

	public static void parseFile(File inputFile, boolean prepareAuxData) throws IOException {
		if (prepareAuxData) {
			prepareAuxData(inputFile);
		}
		log.info("Running the PDTB parser");
		Component parser = new ConnComp();
		log.info("Running connective classifier...");
		parser.parseAnyText(inputFile);
		log.info("Done.");
		parser = new ArgPosComp();
		log.info("Running argument position classifier...");
		parser.parseAnyText(inputFile);
		log.info("Done.");
		parser = new ArgExtComp();
		log.info("Running argument extractor classifier...");
		File pipeFile = parser.parseAnyText(inputFile);
		Map<String, String> pipeMap = genPipeMap(pipeFile);
		log.info("Done.");
		parser = new ExplicitComp();
		log.info("Running Explicit classifier...");
		File expSenseFile = parser.parseAnyText(inputFile);
		joinSense(pipeMap, expSenseFile, pipeFile);
		log.info("Done.");
		parser = new NonExplicitComp();
		log.info("Running NonExplicit classifier...");
		File nonExpSenseFile = parser.parseAnyText(inputFile);
		appendToFile(pipeFile, nonExpSenseFile);
		log.info("Done with everything. The PDTB relations for the file are in: " + pipeFile);
	}

	private static void appendToFile(File pipeFile, File nonExpSenseFile) throws IOException {

		try (FileWriter writer = new FileWriter(pipeFile, true); BufferedReader reader = Util.reader(nonExpSenseFile)) {
			String line;
			while ((line = reader.readLine()) != null) {
				writer.write(line + Util.NEW_LINE);
			}
		}
	}

	private static void joinSense(Map<String, String> pipeMap, File expSenseFile, File pipeFile) throws IOException {
		try (BufferedReader reader = Util.reader(expSenseFile)) {
			String line;
			while ((line = reader.readLine()) != null) {
				String[] tmp = line.split("\\|", -1);
				String pipe = pipeMap.get(tmp[0]);
				if (pipe == null) {
					log.error("Cannot find connective span in pipe map.");
				}
				String[] cols = pipe.split("\\|", -1);

				StringBuilder resultLine = new StringBuilder();

				for (int i = 0; i < cols.length; i++) {
					String col = cols[i];
					if (i == 11) {
						resultLine.append(tmp[1] + "|");
					} else {
						resultLine.append(col + "|");
					}
				}
				resultLine.deleteCharAt(resultLine.length() - 1);
				cols = resultLine.toString().split("\\|", -1);

				pipeMap.put(tmp[0], resultLine.toString());
			}
		}

		PrintWriter pw = new PrintWriter(pipeFile);
		for (String pipe : pipeMap.values()) {
			pw.println(pipe);
		}
		pw.close();
	}

	private static Map<String, String> genPipeMap(File pipeFile) throws IOException {

		Map<String, String> map = new HashMap<>();
		try (BufferedReader reader = Util.reader(pipeFile)) {
			String line;

			while ((line = reader.readLine()) != null) {
				String[] cols = line.split("\\|", -1);
				if (cols.length != 48) {
					log.error("Pipe file " + pipeFile.getAbsolutePath() + " is corrupted, number of columns is "
							+ cols.length + " instead of 48.");
				}
				map.put(cols[3], line);
			}
		}
		return map;
	}

	private static void prepareAuxData(File testFile) throws IOException {

		File[][] trees = Corpus.prepareParseAndDependecyTrees(new File[] { testFile });

		SpanTreeExtractor.anyTextToSpanGen(trees[0][0], testFile);
	}

}
