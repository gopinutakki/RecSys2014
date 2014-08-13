import java.io.File;
import java.io.IOException;

import weka.core.Instances;
import weka.core.converters.ArffLoader;
import weka.core.converters.ArffSaver;
import weka.filters.Filter;
import weka.filters.unsupervised.instance.RemoveRange;

public class Break {

	/**
	 * @param args
	 * @throws Exception
	 */
	public static void main(String[] args) throws Exception {
		if (args.length == 0) {
			System.out
					.println("\nUSAGE: java -Xmx4096m -jar Break.jar <ARFF file>"
							+ "\nor\n"
							+ "USAGE: java -Xmx4096m -jar Break.jar <ARFF file> <Number of Training Instances (default: 170285)>");
			return;
		}

		String fname = args[0];
		int trainingCount = 170285;
		if (args.length > 1)
			trainingCount = Integer.parseInt(args[1]);

		System.out.println("Loading the file....");
		Instances training = loadTrainingARFF(fname);
		System.out.println("Total instances read from the file: "
				+ training.numInstances());
		Instances testing = new Instances(training);
		System.out.println("Breaking the file into Training and Testing.");

		// Training
		RemoveRange tm1 = new RemoveRange();
		tm1.setInputFormat(training);
		tm1.setInstancesIndices((trainingCount + 1) + "-last");
		training = Filter.useFilter(training, tm1);

		// Testing
		RemoveRange tm2 = new RemoveRange();
		tm2.setInputFormat(testing);
		tm2.setInstancesIndices("first-" + trainingCount);
		testing = Filter.useFilter(testing, tm2);

		writeARFF(training, fname.replace(".arff", "_training.arff"));
		writeARFF(testing, fname.replace(".arff", "_testing.arff"));
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
}
