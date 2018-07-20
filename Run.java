package de.unidue.langtech.teaching.seminar;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.collection.CollectionReaderDescription;
import org.apache.uima.fit.factory.AnalysisEngineFactory;
import org.apache.uima.fit.factory.CollectionReaderFactory;
import org.apache.uima.fit.pipeline.SimplePipeline;
import de.tudarmstadt.ukp.dkpro.core.arktools.ArktweetTokenizer;

public class Run {
	public static TreeMap<String, Float> wordValuePairs = new TreeMap<>();
	public static TreeMap<LocalDate, Float> dayAddedValuePairsAustralia = new TreeMap<>();
	public static TreeMap<LocalDate, Integer> dayTweetCounterAustralia = new TreeMap<>();
	public static TreeMap<LocalDate, Float> dayAddedValuePairsUnitedKingdom = new TreeMap<>();
	public static TreeMap<LocalDate, Integer> dayTweetCounterUnitedKingdom = new TreeMap<>();
	public static DateTimeFormatter formatter = DateTimeFormatter.ofPattern("EEE MMM dd HH:mm:ss Z yyyy",
			Locale.ENGLISH);
	public static int counterAus = 0;
	public static int counterUK = 0;

	public static void main(String[] args) throws Exception {
		String inputFolder = "src/main/resources/sample";

		try {
			getWordValueMapFromTxt(wordValuePairs);
		} catch (IOException e) {
			System.err.println("Error while reading Value and Word files");
		}

		// Read all files with file ending *.gz, *.bz2 or *.json that are located in the
		// inputFolder
		CollectionReaderDescription reader = CollectionReaderFactory.createReaderDescription(RawJsonTweetReader.class,
				RawJsonTweetReader.PARAM_SOURCE_LOCATION, inputFolder, RawJsonTweetReader.PARAM_PATTERNS,
				new String[] { "*.gz", "*.bz2", "*.json" });

		// This one hacks a twitter message into single words. This one is bit smarter
		// than just
		// splitting at whitespaces
		AnalysisEngineDescription tokenizer = AnalysisEngineFactory.createEngineDescription(ArktweetTokenizer.class);

		// A demo that shows some basic accessing of the framework API and how to get
		// access to the
		// information in the tweets
		AnalysisEngineDescription printer = AnalysisEngineFactory.createEngineDescription(Printer.class);

		// This framework has many more modules that might become useful for you
		// (depending on your task)
		// An entire list can be found here
		// https://code.google.com/p/dkpro-core-asl/wiki/ComponentList_1_6_2

		SimplePipeline.runPipeline(reader, tokenizer, printer);

		
		try {
			safeHappinessDataInTxt();
		} catch (IOException e) {
			System.err.println("Error while writing Australia and UK files");
		}
		System.out.println("Australien: " + counterAus);
		System.out.println("Vereinigtes Königreich: " + counterUK);
		System.err.println("Fertig.");
	}

	public static void safeHappinessDataInTxt() throws IOException {

		BufferedWriter wordsOutAustralia = new BufferedWriter(
				new FileWriter("src/main/resources/happiness-values/australiaTimeZoned1Lens.txt"));
		for (Map.Entry<LocalDate, Float> entry : dayAddedValuePairsAustralia.entrySet()) {
			wordsOutAustralia.write(
					entry.getKey().toString() + " " + entry.getValue() / dayTweetCounterAustralia.get(entry.getKey()));
			wordsOutAustralia.newLine();
		}
		wordsOutAustralia.close();

		BufferedWriter wordsOutUnitedKingdom = new BufferedWriter(
				new FileWriter("src/main/resources/happiness-values/ukTimeZoned1Lens.txt"));
		for (Map.Entry<LocalDate, Float> entry : dayAddedValuePairsUnitedKingdom.entrySet()) {
			wordsOutUnitedKingdom.write(entry.getKey().toString() + " "
					+ entry.getValue() / dayTweetCounterUnitedKingdom.get(entry.getKey()));
			wordsOutUnitedKingdom.newLine();
		}
		wordsOutUnitedKingdom.close();
	}

	private static void getWordValueMapFromTxt(TreeMap<String, Float> wordValuePairs) throws IOException {

		BufferedReader wordsIn = new BufferedReader(
				new FileReader("src/main/resources/happiness-values/happinessWords.txt"));
		BufferedReader valuesIn = new BufferedReader(
				new FileReader("src/main/resources/happiness-values/happinessValues.txt"));
		String word;
		float value;

		while ((word = wordsIn.readLine()) != null) {
			value = Float.parseFloat(valuesIn.readLine());
			wordValuePairs.put(word, value);
		}

		wordsIn.close();
		valuesIn.close();
	}

}
