import org.sw7d.archeology.Module
import org.sw7d.archeology.ArcheologyFile
import org.sw7d.archeology.data.DataPointProvider
import org.sw7d.archeology.data.DataPoint3d
println "here be start"

//popularity 
//List<DataPoint3d> dp = []
//modules*.files.flatten().findAll{it.javaName()}.sort{-it.popularity}.each { ArcheologyFile javaFile ->
//    dp << new DataPoint3d(name: javaFile.javaName(), delegate: javaFile, x: javaFile.popularity, z: (javaFile.linesCount / 10), y: javaFile.commits.size()*2) 
//}
//
//return new DataPointProvider(zLabel: "Lines of Code /10", yLabel: "Commits *2", xLabel: "Popularity", dataPoints: dp)

List<DataPoint3d> dp = []
modules.sort {-it.files.findAll{it.javaName()}.size()}.eachWithIndex { Module module, int index ->
    def javaFiles = module.files.findAll{it.javaName()}?:[]
    if (!javaFiles) {
        println "no java files found: "+module.name
    }
    int totalCommits = javaFiles.sum {it.commits.size()}
    int commitsPerJavaFile = totalCommits / javaFiles.size()
    dp << new DataPoint3d(name: module.repository+": "+module.name, x: index *2, z: module.files.findAll{it.javaName()}.size() / 10, y: commitsPerJavaFile)  
}
return new DataPointProvider(zLabel: "java files / 10", yLabel: "n/a", xLabel: "Rank * 2", dataPoints: dp)