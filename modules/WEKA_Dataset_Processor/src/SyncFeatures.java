import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import weka.core.Instances;
import weka.core.converters.ArffLoader;
import weka.core.converters.ArffSaver;

public class SyncFeatures {

	/**
	 * @param args
	 * @throws IOException
	 */
	public static void main1(String[] args) throws IOException {
		String tr = "/home/gopi/RecSys2014/modules/WEKA_Dataset_Processor/full2_larger_10_2_train+test_FS.arff";
		String ev = "/home/gopi/RecSys2014/modules/WEKA_Dataset_Processor/full2_larger_10_evaluation.arff";

		Instances t = loadTrainingARFF(tr);
		Instances e = loadTrainingARFF(ev);

		ArrayList<String> tFeatures = new ArrayList<String>();
		for (int i = 0; i < t.numAttributes(); i++) {
			tFeatures.add(t.attribute(i).name());
		}

		for (int i = 0; i < e.numAttributes();) {
			if (tFeatures.contains(e.attribute(i).name())) {
				i++;
			} else
				e.deleteAttributeAt(i);
		}

		// writeInstances(t, tr.replace(".arff", "_synced.arff"));
		writeInstances(e, ev.replace(".arff", "_FS.arff"));
	}

	
	public static void main(String[] args) throws IOException {
		String tr = "/home/gopi/RecSys2014/modules/WEKA_Dataset_Processor/full2_larger_10_2_train+test_FS.arff";
		String ev = "/home/gopi/RecSys2014/modules/WEKA_Dataset_Processor/full2_larger_10_evaluation.arff";

		Instances t = loadTrainingARFF(tr);
		Instances e = loadTrainingARFF(ev);

		ArrayList<String> tFeatures = new ArrayList<String>();
		
		for (int i = 0; i < t.numAttributes(); i++) {
			tFeatures.add(t.attribute(i).name());
		}

		writeInstances(e, ev.replace(".arff", "_FS.arff"));
	}
	
	public static void main2(String[] args) throws IOException {
		String tr = "/home/gopi/RecSys2014/modules/WEKA_Dataset_Processor/full2_larger_10_2_train+test_FS.arff";
		String ev = "/home/gopi/RecSys2014/modules/WEKA_Dataset_Processor/full2_larger_10_evaluation.arff";

		Instances t = loadTrainingARFF(tr);
		Instances e = loadTrainingARFF(ev);

		// for(int i = 0; i < 10; i++){
		// System.out.println(t.attribute(i).name() + t.attribute(i).type());
		// }

		Instances e2 = new Instances(t);
		for (int i = e.numInstances(); i == t.numInstances(); i--) {
			e2.delete(i);
		}

		for (int i = 0; i < e2.numInstances(); i++) {
			for (int a = 0; a < e2.numAttributes(); a++) {

				try {
					if (e2.attribute(a).type() == 0)
						e2.instance(i).setValue(
								a,
								e.instance(i).value(
										e.attribute(e2.attribute(a).name())));
					else
						e2.instance(i).setValue(
								a,
								e.instance(i).stringValue(
										e.attribute(e2.attribute(a).name())));
				} catch (Exception expec) {
					e2.instance(i).setValue(a, "?");
				}
			}
		}

		writeInstances(e, ev.replace(".arff", "_FS.arff"));
	}

	private static void writeInstances(Instances training, String fname)
			throws IOException {
		ArffSaver saver = new ArffSaver();
		saver.setInstances(training);
		saver.setFile(new File(fname));
		saver.writeBatch();
	}

	public static Instances loadTrainingARFF(String fname) throws IOException {
		ArffLoader tr = new ArffLoader();
		tr.setSource(new File(fname));
		Instances data = tr.getDataSet();
		return data;
	}
}
