import org.sw7d.archeology.Module
import org.sw7d.archeology.ArcheologyFile
println "here be start"
def filesByModule = [:].withDefault{new HashSet<Module>()}
def filesByJavaName = [:].withDefault{[]}
modules.each { Module module ->
   module.files.findAll{it.javaName() && !it.absolutePath.contains('test')}.each { ArcheologyFile file ->
     filesByModule[file.javaName()] << module.name
     filesByJavaName[file.javaName()] << file
   } 
}
println "SIZE: "+filesByModule.size()
filesByModule = filesByModule.sort { a, b -> -a.value.size() <=> -b.value.size()}
File output = new File("apache-duplicateClasses.htm")
output.delete()
output << "<html><body>"
int i = 0
filesByModule.each { key, value ->
    if (value.size() > 1) {
        i++
        output << "<h5>$i:"+key+"</h5>"
        output << "<p>modules: "+value+"</p>"
        def files = filesByJavaName[key]
        output << "<ul>"
        files.each { ArcheologyFile file ->
            output << "   <li><a href='file://"+file.canonicalPath+"'>"+file.canonicalPath+" "+file.linesCount+"</a></li>"
        }
        output << "</ul>"
    }
}
output << "</body></html>"
"open ${output.canonicalPath}".execute()
println "done"
return modules.size()

