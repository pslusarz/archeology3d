import org.sw7d.archeology.Module
import org.sw7d.archeology.ArcheologyFile
import org.sw7d.archeology.data.DataPointProvider
import org.sw7d.archeology.data.DataPoint3d

//active projects
/*
   Here we try to study how the dynamics of size and class commits changes over time.
   Note that newer projects cluster together, by staying smaller and having fewer commits
   Then, the projects get older, having to balance number of classes with commits per file. The larger, the fewer commits per file will happen.
   
*/
List<DataPoint3d> dp = []
modules.sort {-it.files.findAll{it.javaName()}.size()}.eachWithIndex { Module module, int index ->
    def javaFiles = module.files.findAll{it.javaName()}?:[]
    if (!javaFiles) {
        println "no java files found: "+module.name
    }
    int totalCommits = javaFiles.sum {it.commits.size()} ?: 0
    int commitsPerJavaFile = totalCommits * 10 / (javaFiles.size() ?: 1)
    Date firstCommit = module.files*.commits.collect().flatten().min() ?: new Date() - 100000
    Date lastCommit = module.files*.commits.collect().flatten().max() ?: new Date() - 10000
    String color = "DarkGray"
    Date today = new Date()
    if (lastCommit -firstCommit <  360) {
        color = "Red"
    } else if (lastCommit -firstCommit < 720) {
        color = "Orange"
    } 
    else if (lastCommit -firstCommit < 1080) {
        color = "Yellow"
    } else if (lastCommit -firstCommit < 1440) {
        color = "Blue"
    }
//    if (color == "Orange") {
//        println "active in 90 days: " +module.name
//    }
    dp << new DataPoint3d(name: module.repository+": "+module.name, x: 0, z: module.files.findAll{it.javaName()}.size() / 50, y: commitsPerJavaFile, type: "box", color: color, size:1)  
    //}
}
return new DataPointProvider(zLabel: "java files / 30", yLabel: "Commits per file * 5", xLabel: "n/a", dataPoints: dp)

