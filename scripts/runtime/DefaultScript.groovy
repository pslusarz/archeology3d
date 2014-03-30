import org.sw7d.archeology.Module
import org.sw7d.archeology.ArcheologyFile
import org.sw7d.archeology.data.DataPointProvider
import org.sw7d.archeology.data.DataPoint3d
println "here be start"

List<DataPoint3d> dp = []
modules.find {it.name.startsWith('hadoop')}.files.findAll {it.javaName()}.each { ArcheologyFile javaFile ->
    dp << new DataPoint3d(name: javaFile.javaName(), z: javaFile.imports.size() * 2, x: (javaFile.linesCount / 10), y: javaFile.commits.size()*2) 
}

def dpp = new DataPointProvider() {
    List<String> getXYZLabels() {
        ['Lines of Code', 'Commits','Imports']
    }
}

dpp.dataPoints = dp

return dpp
