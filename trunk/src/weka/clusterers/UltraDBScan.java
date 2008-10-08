package weka.clusterers;

import weka.clusterers.forOPTICSAndDBScan.*;
import weka.clusterers.forOPTICSAndDBScan.Databases.Database;
import weka.clusterers.forOPTICSAndDBScan.Databases.SequentialDatabase;
import weka.clusterers.forOPTICSAndDBScan.DataObjects.EuclidianDataObject;

import weka.core.*;

import java.util.Enumeration;
import java.util.List;
import java.util.LinkedList;
import java.util.Vector;

/**
 * UltraDBScan algorithm - to infinity and beyond!
 */
public class UltraDBScan extends AbstractClusterer implements OptionHandler {


    /**
     * Value for the distance to consider around a given data point to find clusters.
     *
     * This value is modifiable by the user because of the JavaBean methods given below.
     */
    private double epsilonDistance = 1.0;

    /**
     * Value for the minimum number of points that need to be found for data to be
     * considered a cluster.
     *
     * This value is modifiable by the user because of the JavaBean methods given below.
     */
    private int minimumPointClusterThreshold = 3;

    /**
     * Value used to store the length of the last clustering operation.  This is used
     * to output the time taken to the WEKA interface.
     */
    private long lastClusteringDuration;

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
        return 0;
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
                new Option("Minimum point threshold", "minpoint", 1, "-minpoint <int>")
        );

        optionVector.addElement(
                new Option("Epsilon distance", "epsilon", 1, "-epsilon <double>")
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

        //Use the Utils method here to get an option string back based upon the input.
        System.out.println(currentOption);
    }

    /**
     * Gets the current option settings for the OptionHandler.
     *
     * @return the list of current option settings as an array of strings
     */
    public String[] getOptions() {

        String[] optionsArray = new String[4];

        optionsArray[0] = "-minpoint";
        optionsArray[1] = Integer.toString(getMinimumPointClusterThreshold());
        optionsArray[2] = "-epsilon";
        optionsArray[3] = Double.toString(getEpsilonDistance());

        return optionsArray;
    }

     /**
     * Return the description of this clusterer
     *
     * @return a string representation of state of this clusterer
     */
    public String toString() {
         //TODO Make this print out details of clusters here

        String outputString = "";
        outputString += "Time Taken: \t" + Long.toString(lastClusteringDuration) + " ms";
        outputString += "\n";
        outputString += System.getProperty("java.class.path");
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
        //TODO Need to return the index of the cluster that this given instance is set as
        //Best idea is probably to have a dictionary or so forth that can ?
        return 0;
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

        //weka.clusterers.

        Database currentDatabase = new SequentialDatabase()
        EuclidianDataObject edo = new EuclidianDataObject(data.firstInstance(), "", );


        int clusterIndex = 0;

        long startTime = System.currentTimeMillis();

        /*
          C = 0
          for each unvisited point P in dataset D
             N = getNeighbors (P, epsilon)
             if (sizeof(N) < minPts)
                mark P as NOISE
             else
                ++C
                mark P as visited
                add P to cluster C
                recurse (N)
         */



        //TODO: need to handle the clustering algorithm right here
        for (int i = 0; i < data.numInstances(); i++) {
            System.err.println(data.instance(i).toString());
        }

        this.lastClusteringDuration = (System.currentTimeMillis() - startTime);
    }

    

    /**
     * Obtain the capabilities (in terms of data attributes) that this clusterer can handle
     * @return the capabilities of this clusterer
     */
    public Capabilities getCapabilities() {

      Capabilities currentCapabilities = super.getCapabilities();   // returns the object from weka.classifiers.Classifier

      //TODO check into the capabilities and what we should be offering here
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
