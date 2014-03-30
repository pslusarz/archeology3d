package org.sw7d.archeology.data

import org.sw7d.archeology.Modules
import org.sw7d.archeology.ArcheologyFile

class DefaultDataPointProvider extends DataPointProvider {
    def javaNames
    def namesByPopularity
        
    
    def initModules() {
        modules = Modules.create()
        def javaFiles = modules*.files.flatten().findAll{!it.javaName()?.startsWith('java') && it.extension() == 'java'}
        javaNames = javaFiles*.javaName()
        namesByPopularity = modules*.files*.imports.flatten().findAll{!it?.startsWith('java') && it}.groupBy {it}.sort {a, b -> -a.value.size() <=>-b.value.size()}
        
        println "Now computing data points"
        long start = System.currentTimeMillis()
        namesByPopularity.each { String className, List<String> popularity ->
            if (!hasRequestedMaxDataPoints() || dataPoints.size() < maxDataPoints) {
                ArcheologyFile javaFile = modules.findFirstClassFile(className)
                if (javaFile) {
                    def javaImports = javaFile.imports.findAll{javaNames.contains(it)}
                    dataPoints << new DataPoint3d(name: javaFile.javaName(), x: (popularity.size() / 5) , y: (javaImports.size() / 5), z: (javaFile.linesCount / 100))
                }
            }
        }
        println "Computing data points took: ${(System.currentTimeMillis() - start ) / 1000} seconds"
        
    }
     
    List<String> getXYZLabels() {
        ['Popularity', 'Imports', 'Size']
    }
    
//    DataPoint3d getNextDataPoint() {
//        currentModuleZeroBased++
//        def keys = []
//        keys.addAll(namesByPopularity.keySet())
//        if (exceedsMaxRequestedDataPoints() || currentModuleZeroBased >= keys.size()) {
//            return null;
//        }
//        def className = keys[currentModuleZeroBased]
//        def list = namesByPopularity[className]
//        
//        ArcheologyFile javaFile = modules.findFirstClassFile(className)
//        if (javaFile) {
//            def javaImports = javaFile.imports.findAll{javaNames.contains(it)}
//            return new DataPoint3d(name: javaFile.javaName(), x: (list.size() / 5) , y: (javaImports.size() / 5), z: (javaFile.linesCount / 100))
//
//            } else {
//                return getNextDataPoint()
//            }
//    }
    
    
    
    
    
    
  }

