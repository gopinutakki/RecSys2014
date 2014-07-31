import java.io.File;
import java.io.IOException;

import weka.core.Instances;
import weka.core.converters.ArffLoader;

public class Partition {

	/**
	 * @param args
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException {
		ArffLoader tr = new ArffLoader();
		tr.setSource(new File(""));
		Instances data = tr.getDataSet();
		
		for(int i = 0; i < data.numInstances(); i++){
			
		}
	}
}
