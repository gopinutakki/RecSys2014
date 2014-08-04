import java.io.File;
import java.io.IOException;

import weka.core.Instances;
import weka.core.converters.ArffLoader;
import weka.core.converters.ArffSaver;

public class Partition {

	/**
	 * @param args
	 * @throws IOException
	 */
	public static void main(String[] args) throws IOException {
		ArffLoader tr = new ArffLoader();
		// tr.setSource(new File(
		// "C:\\Users\\WKUUSER\\Documents\\RecSys2014\\dataset\\dataset_recsys - Copy\\dataset_recsys_Mahsa2.arff"));
		tr.setSource(new File(args[0]));	
		Instances data = tr.getDataSet();
		Instances newTweets = new Instances(data);
		Instances reTweets = new Instances(data);

		for (int i = 0; i < newTweets.numInstances(); ) {
			String ts1 = newTweets.instance(i).stringValue(
					newTweets.attribute("retweeted_status-created_at"));

			if (ts1.equals("") || ts1.contains("?")) {
				newTweets.delete(i);
			}else
				i++;
		}

		System.out.println(newTweets.numInstances());
		for (int i = 0; i < reTweets.numInstances();) {
			String ts1 = reTweets.instance(i).stringValue(
					reTweets.attribute("retweeted_status-created_at"));

			if (ts1.equals("") || ts1.contains("?")) {
				i++;
			}else{
				reTweets.delete(i);
			}
		}
		System.out.println(reTweets.numInstances());
		System.out.println(data.numInstances() + "-->"
				+ (newTweets.numInstances() + reTweets.numInstances()));
		
		writeInstances(reTweets, "_retweets.arff");
		writeInstances(newTweets, "_newtweets.arff");
		System.out.println("Done Writing to Disk!");
	}
	
	private static void writeInstances(Instances data, String fname) throws IOException {
		ArffSaver saver = new ArffSaver();
		saver.setInstances(data);
		saver.setFile(new File(fname));
		saver.writeBatch();
	}

}
