package ranker;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;

import weka.core.Instances;
import weka.core.converters.ArffLoader;
import weka.core.converters.ArffSaver;
import weka.core.converters.CSVSaver;
import weka.core.converters.LibSVMSaver;
import weka.core.converters.SVMLightSaver;

public class GenerateRankerInput {

	public static String inputFileName = "";
	public static String sortedFileName = "";
	public static String predictionsFileName = "";
	public static String rankerOutputFileName = "";
	public static String engpredictionFileName = "";

	public static ArrayList<Long> tid = new ArrayList<Long>();
	public static ArrayList<String> predictions = new ArrayList<String>();
	public static ArrayList<Double> ranks = new ArrayList<Double>();
	public static ArrayList<String> engprediction = new ArrayList<String>();

	public static ArrayList<User> users = new ArrayList<User>();
	public static HashMap<String, String> tidPredictionMap = new HashMap<String, String>();
	public static HashMap<String, Double> tidRankMap = new HashMap<String, Double>();

	/**
	 * @param args
	 * @throws IOException
	 */

	public static void main(String args[]) throws IOException {
		if (args.length < 2) {
			printUsage();
			return;
		}

		if (args[0].equals("beforeranking")) {
			inputFileName = args[1];
			predictionsFileName = args[2];
			beforeRanker();
		} else if (args[0].equals("afterranking")) {
			inputFileName = args[1];
			sortedFileName = args[2];
			rankerOutputFileName = args[3];
			predictionsFileName = args[4];
			engpredictionFileName = args[5];
			afterRanker();
		} else {
			printUsage();
		}
	}

	private static void printUsage() {
		System.out
				.println("USAGE: java -Xmx4096m -jar GenerateRankerInput.jar beforeranking [test-ARFF-file] [predictions-file]"
						+ "\nor\n"
						+ "USAGE: java -Xmx4096m -jar GenerateRankerInput.jar afterranking [test-ARFF-file] [ranker-input-ARFF-file] [ranker-score-file] [predictions-file] [engpredictions-file]");

	}

	public static void afterRanker() throws IOException {
		readTestTweetIDs();
		Instances data = loadTrainingARFF(inputFileName);
		Instances sortedData = loadTrainingARFF(sortedFileName);

		readRankerScoreOutputFile();
		readPredictionsFile();
		readengpredictionsFile();

		for (int i = 0; i < data.numInstances(); i++) {
			tidPredictionMap.put(
					data.instance(i).stringValue(data.attribute("id")),
					predictions.get(i));
		}

		for (int i = 0; i < sortedData.numInstances(); i++) {
			tidRankMap.put(
					sortedData.instance(i).stringValue(
							sortedData.attribute("id")), ranks.get(i));
		}

		double r = 0; // For rank.
		for (int i = 0; i < data.numInstances(); i++) {
			r = 0;
			if (tidRankMap.keySet().contains(
					data.instance(i).stringValue(data.attribute("id")))) {
				r = tidRankMap.get(data.instance(i).stringValue(
						data.attribute("id")));
			}
			try {
				users.add(new User((Long.parseLong(data.instance(i)
						.stringValue(data.attribute("twitter_user_id")))), data
						.instance(i).stringValue(data.attribute("id")), tid
						.get(i),
						(Long.parseLong(data.instance(i)
								.stringValue(data.attribute("engagement"))
								.split("_")[0])), r,
						tidPredictionMap.get(data.instance(i).stringValue(
								data.attribute("id"))), engprediction.get(i)));
			} catch (Exception e) {
				System.out.println(((data.instance(i).stringValue(data
						.attribute("twitter_user_id"))))
						+ " "
						+ data.instance(i).stringValue(data.attribute("id"))
						+ " "
						+ tid.get(i)
						+ " "
						+ ((data.instance(i).stringValue(data
								.attribute("engagement"))))
						+ " "
						+ r
						+ " "
						+ tidPredictionMap.get(data.instance(i).stringValue(
								data.attribute("id")))
						+ " "
						+ engprediction.get(i));
			}
		}

		Collections.sort(users, new Comparator<User>() {
			@Override
			public int compare(User u1, User u2) {
				if (u2.uid > u1.uid) {
					return 1;
				} else if (u2.uid < u1.uid) {
					return -1;
				} else {
					if (u2.rank > u1.rank) {
						return 1;
					} else if (u2.rank < u1.rank) {
						return -1;
					} else {
						if (u2.tidFull > u1.tidFull) {
							return 1;
						} else if (u2.tidFull < u1.tidFull) {
							return -1;
						}
					}
				}
				return 0;
			}
		});

		printUsers();
	}

	public static void beforeRanker() throws IOException {

		readTestTweetIDs();
		readPredictionsFile();

		Instances data = loadTrainingARFF(inputFileName);

		for (int i = 0; i < data.numInstances(); i++) {
			tidPredictionMap.put(
					data.instance(i).stringValue(data.attribute("id")),
					predictions.get(i));
		}

		Instances data_0 = new Instances(data);

		for (int i = 0; i < data.numInstances();) {
			if (tidPredictionMap.get(
					data.instance(i).stringValue(data.attribute("id"))).equals(
					"00")
					|| tidPredictionMap.get(
							data.instance(i).stringValue(data.attribute("id")))
							.equals("0")) {
				data.delete(i);
			} else
				i++;
		}

		for (int i = 0; i < data_0.numInstances();) {
			if (tidPredictionMap.get(
					data_0.instance(i).stringValue(data_0.attribute("id")))
					.equals("00")
					|| tidPredictionMap.get(
							data_0.instance(i).stringValue(
									data_0.attribute("id"))).equals("0")) {
				i++;
			} else
				data_0.delete(i);
		}

		data.sort(data.attribute("twitter_user_id"));
		data_0.sort(data.attribute("twitter_user_id"));

		writeARFF(data, "sorted.arff");
		writeCSV(data, "sorted.csv");
		//writeSVM(data, "sorted.dat");
		writeARFF(data_0, "sorted_0.arff");
		writeCSV(data_0, "sorted_0.csv");
		//writeSVM(data_0, "sorted_0.dat");
	}

	private static void printUsers() throws IOException {
		BufferedWriter solWriter = new BufferedWriter(new FileWriter(
				"participant_solution.dat"));
		solWriter.write("userid,tweetid,engagement\n");
		for (int i = 0; i < users.size(); i++) {
			solWriter.write(users.get(i).uid + "," + users.get(i).tidFull + ","
					+ users.get(i).prediction + "\n");
		}
		solWriter.flush();
		solWriter.close();
		System.out.println("Finished writing: participant_solution.dat");
	}

	public static Instances loadTrainingARFF(String fname) throws IOException {
		ArffLoader tr = new ArffLoader();
		tr.setSource(new File(fname));
		Instances data = tr.getDataSet();
		return data;
	}

	public static void writeARFF(Instances t, String fname) throws IOException {
		ArffSaver saver = new ArffSaver();
		saver.setInstances(t);
		saver.setFile(new File(fname));
		saver.writeBatch();
		System.out.println("Done writing: " + fname);
	}

	public static void writeCSV(Instances t, String fname) throws IOException {
		CSVSaver saver = new CSVSaver();
		saver.setInstances(t);
		saver.setFile(new File(fname));
		saver.writeBatch();
		System.out.println("Done writing: " + fname);
	}

	public static void writeSVM(Instances t, String fname) throws IOException {
		SVMLightSaver saver = new SVMLightSaver();
		saver.setInstances(t);
		saver.setFile(new File(fname));
		saver.writeBatch();
		System.out.println("Done writing: " + fname);
	}

	public static void readTestTweetIDs() throws IOException {
		BufferedReader br = new BufferedReader(new FileReader(
				"test_only_id.txt"));
		String line = "";
		tid.clear();
		while ((line = br.readLine()) != null) {
			tid.add(Long.parseLong(line.trim()));
		}
		br.close();
	}

	public static void readPredictionsFile() throws IOException {
		BufferedReader br = new BufferedReader(new FileReader(
				predictionsFileName));
		String line = "";
		predictions.clear();
		line = br.readLine();
		while ((line = br.readLine()) != null) {
			predictions.add(line.split(",")[2].split(":")[1]);
		}
		br.close();
	}

	public static void readengpredictionsFile() throws IOException {
		BufferedReader br = new BufferedReader(new FileReader(
				engpredictionFileName));
		String line = "";
		engprediction.clear();
		while ((line = br.readLine()) != null) {
			engprediction.add(line);
		}
		br.close();
	}

	public static void readengpredictionsFile_() throws IOException {
		BufferedReader br = new BufferedReader(new FileReader(
				engpredictionFileName));
		String line = "";
		engprediction.clear();
		line = br.readLine();
		while ((line = br.readLine()) != null) {
			engprediction.add((line.split(",")[2].split(":")[1]));
		}
		br.close();
	}

	public static void readRankerScoreOutputFile() throws IOException {
		BufferedReader br = new BufferedReader(new FileReader(
				rankerOutputFileName));
		String line = "";
		ranks.clear();
		while ((line = br.readLine()) != null) {
			ranks.add(Double.parseDouble(line));
		}
		br.close();
	}
}

class User {
	long uid;
	String tid;
	long tidFull;
	long engagement;
	double rank;
	int prediction;
	int engprediction;

	public User(long uid, String tid, long tidFull, long engagement,
			Double rank, String prediction, String engprediction) {
		super();
		this.uid = uid;
		this.tid = tid;
		this.tidFull = tidFull;
		this.engagement = engagement;
		this.rank = rank;
		//this.prediction = getMedian(prediction);
		this.prediction = Integer.parseInt(prediction);
		this.engprediction = (int) Math
				.round(Double.parseDouble(engprediction));
	}

	private int getMedian(String prediction) {
		int median;
		String[] tokens = prediction.split("_");
		int[] tints = new int[tokens.length];
		for (int i = 0; i < tokens.length; i++) {
			tints[i] = Integer.parseInt(tokens[i]);
		}
		Arrays.sort(tints);

		if (tints.length % 2 == 0)
			median = (tints[tints.length / 2] + tints[tints.length / 2 - 1]) / 2;
		else
			median = tints[tints.length / 2];
		return median;
	}
}
