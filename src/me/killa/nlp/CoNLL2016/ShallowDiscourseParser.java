package me.killa.nlp.CoNLL2016;

import static sg.edu.nus.comp.pdtb.util.Settings.OUTPUT_FOLDER_NAME;

import java.io.BufferedReader;
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

import net.sf.json.JSONObject;
import net.sf.json.JSONSerializer;
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
//		JSONSerializer js = new JSONSerializer();
//		JSONObject jo = new JSONObject();
//
//		BufferedReader bfr = new BufferedReader(new FileReader(new File("data/pdtb-parses.json")));
//
//		String str = bfr.readLine();
//
//		System.out.println(js.toJSON(str).isEmpty());

		if (args.length < 1) {
			log.error("Please supply path to a text file or directory containging .txt files. ");
		} else {
			File inputFile = new File(args[0]);
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
