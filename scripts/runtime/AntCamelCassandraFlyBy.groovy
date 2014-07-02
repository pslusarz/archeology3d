import org.sw7d.archeology.Module
import org.sw7d.archeology.ArcheologyFile
import org.sw7d.archeology.data.DataPointProvider
import org.sw7d.archeology.data.DataPoint3d
import org.sw7d.archeology.data.Scale3d

List<DataPoint3d> dp = []

def moduleColors = [
      'tuscany-sca-1.x'          : 'Orange',
      //'camel'        : 'Red',
      //'cassandra'    : 'Cyan',
      //'hadoop-common': 'Blue',
      'geronimo-devtools'   : 'Yellow'  
    ]

modules.findAll{moduleColors.keySet().contains(it.name)}*.files.flatten().findAll{it.javaName() && !it.javaName().startsWith('java') && 
         !it.isGenerated()}.each { ArcheologyFile file ->
            
    def color = moduleColors[file.module.name]
    if (color) {
            dp << new DataPoint3d(name: file.javaName(), 
                x: file.popularity * 1000 / file.module.files.size(), 
                y: (file.linesCount),
                z: file.commits.size(),  
                delegate: file, color: color, size: 4)
      }      
    }
return new DataPointProvider(xLabel: "relative popularity",
    yLabel: "size",
    zLabel: "commits", dataPoints: dp, scale: new Scale3d(x: 1, y: 0.1, z: 0.33))