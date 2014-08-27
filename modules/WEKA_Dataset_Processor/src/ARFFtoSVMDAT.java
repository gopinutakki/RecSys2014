import java.io.File;
import java.io.IOException;

import weka.core.Instances;
import weka.core.converters.ArffLoader;
import weka.core.converters.ArffSaver;
import weka.core.converters.CSVSaver;
import weka.core.converters.SVMLightSaver;

public class ARFFtoSVMDAT {

	/**
	 * @param args
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException {
		String trainingDataSource = args[0];
		Instances training = loadTrainingARFF(trainingDataSource);
		
		training.sort(training.attribute("twitter_user_id"));
		
		SVMLightSaver saver = new SVMLightSaver();
	    saver.setInstances(training);
	    saver.setFile(new File(args[0].split(".")[0]+".dat"));
	    saver.setDestination(new File(args[0].split(".")[0]+".dat"));
	    saver.writeBatch();
	}

	public static Instances loadTrainingARFF(String fname) throws IOException {
		ArffLoader tr = new ArffLoader();
		tr.setSource(new File(fname));
		Instances data = tr.getDataSet();
		return data;
	}
}
