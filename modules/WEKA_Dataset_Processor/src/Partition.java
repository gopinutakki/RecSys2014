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
		tr.setSource(new File(
				"C:\\Users\\WKUUSER\\Documents\\RecSys2014\\dataset\\dataset_recsys - Copy\\dataset_recsys_Mahsa2.arff"));
		Instances data = tr.getDataSet();
		Instances newTweets = new Instances(data);
		Instances reTweets = new Instances(data);

		for (int i = 0; i < data.numInstances(); i++) {
			String ts1 = data.instance(i).stringValue(
					data.attribute("retweeted_status-created_at"));

			if (ts1.equals("") || ts1.contains("?")) {
				newTweets.delete(i);
			} else {
				reTweets.delete(i);
			}

		}

		// newTweets.deleteWithMissing(data.attribute("retweeted_status-created_at"));

		System.out.println(data.numInstances() + "-->"
				+ (newTweets.numInstances() + reTweets.numInstances()));
	}
}
