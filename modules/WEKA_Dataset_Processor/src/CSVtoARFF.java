import java.io.File;
import java.io.IOException;

import weka.core.Instances;
import weka.core.converters.ArffLoader;
import weka.core.converters.ArffSaver;
import weka.core.converters.CSVLoader;


public class CSVtoARFF {
	
	public static void main(String args[]) throws IOException{
		
		String trainingDataSource = "/home/gopi/PycharmProjects/RecSys2014/dataset/ARFF/dataset_recsys.arff";
		Instances training = loadTrainingARFF(trainingDataSource);
				
		// load CSV
	    CSVLoader loader = new CSVLoader();
	    loader.setSource(new File(args[0]));
	    Instances data = loader.getDataSet();
	 
	    // save ARFF
	    ArffSaver saver = new ArffSaver();
	    saver.setInstances(data);
	    saver.setFile(new File(args[1]));
	    saver.setDestination(new File(args[1]));
	    saver.writeBatch();
	}
	
	public static Instances loadTrainingARFF(String fname) throws IOException{
		ArffLoader tr = new ArffLoader();
		tr.setSource(new File(fname));
		Instances data = tr.getDataSet();
		
		return data;
	}
}
