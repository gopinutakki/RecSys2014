import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

import weka.classifiers.functions.LinearRegression;
import weka.core.Instances;
import weka.core.converters.ArffLoader;

public class RegressionPerClass {

	/**
	 * @param args
	 * @throws Exception 
	 */
	public static void main(String[] args) throws Exception {
		String trname = "RegressionTraining.arff";
		String tename = "RegressionTesting.arff";

		Instances testing = loadTrainingARFF(tename);
		ArrayList<String> classes = new ArrayList<String>();
		
		System.out.println("Generating sub-class instances.");
		Instances training_2 = loadTrainingARFF(trname);
		Instances training_3 = loadTrainingARFF(trname);
		Instances training_4 = loadTrainingARFF(trname);

		for (int i = 0; i < training_2.numInstances();) {
			if (training_2.instance(i)
					.stringValue(training_2.attribute("engagement"))
					.equals("2")) {
				i++;
			} else
				training_2.delete(i);
		}
		training_2.deleteAttributeAt(training_2.attribute("engagement").index());
		training_2.setClass(training_2.attribute("engagement_regression"));
		System.out.println(training_2.numInstances());
		
		for (int i = 0; i < training_3.numInstances();) {
			if (training_3.instance(i)
					.stringValue(training_3.attribute("engagement"))
					.equals("3")) {
				i++;
			} else
				training_3.delete(i);
		}
		training_3.deleteAttributeAt(training_3.attribute("engagement").index());
		training_3.setClass(training_3.attribute("engagement_regression"));
		System.out.println(training_3.numInstances());
		
		for (int i = 0; i < training_4.numInstances();) {
			if (training_4.instance(i)
					.stringValue(training_4.attribute("engagement"))
					.equals("4")) {
				i++;
			} else
				training_4.delete(i);
		}
		training_4.deleteAttributeAt(training_4.attribute("engagement").index());
		training_4.setClass(training_4.attribute("engagement_regression"));
		System.out.println(training_4.numInstances());
		
		System.out.println("RM 1");
		LinearRegression reg_2 = new LinearRegression();
		reg_2.buildClassifier(training_2);
		
		System.out.println("RM 2");
		LinearRegression reg_3 = new LinearRegression();
		reg_3.buildClassifier(training_3);
		
		System.out.println("RM 3");
		LinearRegression reg_4 = new LinearRegression();
		reg_4.buildClassifier(training_4);
		
		for(int i = 0; i < testing.numInstances(); i++){
			classes.add(testing.instance(i).stringValue(testing.attribute("engagement")));
		}
		testing.deleteAttributeAt(testing.attribute("engagement").index());
		
		BufferedWriter bw = new BufferedWriter(new FileWriter("regression_predictions.csv"));
		bw.write("regression_engagement\n");
		for(int i = 0; i < classes.size(); i++){
			if(testing.instance(i).value(testing.attribute("engagement_regression")) == 0)
				bw.write("0\n");
			else if(testing.instance(i).value(testing.attribute("engagement_regression")) == 1)
				bw.write("1\n");
			else if(testing.instance(i).value(testing.attribute("engagement_regression")) == 2){
				bw.write(reg_2.classifyInstance(testing.instance(i)) + "\n");
			}else if(testing.instance(i).value(testing.attribute("engagement_regression")) == 3){
				bw.write(reg_3.classifyInstance(testing.instance(i)) + "\n");
			}else if(testing.instance(i).value(testing.attribute("engagement_regression")) == 4){
				bw.write(reg_4.classifyInstance(testing.instance(i)) + "\n");
			}			
		}
		bw.flush();
		bw.close();
	}

	public static Instances loadTrainingARFF(String fname) throws IOException {
		ArffLoader tr = new ArffLoader();
		tr.setSource(new File(fname));
		Instances data = tr.getDataSet();
		return data;
	}
}
