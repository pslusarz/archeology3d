package org.sw7d.archeology.data

import org.sw7d.archeology.Modules
import org.sw7d.archeology.ArcheologyFile

class DataPointProvider {
	Modules modules
        boolean loadedModules = false
        def javaNames
        def namesByPopularity
        int currentModuleZeroBased = -1
        int maxDataPoints= 0
        def initModules() {
            modules = Modules.create()
            def javaFiles = modules*.files.flatten().findAll{!it.javaName()?.startsWith('java') && it.extension() == 'java'}
            javaNames = javaFiles*.javaName()
            namesByPopularity = modules*.files*.imports.flatten().findAll{!it?.startsWith('java') && it}.groupBy {it}.sort {a, b -> -a.value.size() <=>-b.value.size()}
  
        }
     
    List<String> getXYZLabels() {
        ['Popularity', 'Imports', 'Size']
    }
    
    DataPoint3d getNextDataPoint() {
            currentModuleZeroBased++
            def keys = []
            keys.addAll(namesByPopularity.keySet())
            if (exceedsMaxRequestedDataPoints() || currentModuleZeroBased >= keys.size()) {
                return null;
            }
            def className = keys[currentModuleZeroBased]
            def list = namesByPopularity[className]
        
            ArcheologyFile javaFile = modules.findFirstClassFile(className)
            if (javaFile) {
                def javaImports = javaFile.imports.findAll{javaNames.contains(it)}
                return new DataPoint3d(name: javaFile.javaName(), x: (list.size() / 5) , y: (javaImports.size() / 5), z: (javaFile.linesCount / 100))

            } else {
                return getNextDataPoint()
            }
    }
    
    String getDataPointCompletionRatio() {
        int denominator = namesByPopularity.size()
        if (hasRequestedMaxDataPoints()) {
           denominator = Math.min(maxDataPoints, denominator)
        }
        return "${currentModuleZeroBased} / ${denominator}"
    }
    
    private boolean exceedsMaxRequestedDataPoints() {
        if (!hasRequestedMaxDataPoints()) {
            return false
        } else {
            return currentModuleZeroBased >= maxDataPoints
        }
    }
    
    private boolean hasRequestedMaxDataPoints() {
       return maxDataPoints > 0      
    }
    
    
}

