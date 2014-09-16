import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;

import com.sun.xml.internal.ws.message.EmptyMessageImpl;

import weka.core.Attribute;
import weka.core.Instances;
import weka.core.converters.ArffLoader;
import weka.core.converters.ArffSaver;
import weka.core.converters.SVMLightSaver;
import weka.filters.Filter;
import weka.filters.unsupervised.attribute.ReplaceMissingValues;
import weka.filters.unsupervised.instance.RemoveWithValues;

public class TestMergerForRanker {

	/**
	 * @param args
	 * @throws Exception
	 */
	public static void main(String[] args) throws Exception {
		// String path =
		// "C:\\Users\\WKUUSER\\Documents\\RecSys2014\\dataset\\TestMergerForRanker\\";

		String path = "/home/gopi/RecSys2014/dataset/TestMergerForRanker/";

		String tr = path + "full_60_training.arff";
		String tin = path + "full_60_01_testing_UIDs_In_Training.arff";
		String tnotin = path + "full_60_01_testing_UIDs_Not_In_Training.arff";

		// attribute --> 'predicted engagement'
		String pin = path + "predictions_WITH_UIDS_adaboost.arff";
		String pnotin = path + "predictions_WITHOUT_UIDS.arff";

		writeTrainingSVM(tr);
		mergeTesting(tin, tnotin, pin, pnotin);
		System.out.println("DONE!");

	}

	private static void mergeTesting(String tin, String tnotin, String pin,
			String pnotin) throws Exception {

		Instances testIn = loadTrainingARFF(tin);
		Instances testNotIn = loadTrainingARFF(tnotin);
		Instances predictionsIn = loadTrainingARFF(pin);
		Instances predictionsNotIn = loadTrainingARFF(pnotin);

		testIn.insertAttributeAt(
				predictionsIn.attribute("predicted engagement"),
				testIn.numAttributes());

		for (int i = 0; i < testIn.numInstances(); i++) {
			testIn.instance(i).setValue(
					testIn.attribute("predicted engagement"),
					(predictionsIn.instance(i).stringValue(predictionsIn
							.attribute("predicted engagement"))));
		}

		testNotIn.insertAttributeAt(
				predictionsNotIn.attribute("predicted engagement"),
				testNotIn.numAttributes());

		for (int i = 0; i < testNotIn.numInstances(); i++) {
			testNotIn.instance(i).setValue(
					testNotIn.attribute("predicted engagement"),
					(predictionsNotIn.instance(i).stringValue(predictionsNotIn
							.attribute("predicted engagement"))));
			testIn.add(testNotIn.instance(i));
		}

		testIn.setClass(testIn.attribute("predicted engagement"));
		testIn.deleteAttributeAt(testIn.attribute("engagement").index());
		writeTestingSVM(testIn, tin);

		breakWrittenTestingSVM(tin.replace(".arff", "_SVM.dat"));
	}

	private static void breakWrittenTestingSVM(String fname) throws IOException {
		BufferedReader br = new BufferedReader(new FileReader(fname));
		BufferedWriter bw0 = new BufferedWriter(new FileWriter(fname.replace(
				"_SVM.dat", "_SVM_0.dat")));
		BufferedWriter bw1 = new BufferedWriter(new FileWriter(fname.replace(
				"_SVM.dat", "_SVM_1.dat")));

		String line = "";
		while ((line = br.readLine()) != null) {
			if (line.startsWith("0"))
				bw0.write(line.trim() + "\n");

			if (line.startsWith("1"))
				bw1.write(line.trim() + "\n");
		}

		bw0.close();
		bw1.close();
		br.close();
		System.out.println("Done splitting.");
	}

	private static void writeARFF(Instances t, String fname) throws IOException {
		ArffSaver saver = new ArffSaver();
		saver.setInstances(t);
		saver.setFile(new File(fname));
		saver.writeBatch();
		System.out.println("Done writing [temp]: " + fname);
	}

	private static void writeTestingSVM(Instances t, String tin)
			throws Exception {

		writeARFF(t, "temp_forRanker.arff");
		writePartedARFF(t);

		ArrayList<String> uids = new ArrayList<String>();
		ArrayList<String> target = new ArrayList<String>();
		t.sort(t.attribute("twitter_user_id"));
		for (int i = 0; i < t.numInstances(); i++) {
			uids.add(t.instance(i).stringValue(t.attribute("twitter_user_id")));
			target.add(t.instance(i).value(t.attribute("predicted engagement"))
					+ "");
		}

		t.deleteAttributeAt(t.attribute("twitter_user_id").index());
		t.deleteAttributeAt(t.attribute("id").index());

		writeSVM(t, "predicted engagement", "temp.dat");
		appendTargetUID(target, uids, t, tin.replace(".arff", "_SVM.dat"));
	}

	private static void writePartedARFF(Instances t) throws IOException {
		Instances t0 = new Instances(t);
		Instances t1 = new Instances(t);

		for (int i = 0; i < t0.numInstances();) {
			if (t0.instance(i)
					.stringValue(t0.attribute("predicted engagement"))
					.equals("0")) {
				i++;
			} else
				t0.delete(i);
		}
		t0.sort(t0.attribute("twitter_user_id"));
		writeARFF(t0, "temp_forRanker_0.arff");

		for (int i = 0; i < t1.numInstances();) {
			if (t1.instance(i)
					.stringValue(t1.attribute("predicted engagement"))
					.equals("0")) {
				t1.delete(i);
			} else
				i++;
		}
		t1.sort(t1.attribute("twitter_user_id"));
		writeARFF(t1, "temp_forRanker_1.arff");
	}

	private static void writeTrainingSVM(String tr) throws Exception {
		ArrayList<String> uids = new ArrayList<String>();
		ArrayList<String> target = new ArrayList<String>();
		Instances t = loadTrainingARFF(tr);
		t.sort(t.attribute("twitter_user_id"));
		for (int i = 0; i < t.numInstances(); i++) {
			uids.add(t.instance(i).stringValue(t.attribute("twitter_user_id")));
			target.add(t.instance(i).value(t.attribute("engagement")) + "");
		}

		writeARFF(t, "temp.arff");
		t.deleteAttributeAt(t.attribute("twitter_user_id").index());
		// t.deleteAttributeAt(t.attribute("engagement").index());
		t.deleteAttributeAt(t.attribute("id").index());

		writeSVM(t, "engagement", "temp.dat");
		appendTargetUID(target, uids, t, tr.replace(".arff", "_SVM.dat"));
	}

	private static void appendTargetUID(ArrayList<String> target,
			ArrayList<String> uids, Instances t, String tr) throws IOException {
		BufferedReader br = new BufferedReader(new FileReader("temp.dat"));
		BufferedWriter bw = new BufferedWriter(new FileWriter(tr));
		String line = "";
		int i = 0;
		while ((line = br.readLine()) != null) {
			String tokens[] = line.split(" ", 2);
			// bw.write(target.get(i) + " qid:" + uids.get(i) + " " +
			// line.trim() + "\n");
			bw.write(tokens[0] + " qid:" + uids.get(i) + " " + tokens[1] + "\n");
			i++;
		}
		bw.flush();
		bw.close();
		br.close();
		System.out.println("Done writing: " + tr);
	}

	public static void writeSVM(Instances t, String classAttr, String fname)
			throws Exception {

		// ReplaceMissingValues rmv = new ReplaceMissingValues();
		// rmv.setInputFormat(t);
		// t = Filter.useFilter(t, rmv);

		// SVMLightSaver saver = new SVMLightSaver();
		// saver.setInstances(t);
		// saver.setFile(new File(fname));
		// saver.writeBatch();

		BufferedWriter bw = new BufferedWriter(new FileWriter(fname));
		String val = "";
		for (int i = 0; i < t.numInstances(); i++) {
			String l = t.instance(i).stringValue(t.attribute(classAttr));
			for (int a = 0; a < t.numAttributes(); a++) {
				val = t.instance(i).value(t.attribute(a)) + "";
				// if (val.equals("") || val.equals("?")) {
				// continue;
				// }

				if (val.equals("NaN"))
					continue;

				l += " " + a + ":" + val;
			}
			bw.write(l + "\n");
		}
		bw.flush();
		bw.close();
		System.out.println("Done writing [temp]: " + fname);
	}

	public static Instances loadTrainingARFF(String fname) throws IOException {
		ArffLoader tr = new ArffLoader();
		tr.setSource(new File(fname));
		Instances data = tr.getDataSet();
		return data;
	}
}
