import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import weka.core.Attribute;
import weka.core.Instances;
import weka.core.converters.ArffLoader;
import weka.core.converters.ArffSaver;

public class AddStatistics {

	static Map<String, Double> count_user_appearance = new HashMap<String, Double>();
	static Map<String, Double> count_imdb_appearance = new HashMap<String, Double>();

	static Map<String, Double> rating_from_user = new HashMap<String, Double>();
	static Map<String, Double> rating_for_imdb_movie = new HashMap<String, Double>();

	public static void main(String args[]) throws IOException {
		System.out.println(args[0]);
		Instances training = loadTrainingARFF(args[0]);
		getAverageUserRating(training);
		getAverageMovieRating(training);
		
		training.insertAttributeAt(new Attribute("avg_user_rating"),
				training.numAttributes());
		training.insertAttributeAt(new Attribute("avg_movie_rating"),
				training.numAttributes());

		training = updateInstances(training);

		writeInstances(training);
		System.out.println("Done!");
	}

	private static void writeInstances(Instances training) throws IOException {
		ArffSaver saver = new ArffSaver();
		saver.setInstances(training);
		saver.setFile(new File("mahsa3.arff"));
		saver.writeBatch();
	}

	private static Instances updateInstances(Instances training) {

		String uid, mid;
		for (int i = 0; i < training.numInstances(); i++) {

			uid = (int) training.instance(i).value(
					training.attribute("twitter_user_id"))
					+ "";

			mid = (int) training.instance(i).value(
					training.attribute("imdb_item_id"))
					+ "";
			training.instance(i).setValue(
					training.attribute("avg_user_rating"),
					rating_from_user.get(uid));
			try {
				training.instance(i).setValue(
						training.attribute("avg_movie_rating"),
						rating_for_imdb_movie.get(mid));
			} catch (NullPointerException npe) {
				// training.instance(i).setValue(
				// training.attribute("avg_movie_rating"),
				// training.instance(i).value(
				// training.attribute("movie_rating")));

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

	public static void getAverageMovieRating(Instances training) {

		String mid;
		int rating;

		for (int i = 0; i < training.numInstances(); i++) {

			mid = (int) training.instance(i).value(
					training.attribute("imdb_item_id"))
					+ "";
						
			rating = (int) training.instance(i).value(
					training.attribute("movie_rating"));

			double count = count_imdb_appearance.containsKey(mid) ? count_imdb_appearance
					.get(mid) : 0;
			count_imdb_appearance.put(mid, count + 1);

			double r = rating_for_imdb_movie.containsKey(mid) ? rating_for_imdb_movie
					.get(mid) : 0;
			rating_for_imdb_movie.put(mid, r + rating);
		}

		for (String u : count_imdb_appearance.keySet()) {
			double rt = rating_for_imdb_movie.get(u);
			double c = count_imdb_appearance.get(u);

			count_imdb_appearance.put(u, rt/c);			
		}
	}

	public static void getAverageUserRating(Instances training) {

		String uid;
		int rating;

		for (int i = 0; i < training.numInstances(); i++) {

			try {
				uid = (int) training.instance(i).value(
						training.attribute("twitter_user_id"))
						+ "";
				rating = (int) training.instance(i).value(
						training.attribute("movie_rating"));
			} catch (NumberFormatException nfe) {
				continue;
			} catch (ArrayIndexOutOfBoundsException aiobe) {
				continue;
			}

			double count = count_user_appearance.containsKey(uid) ? count_user_appearance
					.get(uid) : 0;
			count_user_appearance.put(uid, count + 1);

			double r = rating_from_user.containsKey(uid) ? count_user_appearance
					.get(uid) : 0;
			rating_from_user.put(uid, r + rating);
		}

		for (String u : count_user_appearance.keySet()) {
			double rt = rating_from_user.get(u);
			double c = count_user_appearance.get(u);

			count_user_appearance.put(u, rt / c);
		}
	}
}
