import org.sw7d.archeology.Module
import org.sw7d.archeology.ArcheologyFile
println "here be start"
def filesByModule = [:].withDefault{[]}
modules.each { Module module ->
   module.files.findAll{it.javaName()}.each { ArcheologyFile file ->
     filesByModule[file.javaName()] << module.name  
   } 
}
println "SIZE: "+filesByModule.size()
filesByModule = filesByModule.sort { a, b -> -a.value.size() <=> -b.value.size()}
filesByModule.each { key, value ->
    if (value.size() > 1) {
        println key
        println "   modules: "+value
    }
}
println "--------here be end--------"
return modules.size()

