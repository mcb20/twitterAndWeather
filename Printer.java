package de.unidue.langtech.teaching.seminar;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.TreeMap;

import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.fit.component.JCasAnnotator_ImplBase;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token;
import de.unidue.langtech.teaching.type.RawTweet;

public class Printer extends JCasAnnotator_ImplBase {
	// int i = 0;

	@Override
	public void process(JCas aJCas) throws AnalysisEngineProcessException {
		// This method is called for each Tweet the reader reads
		// printRaw(aJCas);
		// i++;
		// if(i>10000) {
		// try {
		// System.out.println("KKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKK");
		// Run.safeHappinessDataInTxt();
		// } catch (IOException e) {
		// // TODO Auto-generated catch block
		// e.printStackTrace();
		// }
		// }

		RawTweet raw = JCasUtil.selectSingle(aJCas, RawTweet.class);
		String rawTweet = raw.getRawTweet();
		JSONParser parser = new JSONParser();

		try {
			JSONObject jsonTweet = (JSONObject) parser.parse(rawTweet);

			if (jsonTweet.get("place") != null
					&& (((JSONObject) jsonTweet.get("place")).get("country").equals("Australia")
							|| ((JSONObject) jsonTweet.get("place")).get("country").equals("United Kingdom"))) {
				printTokens(aJCas, jsonTweet);
				System.out.println();
			}
		} catch (ParseException e) {
			System.err.println("END OF FILE");
		} catch (java.text.ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private void printTokens(JCas aJCas, JSONObject jsonTweet) throws ParseException, java.text.ParseException {
		// The splitting into words that the ArkTokenizer did is stored in a data type
		// called
		// "Token"
		// We get all data types in this JCas (aka a single tweet) and then print each
		// word or token
		Collection<Token> tokens = JCasUtil.select(aJCas, Token.class);

		List<Float> wordValues = getWordValueOfSingleWords(tokens);
		if (wordValues.size() == 0) {
			return;
		}

		LocalDateTime date = LocalDateTime.parse((CharSequence) jsonTweet.get("created_at"), Run.formatter);
		LocalDate localDate;
		if (((JSONObject) jsonTweet.get("place")).get("country").equals("Australia")) {
			localDate = date.plusHours(10).toLocalDate();
			addToTreeMaps(localDate, wordValues, Run.dayAddedValuePairsAustralia, Run.dayTweetCounterAustralia);
			System.out.println(date.plusHours(10));
		} else {
			localDate = date.plusHours(1).toLocalDate();
			addToTreeMaps(localDate, wordValues, Run.dayAddedValuePairsUnitedKingdom, Run.dayTweetCounterUnitedKingdom);
			System.out.println(date.plusHours(1));
		}

		System.out.println(localDate);
		System.out.println(wordValues);
	}

	/**
	 * adding the tweet value to the Date in a TreeMap
	 */

	private void addToTreeMaps(LocalDate localDate, List<Float> wordValues,
			TreeMap<LocalDate, Float> dayAddedValuePairs, TreeMap<LocalDate, Integer> dayTweetCounter) {

		for (float value : wordValues) {
			if (!dayAddedValuePairs.containsKey(localDate)) {
				dayAddedValuePairs.put(localDate, value);
			} else {
				float temporaryFloat = dayAddedValuePairs.get(localDate).floatValue();
				dayAddedValuePairs.put(localDate, value + temporaryFloat);
			}
		}
		if (!dayTweetCounter.containsKey(localDate)) {
			dayTweetCounter.put(localDate, wordValues.size());
		} else {
			int temporaryInt = dayTweetCounter.get(localDate).intValue();
			dayTweetCounter.put(localDate, temporaryInt + wordValues.size());
		}
	}

	/**
	 * if token is a listed word then get happiness-values for the tweet
	 */
	private List<Float> getWordValueOfSingleWords(Collection<Token> tokens) {
		List<Float> valuesOfSingleWords = new ArrayList<>();
		Float tokenValue = null;

		for (Token token : tokens) {
			tokenValue = Run.wordValuePairs.get(token.getCoveredText().toLowerCase());
			if (tokenValue != null && tokenValue.floatValue() >= 1 && tokenValue.floatValue() <= 9
			// && (tokenValue.floatValue() < 4 || tokenValue.floatValue() > 6)
			) {
				valuesOfSingleWords.add(tokenValue.floatValue());
			}
			tokenValue = null;
		}
		return valuesOfSingleWords;
	}

}
