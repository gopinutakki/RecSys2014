import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import weka.core.Instances;
import weka.core.converters.ArffLoader;
import weka.core.converters.ArffSaver;

public class TestBreakerByUID {

	/**
	 * @param args
	 * @throws IOException
	 */
	public static void main(String[] args) throws IOException {
		if (args.length != 2) {
			System.out
					.println("USAGE: java -jar TestBreakerByUID.jar <training-set.arff> <test-set.arff>");
			return;
		}

		System.out
				.println("The attribute that points to the user id: 'twitter_user_id'");
		Instances training = loadTrainingARFF(args[0]);
		Instances testing = loadTrainingARFF(args[1]);

		ArrayList<String> training_uids = new ArrayList<String>();
		for (int i = 0; i < training.numInstances(); i++) {
			training_uids.add(training.instance(i).stringValue(
					training.attribute("twitter_user_id")));
		}

		Instances testing_inTR = new Instances(testing);
		Instances testing_notInTR = new Instances(testing);

		for (int i = 0; i < testing_inTR.numInstances();) {
			if (training_uids.contains(testing_inTR.instance(i).stringValue(
					testing_inTR.attribute("twitter_user_id")))) {
				i++;
			} else {
				testing_inTR.delete(i);
			}
		}

		for (int i = 0; i < testing_notInTR.numInstances();) {
			if (training_uids.contains(testing_notInTR.instance(i).stringValue(
					testing_notInTR.attribute("twitter_user_id")))) {
				testing_notInTR.delete(i);
			} else {
				i++;
			}
		}

		writeInstances(testing_inTR,
				args[1].replace(".arff", "_UIDs_In_Training.arff"));
		writeInstances(testing_notInTR,
				args[1].replace(".arff", "_UIDs_Not_In_Training.arff"));
	}

	public static Instances loadTrainingARFF(String fname) throws IOException {
		ArffLoader tr = new ArffLoader();
		tr.setSource(new File(fname));
		Instances data = tr.getDataSet();
		return data;
	}

	private static void writeInstances(Instances training, String fname)
			throws IOException {
		ArffSaver saver = new ArffSaver();
		saver.setInstances(training);
		saver.setFile(new File(fname));
		saver.writeBatch();
		System.out.println("Done writing: " + fname);
	}
}
