import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import weka.core.Attribute;
import weka.core.FastVector;
import weka.core.Instances;
import weka.core.converters.ArffLoader;
import weka.core.converters.ArffSaver;

public class AddStatistics {

	static HashMap<String, HashMap<String, String>> imdb_data = new HashMap<String, HashMap<String, String>>();

	public static void main(String args[]) throws IOException, ParseException {
		System.out.println(args[0]);

		Map<String, Double> newValues = new HashMap<String, Double>();

		Instances training = loadTrainingARFF(args[0]);
		training.insertAttributeAt(new Attribute("avg_user_rating"),
				training.numAttributes());
		training.insertAttributeAt(new Attribute("avg_movie_rating"),
				training.numAttributes());
		training.insertAttributeAt(new Attribute("movie_retweet_count"),
				training.numAttributes());
		training.insertAttributeAt(new Attribute(
				"entities-user_mentions-id_str_count"), training
				.numAttributes());
		training.insertAttributeAt(new Attribute(
				"retweeted_status-entities-user_mentions-id_count"), training
				.numAttributes());
		training.insertAttributeAt(new Attribute("unix_timestamp",
				(FastVector) null), training.numAttributes());

		training.insertAttributeAt(new Attribute("imdb_genres",
				(FastVector) null), training.numAttributes());
		training.insertAttributeAt(
				new Attribute("imdb_cast", (FastVector) null),
				training.numAttributes());
		training.insertAttributeAt(new Attribute("imdb_release_date",
				(FastVector) null), training.numAttributes());
		training.insertAttributeAt(new Attribute("imdb_director",
				(FastVector) null), training.numAttributes());

		newValues = getAverage(training, "twitter_user_id", "movie_rating");
		training = updateInstances(training, newValues, "twitter_user_id",
				"avg_user_rating");

		newValues = getCount(training, "twitter_user_id",
				"entities-user_mentions-id_str");
		training = updateInstances(training, newValues, "twitter_user_id",
				"entities-user_mentions-id_str_count");

		newValues = getCount(training, "twitter_user_id",
				"retweeted_status-entities-user_mentions-id");
		training = updateInstances(training, newValues, "twitter_user_id",
				"retweeted_status-entities-user_mentions-id_count");

		newValues = getAverage(training, "imdb_item_id", "movie_rating");
		training = updateInstances(training, newValues, "imdb_item_id",
				"avg_movie_rating");

		newValues = getCount(training, "imdb_item_id", "class");
		training = updateInstances(training, newValues, "imdb_item_id",
				"movie_retweet_count");

		training = addUNIXTimestamp(training);
		
		readIMDbData();

		training = updateInstanceIMDb(training, "imdb_genres");
		training = updateInstanceIMDb(training, "imdb_cast");
		training = updateInstanceIMDb(training, "imdb_release_date");
		training = updateInstanceIMDb(training, "imdb_director");

		writeInstances(training);
		System.out.println("Done!");
	}

	private static Instances addUNIXTimestamp(Instances training)
			throws ParseException {
		for (int i = 0; i < training.numInstances(); i++) {
			String ts = training.instance(i).stringValue(
					training.attribute("created_at"));
			training.instance(i).setValue(training.attribute("unix_timestamp"),
					getUNIXTimestamp(ts));
		}
		return training;
	}

	private static Instances updateInstanceIMDb(Instances training,
			String imdb_ftr) {

		String id;
		for (int i = 0; i < training.numInstances(); i++) {
			id = training.instance(i).stringValue(
					training.attribute("imdb_item_id"));
			try {
				if (imdb_data.keySet().contains(id)) {
					training.instance(i).setValue(training.attribute(imdb_ftr),
							imdb_data.get(id).get(imdb_ftr) + "");
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return training;
	}

	private static void writeInstances(Instances training) throws IOException {
		ArffSaver saver = new ArffSaver();
		saver.setInstances(training);
		saver.setFile(new File("mahsa3.arff"));
		saver.writeBatch();
	}

	private static Instances updateInstances(Instances training,
			Map<String, Double> newValues, String k1, String k2) {

		String id;
		for (int i = 0; i < training.numInstances(); i++) {

			id = (int) training.instance(i).value(training.attribute(k1)) + "";
			try {
				training.instance(i).setValue(training.attribute(k2),
						newValues.get(id));
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return training;
	}

	public static Instances loadTrainingARFF(String fname) throws IOException {
		ArffLoader tr = new ArffLoader();
		tr.setSource(new File(fname));
		Instances data = tr.getDataSet();

		return data;
	}

	public static String getUNIXTimestamp(String date) throws ParseException {
		final String TWITTER = "EEE MMM dd HH:mm:ss ZZZZZ yyyy";
		SimpleDateFormat sf = new SimpleDateFormat(TWITTER, Locale.ENGLISH);
		sf.setLenient(true);
		return sf.parse(date).getTime() + "";
	}

	public static Map<String, Double> getAverage(Instances training,
			String key1, String key2) {
		Map<String, Double> avgRating = new HashMap<String, Double>();
		Map<String, Double> appCount = new HashMap<String, Double>();

		String uid;
		int rating;

		for (int i = 0; i < training.numInstances(); i++) {

			try {
				uid = (int) training.instance(i)
						.value(training.attribute(key1)) + "";

				rating = (int) training.instance(i).value(
						training.attribute(key2));
			} catch (NumberFormatException nfe) {
				continue;
			} catch (ArrayIndexOutOfBoundsException aiobe) {
				continue;
			}

			double count = appCount.containsKey(uid) ? appCount.get(uid) : 0;
			appCount.put(uid, count + 1);

			double r = avgRating.containsKey(uid) ? avgRating.get(uid) : 0;
			avgRating.put(uid, r + rating);
		}

		for (String u : appCount.keySet()) {
			double rt = avgRating.get(u);
			double c = appCount.get(u);

			avgRating.put(u, rt / c);
		}

		return avgRating;
	}

	public static Map<String, Double> getCount(Instances training, String key1,
			String key2) {
		// name says avg but it is only count. I am too lazy.
		Map<String, Double> avgRating = new HashMap<String, Double>();
		Map<String, Double> appCount = new HashMap<String, Double>();

		String uid;
		int rating;

		for (int i = 0; i < training.numInstances(); i++) {

			try {
				uid = (int) training.instance(i)
						.value(training.attribute(key1)) + "";
				rating = (int) training.instance(i).value(
						training.attribute(key2));
			} catch (NumberFormatException nfe) {
				continue;
			} catch (ArrayIndexOutOfBoundsException aiobe) {
				continue;
			}

			double count = appCount.containsKey(uid) ? appCount.get(uid) : 0;
			appCount.put(uid, count + 1);

			double r = avgRating.containsKey(uid) ? avgRating.get(uid) : 0;
			avgRating.put(uid, r + rating);
		}

		return avgRating;
	}

	public static void readIMDbData() throws IOException {
		// C:\Users\WKUUSER\Documents\RecSys2014\dataset
		String imdbFile = "C:\\Users\\WKUUSER\\Documents\\RecSys2014\\dataset\\imdb_features.csv";
		// String imdbFile = "/home/gopi/RecSys2014/dataset/imdb_features.csv";
		String line = "";
		BufferedReader br = new BufferedReader(new FileReader(imdbFile));

		while ((line = br.readLine()) != null) {
			try {
				String[] tokens = line.split("\t", -1);
				HashMap<String, String> imdbVals = new HashMap<String, String>();

				imdbVals.put("imdb_genres", tokens[1].trim());
				imdbVals.put("imdb_cast", tokens[2].trim());
				imdbVals.put("imdb_release_date", tokens[3].trim());
				imdbVals.put("imdb_director", tokens[4].trim());

				imdb_data
						.put(Integer.parseInt(tokens[0].trim()) + "", imdbVals);
			} catch (ArrayIndexOutOfBoundsException e) {
				System.out.println("Error--> " + line + "\n");
			}
		}
		br.close();
	}
}
