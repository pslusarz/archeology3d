package org.sw7d.archeology.data

import org.sw7d.archeology.Modules
import org.sw7d.archeology.ArcheologyFile

class DefaultDataPointProvider extends DataPointProvider {
    def javaNames
    def namesByPopularity
    
    DefaultDataPointProvider() {
        xLabel = "Popularity"
        yLabel = "Imports"
        zLabel = "Lines of Code"
    }
    
    def initModules() {
        modules = Modules.create()
//        def javaFiles = modules*.files.flatten().findAll{!it.javaName()?.startsWith('java') && (it.extension() == 'java' || it.extension() == 'groovy')}
//        javaNames = javaFiles*.javaName()
//        namesByPopularity = modules*.files*.imports.flatten().findAll{!it?.startsWith('java') && it}.groupBy {it}.sort {a, b -> -a.value.size() <=>-b.value.size()}
//        
//        println "Now computing data points"
//        long start = System.currentTimeMillis()
//        namesByPopularity.each { String className, List<String> popularity ->
//            if (!hasRequestedMaxDataPoints() || dataPoints.size() < maxDataPoints) {
//                ArcheologyFile javaFile = modules.findFirstClassFile(className)
//                if (javaFile) {
//                    def javaImports = javaFile.imports.findAll{javaNames.contains(it)}
//                    javaFile.popularity = popularity.size()
//                    javaFile.javaImports = javaImports
//                    dataPoints << new DataPoint3d(name: javaFile.javaName(), x: (popularity.size() / 5) , y: (javaImports.size() / 5), z: (javaFile.linesCount / 100), delegate: javaFile)
//                }
//            }
//        }
        //println "Computing data points took: ${(System.currentTimeMillis() - start ) / 1000} seconds"
        modules*.files.flatten().findAll{it.popularity > 0}.sort{-it.popularity}.each {
            dataPoints << new DataPoint3d(name: it.javaName(), x: (it.popularity / 5) , y: (it.javaImports.size() / 5), z: (it.linesCount / 100), delegate: it)
        }
        
    }
     
    
    
    
    
    
    
    
  }

