package weka.clusterers;

import weka.clusterers.forOPTICSAndDBScan.Databases.Database;
import weka.clusterers.forOPTICSAndDBScan.Databases.SequentialDatabase;
import weka.clusterers.forOPTICSAndDBScan.DataObjects.EuclidianDataObject;
import weka.clusterers.forOPTICSAndDBScan.DataObjects.DataObject;

import weka.core.*;
import weka.filters.unsupervised.attribute.PotentialClassIgnorer;
import weka.filters.unsupervised.attribute.ReplaceMissingValues;
import weka.filters.Filter;

import java.util.*;

/**
 * UltraKMedoids algorithm - to infinity and beyond!
 */
public class UltraKMedoids extends RandomizableClusterer implements OptionHandler {




	/**
	 * Maintain the medoids of clusters that have been found
	 */
	private List<Integer> medoidList;

	/**
	 * Storage for the database of instances, used for clustering
	 */
    private Database clustererDatabase;

    /**
     * Value for the number of clusters that should be located.
     *
     * This value is modifiable by the user because of the JavaBean methods given below.
     */
    private int selectedClusterCount = 3;
	private int selectedClusterCountOriginal = 3;

    /**
     * Value used to store the length of the last clustering operation.  This is used
     * to output the time taken to the WEKA interface.
     */
    private long lastClusteringDuration;

	/**
	 * Value used to determine if algorithm customisations should be used
	 */
	private boolean useCustomisations = false;

	/**
	 * Maintain a counter for watching what the current instance of data
	 * is (as instances get their cluster information returned to WEKA).
	 */
	private int processedInstanceIdentifer = 0;

	/**
     * Returns tip text for this property (for Explorer/Experimenter GUI)
     * @return tooltip text for the minimumPointClusterThreshold property
     */
    public String selectedClusterCountTipText() {
        return "Number of clusters to consider (k-medoids)";
    }

    /**
     * Return the selected number of clusters to obtain from data set
     * @return the minimum number of points needed for a cluster
     */
    public int getSelectedClusterCount() {
        return this.selectedClusterCount;
    }

    /**
     * Set the selected number of clusters to obtain from data set
     * @param selectedClusterCount the number of clusters to examine for
     */
    public void setSelectedClusterCount(int selectedClusterCount) {
        this.selectedClusterCount = selectedClusterCount;
		this.selectedClusterCountOriginal = selectedClusterCount;
    }

	/**
	 * Get the value used to set whether or not to use customisations
	 * @return whether or not to use the customisations to the algorithm
	 */
	public boolean getUseCustomisations() {
		return useCustomisations;
	}

	/**
	 * Set the value used to determine whether or not to use customisations
	 * @param useCustomisations the input value to set whether or not to use customisations
	 */
	public void setUseCustomisations(boolean useCustomisations) {
		this.useCustomisations = useCustomisations;
	}

	/**
     * Returns tip text for this property (for Explorer/Experimenter GUI)
     * @return tooltip text for the minimumPointClusterThreshold property
     */
    public String useCustomisationsTipText() {
        return "Whether or not to invoke algorithm customisations";
    }

    public String globalInfo() {
        return "A customised implementation of the K-Medoids algorithm, with a few changes to see what effect they have on clusters";
    }


    /**
     * Returns the number of clusters.
     *
     * @return the number of clusters generated for a training dataset.
     * @throws Exception if number of clusters could not be returned
     *                   successfully
     */
    public int numberOfClusters() throws Exception {
        return this.medoidList.size();
    }

    /**
     * Returns the revision string.
     *
     * @return the revision
     */
    public String getRevision() {
        return RevisionUtils.extract("$Revision: 1.0.0 (Super Secret Sunset Special Edition) $");
    }

    /**
     * Returns an enumeration of all the available options..
     *
     * @return an enumeration of all available options.
     */
    public Enumeration listOptions() {
        Vector<Option> optionVector = new Vector<Option>();

        optionVector.addElement(
                new Option("Cluster count to search for", "clusters", 1, "-clusters <integer>")
        );

		optionVector.addElement(
                new Option("Enable customisations", "custom", 1, "-custom <boolean>")
        );

		//Put the parent's options into our listing
		Enumeration parentOptions = super.listOptions();
		while (parentOptions.hasMoreElements()) {
			Option currentOption = (Option)parentOptions.nextElement();
			optionVector.addElement(currentOption);
		}

        return optionVector.elements();  //To change body of implemented methods use File | Settings | File Templates.
    }

    /**
     * Sets the OptionHandler's options using the given list. All options
     * will be set (or reset) during this call (i.e. incremental setting
     * of options is not possible).
     *
     * @param options the list of options as an array of strings
     * @throws Exception if an option is not supported
     */
    public void setOptions(String[] options) throws Exception {

		//Set the parent's options by passing the same array in
		super.setOptions(options);

        String currentOption = Utils.getOption("clusters", options);
        this.selectedClusterCount = Integer.valueOf(currentOption);
		this.selectedClusterCountOriginal = Integer.valueOf(currentOption);

		currentOption = Utils.getOption("custom", options);
        this.useCustomisations = Boolean.valueOf(currentOption);

        //Use the Utils method here to get an option string back based upon the input.
        //System.out.println(currentOption);
    }

    /**
     * Gets the current option settings for the OptionHandler.
     *
     * @return the list of current option settings as an array of strings
     */
    public String[] getOptions() {

        Vector<String> optionsVector = new Vector<String>();

        optionsVector.add("-clusters");
        optionsVector.add(Integer.toString(getSelectedClusterCount()));
		optionsVector.add("-custom");
        optionsVector.add(Boolean.toString(getUseCustomisations()));


		//Get options for the parent class
		String[] parentOptions = super.getOptions();
		for(String currentString: parentOptions) {
			optionsVector.add(currentString);
		}

        return optionsVector.toArray(new String[optionsVector.size()]);
    }

     /**
     * Return the description of this clusterer
     *
     * @return a string representation of state of this clusterer
     */
    public String toString() {

        String outputString = "";
		outputString += ("UltraKMedoids Clustering Output" +
						 "----------------------------\n");
		outputString += ("Attributes: \t" + this.clustererDatabase.getInstances().numAttributes() + "\n");
		outputString += "\n";
		outputString += ("Clusters to find: " + this.selectedClusterCount + "\n");
		outputString += "\n";

		outputString += ("Time Taken: \t" + Long.toString(lastClusteringDuration) + " ms ("+ Double.toString(lastClusteringDuration/1000.0) + " secs)\n");
		outputString += ("Count of Original Instances: \t" + this.clustererDatabase.getInstances().numInstances() + "\n");
		outputString += ("Count of Clustered Instances: \t" + this.clustererDatabase.size() + "\n");
		outputString += ("Total Clusters Found: \t" + Integer.toString(this.medoidList.size()) + "\n");
		outputString += "\n";

		Iterator currentDataIterator = this.clustererDatabase.dataObjectIterator();
		int currentIndex = 0;

		//Step through all clusters and write out details
		while (currentDataIterator.hasNext()) {

			 DataObject currentDataObject = (DataObject)currentDataIterator.next();
			 outputString += ( "("+currentIndex+++") "+ currentDataObject.toString() + "\t==>\t" );

			 int currentClusterLabel = currentDataObject.getClusterLabel();
			 if (currentClusterLabel == DataObject.NOISE)
				 outputString += "NOISE";
			 else if (currentClusterLabel == DataObject.UNCLASSIFIED)
				 outputString += "UNCLASSIFIED";
			 else
			 	outputString += currentClusterLabel;

			 outputString += "\n";
        }

        return outputString;
    }

    /**
    * Classifies a given instance. Either this or distributionForInstance()
    * needs to be implemented by subclasses.
    *
    * @param instance the instance to be assigned to a cluster
    * @return the number of the assigned cluster as an integer
    * @exception Exception if instance could not be clustered
    * successfully
    */
    public int clusterInstance(Instance instance) throws Exception {
		//We don't use the instance passed in here as it is part of the function and unneeded

		//Pull out the current database DataObject based upon the current identifier index
		DataObject currentObject = this.clustererDatabase.getDataObject(Integer.toString(this.processedInstanceIdentifer));
		//Move onto the next object in the database
		this.processedInstanceIdentifer++;

		//Reset the processed instance identifier if we go over the end of the database (may need to re-cluster)
		if (this.processedInstanceIdentifer >= this.clustererDatabase.size()) {
			this.processedInstanceIdentifer = 0;
		}

		int currentClusterIdentifier = currentObject.getClusterLabel();
		if (currentClusterIdentifier == DataObject.NOISE || currentClusterIdentifier == DataObject.UNCLASSIFIED ||
			currentClusterIdentifier == DataObject.UNDEFINED) {
			throw new Exception("Can't cluster this instance");
		}

        return currentClusterIdentifier;
    }

    /**
     * Generates a clusterer. Has to initialize all fields of the clusterer
     * that are not being set via options.
     *
     * @param data set of instances serving as training data
     * @throws Exception if the clusterer has not been
     *                   generated successfully
    */
    public void buildClusterer(Instances data) throws Exception {

		//Check to see if our clusterer can handle the data
		this.getCapabilities().testWithFail(data);

		this.selectedClusterCount = this.selectedClusterCountOriginal;

		//Handle any instances with missing data by removing them entirely
		//We don't try and disguise the fact we can't deal with these bad records.
      	PotentialClassIgnorer missingValuesFilter = new ReplaceMissingValues();
		//Set the structure according to the inputted data set of isntances
        missingValuesFilter.setInputFormat(data);
		//Carry out the filtering and now we have our data set minus any bad values
        Instances filteredData = Filter.useFilter(data, missingValuesFilter);

		//Create the database to be used for the given instances
        this.clustererDatabase = new DKSequentialDatabase(data);

		//Add all instances into the database
		for(int i = 0; i < data.numInstances(); i++) {
			DataObject createdDataObject = new EuclidianDataObject(data.instance(i), Integer.toString(i), this.clustererDatabase);
			this.clustererDatabase.insert(createdDataObject);
		}

		//Step through the database and calculate distances between objects
		this.clustererDatabase.setMinMaxValues();

		//Create randomiser object and seed with user-provided seed number
		Random clustererRandomiser = new Random(this.getSeed());

		//Create k-length list for the medoids indices to be placed in
		this.medoidList = new ArrayList<Integer>(this.selectedClusterCount);

		//Start timing the algorithm
        long startTime = System.currentTimeMillis();

		//Select k many objects as aritrary medoid objects
		while (this.medoidList.size() < this.selectedClusterCount) {
			//Loop until we have enough items to satisfy the medoid count
			int randomisedInteger = clustererRandomiser.nextInt(this.clustererDatabase.size());

			//If the medoid list doesn't contain our randomly indexed data object, insert it
			if (!this.medoidList.contains(randomisedInteger)) {
				this.medoidList.add(randomisedInteger);
			}
		}

		//Continue looping until there is no change in the medoid assignments
		boolean hasChanged = true;
		while (hasChanged) {

			List<Integer> modifiedMedoidList = null;
			int closestMedoidToNonMedoid = 0;

			//Randomly select an object that is not a medoid to check for a change
			boolean isNonMedoidSelected = false;
			int selectedNonMedoidIndex = 0;
			while (isNonMedoidSelected == false) {
				int randomisedInteger = clustererRandomiser.nextInt(this.clustererDatabase.size());

				//If the medoid list doesn't contain our randomly indexed data object, choose it!
				if (!this.medoidList.contains(randomisedInteger)) {
					selectedNonMedoidIndex = randomisedInteger;
					isNonMedoidSelected = true;

					//Create the modified listing of medoids
					modifiedMedoidList = new ArrayList<Integer>(this.medoidList);
					//Calculate the originally closest medoid to the non-medoid object selected
					closestMedoidToNonMedoid = this.nearestMedoidSearch(selectedNonMedoidIndex, this.medoidList);
					//Make the change - change the medoid over in the temporary list
					modifiedMedoidList.set(closestMedoidToNonMedoid, selectedNonMedoidIndex);
				}
			}

			//Compute the total of distances for both original and modified cluster assignments
			//So, change the medoid over temporarily, when appropriate
			double originalTotalCost = 0;
			double modifiedTotalCost = 0;

			Iterator dataObjectIterator = this.clustererDatabase.dataObjectIterator();

			int currentIndex = 0;
			while (dataObjectIterator.hasNext()) {
				DataObject currentDataObject = (DataObject)dataObjectIterator.next();

				//Assign each remaining object to the cluster with nearest medoid
				//Note: if the object is a medoid, the distance should be 0 (hence allocated to itself)
				currentDataObject.setClusterLabel( this.nearestMedoidSearch(currentIndex, this.medoidList) );

				//Get the cluster assignment of current object, use that as an index for the medoidList
				//From that index, get the value for the key in the database, and then get the data object
				DataObject associatedMedoid = this.clustererDatabase.getDataObject(Integer.toString(this.medoidList.get(currentDataObject.getClusterLabel())));

				double calculatedDistance = currentDataObject.distance(associatedMedoid);
				originalTotalCost += calculatedDistance;

				if (!useCustomisations) {
					//Recompute clustering assignments for this point
					//The point may find its assignment stays the same, but we need to check for all points just in case
					int alternateMedoidIndex = this.nearestMedoidSearch(currentIndex, modifiedMedoidList);
					DataObject alternateMedoid = this.clustererDatabase.getDataObject(Integer.toString(modifiedMedoidList.get(alternateMedoidIndex)));
					modifiedTotalCost += currentDataObject.distance(alternateMedoid);

				} else if (useCustomisations) {
					if (currentDataObject.getClusterLabel() == closestMedoidToNonMedoid) {
						DataObject alternateMedoid = this.clustererDatabase.getDataObject(Integer.toString(selectedNonMedoidIndex));
						modifiedTotalCost += currentDataObject.distance(alternateMedoid);
					} else {
						modifiedTotalCost += calculatedDistance;
					}
				}

				//Move to the next index from the database
				currentIndex++;
			}

			//If the total distances of the modified set are less than the total of the original clusters
			if (modifiedTotalCost < originalTotalCost) {
				//Commit the change to the medoid, and repeat
				this.medoidList.set(closestMedoidToNonMedoid, selectedNonMedoidIndex);

				//System.err.println("Swap! " + closestMedoidToNonMedoid + " -> " + selectedNonMedoidIndex);

				//If the change is sufficiently suitable, then we can stop
				if (this.useCustomisations && true) {
					hasChanged = false;
				}
			} else {
				//Otherwise, set the flag to show no change was made, and stop processing
				hasChanged = false;
			}
		}

        this.lastClusteringDuration = (System.currentTimeMillis() - startTime);
    }


	/**
	 * Calculate the nearest medoid for a given data object using the clusterer database's distances.
	 * @param inputDataObjectIndex the index/key for the given data object in the clusterer database
	 * @return the index in the <b>medoidList</b> for the closest medoid
	 */
	public int nearestMedoidSearch(int inputDataObjectIndex, List<Integer> inputMedoidList) {

		//Get our current data object to check from the inputted index
		DataObject currentDataObject = this.clustererDatabase.getDataObject(Integer.toString(inputDataObjectIndex));
		
		double smallestDistance = -1;
		int closestMedoidIndex = -1;

		//Walk through all medoids in the current listing
		int medoidCounter = 0;
		for (Integer currentMedoidIndex: inputMedoidList) {
			//Obtain the medoid object back to compare its distance to the current object
			DataObject currentMedoidObject = this.clustererDatabase.getDataObject(Integer.toString(currentMedoidIndex));

			//Calculate the distance to the current object from the medoid
			double currentDistance = currentMedoidObject.distance(currentDataObject);

			//If uninitialised or the current distance is closer, then set this as the closest
			if (smallestDistance == -1 || currentDistance < smallestDistance) {
				smallestDistance = currentDistance;
				closestMedoidIndex = medoidCounter;
			}

			medoidCounter++;
		}
		
		return closestMedoidIndex;
	}

	/**
     * Obtain the capabilities (in terms of data attributes) that this clusterer can handle
     * @return the capabilities of this clusterer
     */
    public Capabilities getCapabilities() {

      Capabilities currentCapabilities = super.getCapabilities();   // returns the object from weka.classifiers.Classifier

      // Set the attributes that our clusterer can handle
      currentCapabilities.enable(Capabilities.Capability.NOMINAL_ATTRIBUTES);
      currentCapabilities.enable(Capabilities.Capability.NUMERIC_ATTRIBUTES);
      currentCapabilities.enable(Capabilities.Capability.DATE_ATTRIBUTES);
      currentCapabilities.enable(Capabilities.Capability.MISSING_VALUES);

      return currentCapabilities;
    }

    public static void main(String[] args) {
        runClusterer(new UltraDBScan(), args);
    }
}