package org.sw7d.archeology.data

import org.sw7d.archeology.Modules
class DataPointProvider {
    public Scale3d scale = new Scale3d()
    public Modules modules
    public boolean loadedModules = false
    int maxDataPoints= 0
    protected int currentModuleZeroBased = -1
    List<DataPoint3d> dataPoints = []
    List<Edge3d> edges = []
    String xLabel = 'X', yLabel = 'Y', zLabel = 'Z'
    List<String> getXYZLabels() {
        [xLabel, yLabel, zLabel]
    }
    def initModules() {
        
    }
    
    Set<DataPoint3d> vertices = new HashSet<>()  
    public edge(DataPoint3d from, to) {
      [from, to].each { 
          if (vertices.add(it)) {
            it.scale = this.scale
            dataPoints << it
          }
      }  
      edges << new Edge3d(from: from, to: to)
    }
    
    Iterator<Edge3d> edgeIterator
    Edge3d getNextEdge() {
      if (edgeIterator == null) {
          println "creating new iterator"
          edgeIterator = edges.iterator()
      } 
      if (edgeIterator.hasNext()) {
        return edgeIterator.next()
      } else {
          return null
      }
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
            dataPoints[currentModuleZeroBased].scale = this.scale
            return dataPoints[currentModuleZeroBased]
        }
    }
    
    
}

