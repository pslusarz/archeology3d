import org.sw7d.archeology.Module
import org.sw7d.archeology.ArcheologyFile
import org.sw7d.archeology.data.DataPointProvider
import org.sw7d.archeology.data.DataPoint3d
import org.sw7d.archeology.data.Scale3d
import org.sw7d.archeology.parsing.NameParser


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
Map<String, List<ArcheologyFile>> wordsWithClasses = [:].withDefault {[]}
modules*.files.flatten().findAll{it.javaName() && !it.javaName().startsWith('java') && 
    !isGenerated(it)}.each { ArcheologyFile file ->
    def words = NameParser.getWords(file.javaName()) 
    words.each { String word ->
        wordsWithClasses[word] << file
    }
         
       
}

wordsWithClasses.sort {-it.value.size()}.eachWithIndex { String word, List<ArcheologyFile> files, int i ->
    if (files.size() > 10) {
        String usages = ""
        files.each {
          usages += "${it.javaName()}[${it.module.name}] ,"  
        }
        println word + "  "+files.size()+"  "+ files
        dp << new DataPoint3d(name: word, x: i, y: 0, z:files.size())
    }
}

return new DataPointProvider(xLabel: "rank",
    yLabel: "",
    zLabel: "occurences", dataPoints: dp)

