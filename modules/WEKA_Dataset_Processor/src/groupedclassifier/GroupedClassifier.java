package groupedclassifier;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import weka.classifiers.Classifier;
import weka.classifiers.meta.AdaBoostM1;
import weka.core.Instances;
import weka.core.converters.ArffLoader;

public class GroupedClassifier {

	static final HashMap<String, Classifier> groupedClassifiers = new HashMap<String, Classifier>();

	/**
	 * @param args
	 * @throws Exception
	 */
	public static void main(String[] args) throws Exception {

		final Instances training = loadTrainingARFF(args[0]);
		Instances testing = loadTrainingARFF(args[1]);

		testing.setClassIndex(testing.numAttributes() - 1);

		HashSet<String> trid = getUserIds(training);
		HashSet<String> trid_temp = getUserIds(training);
		HashSet<String> teid = getUserIds(testing);

		teid.retainAll(teid);
		System.out.println("Common: " + trid.size());
		trid_temp.removeAll(teid);
		trid.removeAll(trid_temp);
		
		// HashSet<String> uid = getUserIds(training);
		HashSet<String> uid = trid;

		System.out.println("Modeling for all users.");
		Classifier c = getClassifier(training);
		groupedClassifiers.put("FULL", c);

		System.out.println("Modeling for grouped users.");
		ExecutorService exec = Executors.newFixedThreadPool(Runtime
				.getRuntime().availableProcessors());
		try {
			for (final String u : uid) {
				exec.submit(new Runnable() {

					@Override
					public void run() {
						System.out.println("User: " + u);
						Instances uInstances = new Instances(training);
						uInstances = getUserInstances(uInstances, u);
						uInstances.setClass(uInstances.attribute("engagement"));
						try {
							groupedClassifiers
									.put(u, getClassifier(uInstances));
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
				});
			}
		} finally {
			exec.shutdown();
		}

		for (String u : uid) {
			Instances uInstances = new Instances(training);
			uInstances = getUserInstances(uInstances, u);
			uInstances.setClass(uInstances.attribute("engagement"));
			groupedClassifiers.put(u, getClassifier(uInstances));
		}

		System.out.println("Classifying the test set.");
		int count = 0;
		for (int i = 0; i < testing.numInstances(); i++) {
			if (!uid.contains(testing.instance(i).stringValue(
					testing.attribute("twitter_user_id")))) {
				System.out.println("Not found: "
						+ testing.instance(i).stringValue(
								testing.attribute("twitter_user_id")));
				c = groupedClassifiers.get("FULL");
			} else {
				c = groupedClassifiers.get(testing.instance(i).stringValue(
						testing.attribute("twitter_user_id")));
			}

			if (c.classifyInstance(testing.instance(i)) == testing.instance(i)
					.classValue()) {
				count++;
			}
		}

		System.out.println(count + " -/- " + testing.numInstances());
	}

	public static Instances getUserInstances(Instances t, String uid) {
		for (int i = 0; i < t.numInstances();) {
			if (t.instance(i).stringValue(t.attribute("twitter_user_id"))
					.equals(uid)) {
				i++;
			} else
				t.delete(i);
		}

		return t;
	}

	public static HashSet<String> getUserIds(Instances t) {
		HashSet<String> uid = new HashSet<String>();
		for (int i = 0; i < t.numInstances(); i++) {
			uid.add(t.instance(i).stringValue(t.attribute("twitter_user_id")));
		}
		return uid;
	}

	public static Classifier getClassifier(Instances t) throws Exception {
		t.setClass(t.attribute("engagement"));
		Classifier c = new AdaBoostM1();
		// -Q -P 100 -S 1 -I 10 -W weka.classifiers.trees.DecisionStump
		// -Q -P 100 -S 1 -I 100 -W weka.classifiers.trees.J48 -- -C 0.25 -M 2

		// String[] options = weka.core.Utils
		// .splitOptions("-Q -P 100 -S 1 -I 10 -W weka.classifiers.trees.DecisionStump");
		// c.setOptions(options);
		c.buildClassifier(t);
		return c;
	}

	public static Instances loadTrainingARFF(String fname) throws IOException {
		ArffLoader tr = new ArffLoader();
		tr.setSource(new File(fname));
		Instances data = tr.getDataSet();
		return data;
	}
}
