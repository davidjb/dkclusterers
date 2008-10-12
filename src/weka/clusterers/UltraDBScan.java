package weka.clusterers;

import weka.clusterers.forOPTICSAndDBScan.Databases.Database;
import weka.clusterers.forOPTICSAndDBScan.Databases.SequentialDatabase;
import weka.clusterers.forOPTICSAndDBScan.DataObjects.EuclidianDataObject;
import weka.clusterers.forOPTICSAndDBScan.DataObjects.DataObject;

import weka.core.*;
import weka.filters.unsupervised.attribute.ReplaceMissingValues;
import weka.filters.unsupervised.attribute.PotentialClassIgnorer;
import weka.filters.Filter;

import java.util.*;

/**
 * UltraDBScan algorithm - to infinity and beyond!
 */
public class UltraDBScan extends AbstractClusterer implements OptionHandler {


	/**
	 * Keep track of the number of points within the current cluster to manipulate the
	 * epsilon value to check for different distances.
	 */
	private int currentClusterCounter = 0;

	/**
	 * Maintain a counter for watching what the current instance of data
	 * is (as instances get their cluster information returned to WEKA). 
	 */
	private int processedInstanceIdentifer = 0;

	/**
	 * Specify the identifier for the clusters, incremented as new clusters are found
	 */
	private int clusterIdentifier = 0;

	/**
	 * Maintain the total number of clusters that have been found
	 */
	private int totalClusters = 0;

	/**
	 * Storage for the database of instances, used for clustering
	 */
    private Database clustererDatabase;

    /**
     * Value for the distance to consider around a given data point to find clusters.
     *
     * This value is modifiable by the user because of the JavaBean methods given below.
	 * The 'original' value is used as a backup, for when we need to replace the changed value.
     */
    private double epsilonDistance = 0.9;
	private double epsilonDistanceOriginal = 0.9;

    /**
     * Value for the minimum number of points that need to be found for data to be
     * considered a cluster.
     *
     * This value is modifiable by the user because of the JavaBean methods given below.
     */
    private int minimumPointClusterThreshold = 6;
	private int minimumPointClusterThresholdOriginal = 6;

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

	/**
     * Returns tip text for this property (for Explorer/Experimenter GUI)
     * @return tooltip text for the epsilonDistance property
     */
    public String epsilonDistanceTipText() {
        return "Distance to consider around data for clusters";
    }

    /**
     * Return the distance that needs to be checked around a data node for cluster points
     * @return the distance to check around a given data node
     */
    public double getEpsilonDistance() {
        return epsilonDistance;
    }

    /**
     * Set the distance that needs to be checked around a data node for cluster points
     * @param epsilonDistance the distance to check around a given data node
     */
    public void setEpsilonDistance(double epsilonDistance) {
        this.epsilonDistance = epsilonDistance;
		this.epsilonDistanceOriginal = epsilonDistance;
    }

    /**
     * Returns tip text for this property (for Explorer/Experimenter GUI)
     * @return tooltip text for the minimumPointClusterThreshold property
     */
    public String minimumPointClusterThresholdTipText() {
        return "Minimum number of points to consider cluster";
    }

    /**
     * Return the smallest number of points necessary to make a cluster
     * @return the minimum number of points needed for a cluster
     */
    public int getMinimumPointClusterThreshold() {
        return minimumPointClusterThreshold;
    }

    /**
     * Set the minimum number of points necessary to make a cluster
     * @param minimumPointClusterThreshold the minimum number of points needed for a cluster
     */
    public void setMinimumPointClusterThreshold(int minimumPointClusterThreshold) {
        this.minimumPointClusterThreshold = minimumPointClusterThreshold;
		this.minimumPointClusterThresholdOriginal = minimumPointClusterThreshold;
    }

    public String globalInfo() {
        return "A customised implementation of the DBScan algorithm, with a few changes to see what effect they have on clusters";
    }


    /**
     * Returns the number of clusters.
     *
     * @return the number of clusters generated for a training dataset.
     * @throws Exception if number of clusters could not be returned
     *                   successfully
     */
    public int numberOfClusters() throws Exception {
        return this.totalClusters;
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
                new Option("Minimum point threshold", "minpoint", 1, "-minpoint <integer>")
        );

		optionVector.addElement(
                new Option("Epsilon distance", "epsilon", 1, "-epsilon <double>")
        );

		optionVector.addElement(
                new Option("Enable customisations", "custom", 1, "-custom <boolean>")
        );

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
        String currentOption = Utils.getOption("minpoint", options);
        this.minimumPointClusterThreshold = Integer.valueOf(currentOption);

        currentOption = Utils.getOption("epsilon", options);
        this.epsilonDistance = Double.valueOf(currentOption);

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

        optionsVector.add("-minpoint");
        optionsVector.add(Integer.toString(getMinimumPointClusterThreshold()));
        optionsVector.add("-epsilon");
        optionsVector.add(Double.toString(getEpsilonDistance()));
		optionsVector.add("-custom");
        optionsVector.add(Boolean.toString(getUseCustomisations()));

        return optionsVector.toArray(new String[optionsVector.size()]);
    }

     /**
     * Return the description of this clusterer
     *
     * @return a string representation of state of this clusterer
     */
    public String toString() {

        String outputString = "";
		outputString += ("UltraDBScan Clustering Output" +
						 "----------------------------\n");
		outputString += ("Attributes: \t" + this.clustererDatabase.getInstances().numAttributes() + "\n");
		outputString += "\n";
		outputString += ("Epsilon: " + this.epsilonDistance + "\n");
		outputString += ("Cluster Point Threshold: " + this.minimumPointClusterThreshold + "\n");
		outputString += ("Use customisations? " + this.useCustomisations + "\n");
		 outputString += "\n";

		outputString += ("Time Taken: \t" + Long.toString(lastClusteringDuration) + " ms ("+ Double.toString(lastClusteringDuration/1000.0) + " secs)\n");
		outputString += ("Count of Original Instances: \t" + this.clustererDatabase.getInstances().numInstances() + "\n");
		outputString += ("Count of Clustered Instances: \t" + this.clustererDatabase.size() + "\n");
		outputString += ("Total Clusters Found: \t" + Integer.toString(totalClusters) + "\n");
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

		this.currentClusterCounter = 0;
		this.clusterIdentifier = 0;
		this.epsilonDistance = this.epsilonDistanceOriginal;
		this.minimumPointClusterThreshold = this.minimumPointClusterThresholdOriginal;

		//Handle any instances with missing data by removing them entirely
		//We don't try and disguise the fact we can't deal with these bad records.
      	PotentialClassIgnorer missingValuesFilter = new ReplaceMissingValues();
		//Set the structure according to the inputted data set of isntances
        missingValuesFilter.setInputFormat(data);
		//Carry out the filtering and now we have our data set minus any bad values
        Instances filteredData = Filter.useFilter(data, missingValuesFilter);

        this.clustererDatabase = new SequentialDatabase(filteredData);

		//Add all instances into the database
		for(int i = 0; i < filteredData.numInstances(); i++) {
			DataObject createdDataObject = new EuclidianDataObject(filteredData.instance(i), Integer.toString(i), this.clustererDatabase);
			this.clustererDatabase.insert(createdDataObject);
		}

		//Walk through the instances in the database and set up their values
		this.clustererDatabase.setMinMaxValues();

		Iterator dataObjectIterator = this.clustererDatabase.dataObjectIterator();

		//Start timing the algorithm
        long startTime = System.currentTimeMillis();

        while (dataObjectIterator.hasNext()) {

			DataObject currentDataObject = (DataObject)dataObjectIterator.next();

			//Need to check if point already visited
			if (currentDataObject.getClusterLabel() == DataObject.UNCLASSIFIED) {
				
				List currentRangeSearch = this.clustererDatabase.epsilonRangeQuery(this.epsilonDistance, currentDataObject);
				if (currentRangeSearch.size() >= this.minimumPointClusterThreshold) {

					currentDataObject.setClusterLabel(this.clusterIdentifier);
					this.currentClusterCounter++;

					//need to recurse to all elements in the list
					this.recurseToNeighbours(currentRangeSearch, clusterIdentifier);

					if (this.currentClusterCounter >= this.minimumPointClusterThreshold)
						this.clusterIdentifier++; //Increment for next data object's cluster

					this.currentClusterCounter = 0;

					//If customising the process, reset the cluster counter as we are moving on
					if (this.useCustomisations) {
						this.epsilonDistance = this.epsilonDistanceOriginal;
					}

				} else {
					currentDataObject.setClusterLabel(DataObject.NOISE);
				}
			}
		}

        this.lastClusteringDuration = (System.currentTimeMillis() - startTime);

		this.totalClusters = clusterIdentifier;
    }

	/**
	 * Rescursively progresses through the list of data objects to find other cluster members
	 * @param inputRangeSearch the list of data objects being searched through
	 * @param clusterIdentifier the current cluster identification integer
	 */
	private void recurseToNeighbours(List inputRangeSearch, int clusterIdentifier) {

		Iterator currentIterator = inputRangeSearch.iterator();
		
		while (currentIterator.hasNext()) {

			DataObject currentDataObject = (DataObject)currentIterator.next();

			//Need to check if point already visited
			if (currentDataObject.getClusterLabel() != clusterIdentifier) {

				//Set the cluster label as provided
				currentDataObject.setClusterLabel(clusterIdentifier);

				this.currentClusterCounter++;

				//Need to recurse to all of this object's nearest neighbours
				List currentRangeSearch = this.clustererDatabase.epsilonRangeQuery(this.epsilonDistance, currentDataObject);
				this.recurseToNeighbours(currentRangeSearch, clusterIdentifier);

			}
		}
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
      currentCapabilities.enable(Capabilities.Capability.MISSING_VALUES); //Can't handle

      return currentCapabilities;
    }

    public static void main(String[] args) {
        runClusterer(new UltraDBScan(), args);
    }
}
