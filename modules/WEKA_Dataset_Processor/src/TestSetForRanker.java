import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

import weka.core.Attribute;
import weka.core.FastVector;
import weka.core.Instances;
import weka.core.converters.ArffLoader;
import weka.core.converters.ArffSaver;

public class TestSetForRanker {

	/**
	 * @param args
	 * @throws Exception
	 */
	public static void main(String[] args) throws Exception {
		String fname = "full2_larger_10_2_train+test+evaluation_FS_Sorted_eval.arff";
		Instances t = loadTrainingARFF(fname);

		t.insertAttributeAt(new Attribute("id", (FastVector) null), 0);

		BufferedReader br = new BufferedReader(new FileReader(
				"/home/gopi/RecSys2014/dataset/evaluation_only_id.txt"));

		for (int i = 0; i < t.numInstances(); i++) {
			t.instance(i).setValue(t.attribute("id"), br.readLine().trim());
		}
		br.close();

		t.sort(t.attribute("twitter_user_id"));

		BufferedWriter bw = new BufferedWriter(new FileWriter(
				"PILOTS_solution.csv"));
		for (int i = 0; i < t.numInstances(); i++) {
			// bw.write(t.instance(i).stringValue(
			// t.attribute("twitter_user_id"))
			// + "," + t.instance(i).stringValue(t.attribute("id")) + "\n");
			String s = t.instance(i).stringValue(t.attribute("id"));
			bw.write(t.instance(i).stringValue(t.attribute("twitter_user_id"))
					+ ",\"" + s + "\"\n");
		}
		bw.flush();
		bw.close();

		writeARFF(t, fname.replace(".arff", "_2.arff"));
		writeTrainingSVM(fname.replace(".arff", "_2.arff"));
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
