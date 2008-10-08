package weka.clusterers;

import weka.clusterers.AbstractClusterer;
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
     * Counter for the number of available clusters (may refactor into list.size())
     */
    private int clusterCounter = 0;

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
     * Generates a clusterer. Has to initialize all fields of the clusterer
     * that are not being set via options.
     *
     * @param data set of instances serving as training data
     * @throws Exception if the clusterer has not been
     *                   generated successfully
     */
    public void buildClusterer(Instances data) throws Exception {

        //TODO: need to handle the clustering algorithm right here
        for (int i = 0; i < data.numInstances(); i++) {
            System.err.println(data.instance(i).toString());
        }
    }

    /**
     * Returns the number of clusters.
     *
     * @return the number of clusters generated for a training dataset.
     * @throws Exception if number of clusters could not be returned
     *                   successfully
     */
    public int numberOfClusters() throws Exception {
        return clusterCounter;
    }

    public Capabilities getCapabilities() {

        //TODO check into the capabilities and what we should be offering here
      Capabilities result = super.getCapabilities();   // returns the object from weka.classifiers.Classifier

      // attributes
      result.enable(Capabilities.Capability.NOMINAL_ATTRIBUTES);
      result.enable(Capabilities.Capability.NUMERIC_ATTRIBUTES);
      //result.enable(Capabilities.Capability.DATE_ATTRIBUTES);
      //result.enable(Capabilities.Capability.MISSING_VALUES);

      // class
      result.enable(Capabilities.Capability.NOMINAL_CLASS);
      //result.enable(Capabilities.Capability.MISSING_CLASS_VALUES);

      return result;
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
        return "Hello world!" + System.getProperty("java.class.path");
    }

    public static void main(String[] args) {
        runClusterer(new UltraDBScan(), args);
    }
}
