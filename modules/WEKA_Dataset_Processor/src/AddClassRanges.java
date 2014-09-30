import java.io.File;
import java.io.IOException;

import weka.core.Attribute;
import weka.core.FastVector;
import weka.core.Instances;
import weka.core.converters.ArffLoader;
import weka.core.converters.ArffSaver;

public class AddClassRanges {

	/**
	 * @param args
	 * @throws IOException
	 */
	public static void main(String[] args) throws IOException {
		String tr = "full2_larger_10_2_train+test+evaluation_FS_Sorted.arff";
		Instances training = loadTrainingARFF(tr);
		training.insertAttributeAt(new Attribute("engagement_range",
				(FastVector) null), training.numAttributes());

		for (int i = 0; i < training.numInstances(); i++) {
			int val = (int) (training.instance(i).value(training.attribute(
					"engagement").index()));

			System.out.println(val);
			if (val == 0) {
				training.instance(i).setValue(
						training.attribute("engagement_range"), "a");
			} else if (val == 1) {
				training.instance(i).setValue(
						training.attribute("engagement_range"), "b");
			} else if (val < 50) {
				training.instance(i).setValue(
						training.attribute("engagement_range"), "d");
			} else {
				training.instance(i).setValue(
						training.attribute("engagement_range"), "e");
			}
		}

		writeARFF(training, tr.replace(".arff", "_RANGE.arff"));
	}

	public static void writeARFF(Instances t, String fname) throws IOException {
		ArffSaver saver = new ArffSaver();
		saver.setInstances(t);
		saver.setFile(new File(fname));
		saver.writeBatch();
		System.out.println("Done writing: " + fname);
	}

	public static Instances loadTrainingARFF(String fname) throws IOException {
		ArffLoader tr = new ArffLoader();
		tr.setSource(new File(fname));
		Instances data = tr.getDataSet();
		return data;
	}
}
