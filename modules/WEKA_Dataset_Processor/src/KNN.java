import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

import weka.classifiers.Classifier;
import weka.classifiers.lazy.IBk;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.converters.ArffLoader;
import weka.core.neighboursearch.LinearNNSearch;

public class KNN {
	public static Instances loadARFF(String fname) throws IOException {
		ArffLoader tr = new ArffLoader();
		tr.setSource(new File(fname));
		Instances data = tr.getDataSet();
		return data;
	}

	public static void main(String[] args) throws Exception {
		Instances training = loadARFF("full_60_training_reduced.arff");		
		Instances testing = loadARFF("full_60_testing_reduced.arff");

		training.setClassIndex(training.numAttributes()-1);
		testing.setClassIndex(testing.numAttributes()-1);
		
		
		LinearNNSearch knn = new LinearNNSearch();
		knn.setInstances(training);
//		IBk ibk = new IBk();
//		ibk.setOptions(new String[]{"-E", "-I"});
//		ibk.setKNN(3);
//		ibk.buildClassifier(training);
	
		
		BufferedWriter bw = new BufferedWriter(new FileWriter("knn_out.csv"));
		for(int i = 0; i < testing.numInstances(); i++){
//			double c = ibk.classifyInstance(testing.instance(i));
//			System.out.println("-->" + c + " : " + training.classAttribute().value((int)c));
			//System.out.println(knn.nearestNeighbour(testing.instance(i)).classValue() + "\t" + testing.instance(i).classValue());
			bw.write(knn.nearestNeighbour(testing.instance(i)).classValue() + "\t" + testing.instance(i).classValue() + "\n");
		}
		bw.flush();
		bw.close();
	}
}
