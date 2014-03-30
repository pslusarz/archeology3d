package org.sw7d.archeology.data

import org.sw7d.archeology.Modules
class DataPointProvider {
    public Modules modules
    public boolean loadedModules = false
    int maxDataPoints= 0
    protected int currentModuleZeroBased = -1
    List<DataPoint3d> dataPoints = []
    List<String> getXYZLabels() {
        ['X', 'Y', 'Z']
    }
    def initModules() {
        
    }
    
    String getDataPointCompletionRatio() {
        int denominator = dataPoints.size()
        if (hasRequestedMaxDataPoints()) {
            denominator = Math.min(maxDataPoints, denominator)
        }
        return "${currentModuleZeroBased} / ${denominator}"
    }
    
    protected boolean exceedsMaxRequestedDataPoints() {
        if (!hasRequestedMaxDataPoints()) {
            return false
        } else {
            return currentModuleZeroBased >= maxDataPoints
        }
    }
    
    protected boolean hasRequestedMaxDataPoints() {
        return maxDataPoints > 0      
    }
    
    public DataPoint3d getNextDataPoint() {
        
        if (exceedsMaxRequestedDataPoints() || currentModuleZeroBased >= dataPoints.size()-1) {
            return null;
        } else {
            currentModuleZeroBased++
            return dataPoints[currentModuleZeroBased]
        }
    }
    
    
}

