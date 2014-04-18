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
    
    def colors = ['Orange','Brown','Red','Magenta','Pink'] //['DarkGray', 'Gray', 'LightGray', 'Cyan','Yellow','Green',
    
    def initModules() {
        modules = Modules.create()
        def moduleColors = [:].withDefault{'Blue'}
        modules.sort{-it.files.size()}[0..(colors.size()-1)].eachWithIndex { module, i ->
            moduleColors[module.name] = colors[i]
        }
        modules*.files.flatten().findAll{it.popularity > 0}.sort{-it.popularity}.each { ArcheologyFile it ->
            dataPoints << new DataPoint3d(name: it.javaName(), x: (it.popularity) , y: (it.javaImports.size()), z: (it.linesCount), 
                delegate: it, 
                color: moduleColors[it.module.name],
                scale: new Scale3d(y: 5.0, z: 0.1)
            )
        }
        
    }
     
    
    
    
    
    
    
    
  }

