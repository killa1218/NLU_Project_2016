package me.killa.nlp.CoNLL2016;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

import net.sf.json.JSONObject;
import net.sf.json.JSONSerializer;

public class ShallowDiscourseParser {

	public static void main(String[] args) throws IOException {
		JSONSerializer js = new JSONSerializer();
		JSONObject jo = new JSONObject();

		BufferedReader bfr = new BufferedReader(new FileReader(new File("data/pdtb-parses.json")));

		String str = bfr.readLine();

		System.out.println(js.toJSON(str).isEmpty());
	}

}
