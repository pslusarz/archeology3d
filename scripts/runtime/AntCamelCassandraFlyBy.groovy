import org.sw7d.archeology.Module
import org.sw7d.archeology.ArcheologyFile
import org.sw7d.archeology.data.DataPointProvider
import org.sw7d.archeology.data.DataPoint3d


def isGenerated(ArcheologyFile file) {
    file.canonicalPath.contains('thrift') || 
    file.canonicalPath.contains('protobuf/generated') || 
    file.canonicalPath.contains('codegen')
}





List<DataPoint3d> dp = []

def moduleColors = [
      'ant'          : 'Orange',
      'camel'        : 'Red',
      'cassandra'    : 'Cyan',
      'hadoop-common': 'Blue',
      'httpclient'   : 'Yellow'  
    ]

modules*.files.flatten().findAll{it.javaName() && !it.javaName().startsWith('java') && 
         !isGenerated(it)}.each { ArcheologyFile file ->
            
    def color = moduleColors[file.module.name]
    if (color) {
            dp << new DataPoint3d(name: file.javaName(), 
                x: file.popularity * 1000 / file.module.files.size(), 
                y: (file.linesCount) / 10,
                z: file.commits.size()/3,  
                delegate: file, color: color, size: 4)
      }      
    }
return new DataPointProvider(xLabel: "relative popularity",
    yLabel: "size",
    zLabel: "commits", dataPoints: dp)

