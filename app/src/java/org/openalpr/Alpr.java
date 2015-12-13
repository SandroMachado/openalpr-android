package org.openalpr;

public interface Alpr {
	/**
	 * 
	 * @param imgFilePath - Image containing the license plate
	 * @param topN - Max number of possible plate numbers to return(default 10)
	 * @return - JSON string of results
	 */
	String recognize(String imgFilePath, int topN);
	
	/**
	 * 
	 * @param country - Country code to identify (either us for USA or eu for Europe).
     					Default=us
	 * @param region -  Attempt to match the plate number against a region template (e.g., md
     					for Maryland, ca for California)
	 * @param imgFilePath - Image containing the license plate
	 * @param topN - Max number of possible plate numbers to return(default 10)
	 * @return - JSON string of results
	 */
	String recognizeWithCountryNRegion(String country, String region,
                                       String imgFilePath, int topN);
	
	/**
	 * 
	 * @param country - Country code to identify (either us for USA or eu for Europe).
     					Default=us
	 * @param region -  Attempt to match the plate number against a region template (e.g., md
     					for Maryland, ca for California)
	 * @param imgFilePath - Image containing the license plate
	 * @param configFilePath - Config file path (default /etc/openalpr/openalpr.conf)
	 * @param topN - Max number of possible plate numbers to return(default 10)
	 * @return - JSON string of results
	 */
	String recognizeWithCountryRegionNConfig(String country, String region,
                                             String configFilePath, String imgFilePath, int topN);
	/**
	 * 
	 * @return - Version string
	 */
	String version();
	
	public static class Factory {
        static Alpr instance;
		public synchronized static Alpr create(){
			if(instance == null){
                instance = new AlprJNIWrapper();
            }
            return instance;
		}
	}
	
}
