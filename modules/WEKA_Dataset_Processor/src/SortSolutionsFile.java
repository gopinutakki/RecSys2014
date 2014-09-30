import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

public class SortSolutionsFile {

	/**
	 * @param args
	 * @throws IOException
	 */
	public static void main(String[] args) throws IOException {

		BufferedReader br = new BufferedReader(new FileReader(
				"PILOTS_solution.csv"));
		String line = "";

		ArrayList<Row> rows = new ArrayList<Row>();
		while ((line = br.readLine()) != null) {
			String[] tokens = line.split(",");
			rows.add(new Row(Long.parseLong(tokens[0]), Long
					.parseLong(tokens[1]), Integer.parseInt(tokens[2])));
		}
		br.close();
		
		Collections.sort(rows, new Comparator<Row>() {

			@Override
			public int compare(Row u1, Row u2) {
				if (u2.uid > u1.uid) {
					return 1;
				} else if (u2.uid < u1.uid) {
					return -1;
				} else {
					if (u2.eng > u1.eng) {
						return 1;
					} else if (u2.eng < u1.eng) {
						return -1;
					} else {
						if (u2.tid > u1.tid) {
							return 1;
						} else if (u2.tid < u1.tid) {
							return -1;
						}
					}
				}
				return 0;
			}
		});
		
		BufferedWriter bw = new BufferedWriter(new FileWriter("solution_PILOTS.dat"));
		for(Row row : rows){
			bw.write(row.uid + "," + row.tid + "," + row.eng + "\n");
		}
		bw.close();
	}

}

class Row {
	long uid;
	long tid;
	int eng;

	public Row(long u, long t, int e) {
		this.uid = u;
		this.tid = t;
		this.eng = e;
	}
}