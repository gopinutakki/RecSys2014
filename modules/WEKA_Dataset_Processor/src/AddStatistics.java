import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import weka.core.Attribute;
import weka.core.Instances;
import weka.core.converters.ArffLoader;
import weka.core.converters.ArffSaver;

public class AddStatistics {

	public static void main(String args[]) throws IOException {
		System.out.println(args[0]);

		Map<String, Double> newValues = new HashMap<String, Double>();

		Instances training = loadTrainingARFF(args[0]);
		training.insertAttributeAt(new Attribute("avg_user_rating"),
				training.numAttributes());
		training.insertAttributeAt(new Attribute("avg_movie_rating"),
				training.numAttributes());
		training.insertAttributeAt(new Attribute("movie_retweet_count"),
				training.numAttributes());
		training.insertAttributeAt(new Attribute("entities-user_mentions-id_str_count"),
				training.numAttributes());
		training.insertAttributeAt(new Attribute("retweeted_status-entities-user_mentions-id_count"),
				training.numAttributes());

		newValues = getAverage(training, "twitter_user_id", "movie_rating");
		training = updateInstances(training, newValues, "twitter_user_id",
				"avg_user_rating");
		
		newValues = getCount(training, "twitter_user_id", "entities-user_mentions-id_str");
		training = updateInstances(training, newValues, "twitter_user_id",
				"entities-user_mentions-id_str_count");

		newValues = getCount(training, "twitter_user_id", "retweeted_status-entities-user_mentions-id");
		training = updateInstances(training, newValues, "twitter_user_id",
				"retweeted_status-entities-user_mentions-id_count");			
		
		newValues = getAverage(training, "imdb_item_id", "movie_rating");
		training = updateInstances(training, newValues, "imdb_item_id",
				"avg_movie_rating");

		newValues = getCount(training, "imdb_item_id", "class");
		training = updateInstances(training, newValues, "imdb_item_id",
				"movie_retweet_count");

		writeInstances(training);
		System.out.println("Done!");
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
		Map<String, Double> avgRating = new HashMap<String, Double>(); // name says avg but it is only count. I am too lazy.
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
}

