package weka.clusterers;

import weka.clusterers.forOPTICSAndDBScan.Databases.SequentialDatabase;
import weka.core.Instances;

/**
 * DKSequentialDatabase database structure - utilise the existing SequentialDatabase and
 * customise its functionality to suit our clusterers that we have.
 */
public class DKSequentialDatabase extends SequentialDatabase {

	/**
	 * Constructs a new sequential database and holds the original instances
	 *
	 * @param instances
	 */
	public DKSequentialDatabase(Instances instances) {
		super(instances);
	}


}
