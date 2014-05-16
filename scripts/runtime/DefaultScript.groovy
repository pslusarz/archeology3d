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
//
//
//
//
//
List<DataPoint3d> dp = []
//
//def moduleColors = [
//      'ant'          : 'Orange',
//      'camel'        : 'Red',
//      'cassandra'    : 'Cyan',
//      'hadoop-common': 'Blue',
//      'httpclient'   : 'Yellow'  
//    ]
Map<String, List<ArcheologyFile>> wordsWithClasses = [:].withDefault {[]}
modules*.files.flatten().findAll{it.javaName() && !it.javaName().startsWith('java') && 
         !isGenerated(it)}.each { ArcheologyFile file ->
         def words = NameParser.getWords(file.javaName()) 
         words.each { String word ->
             //String theWord = word.toLowerCase()[-1] == 's' ? word[0..-2].toLowerCase(): word.toLowerCase() 
             wordsWithClasses[word.toLowerCase()] << file
         }
         
       
}
//
//wordsWithClasses.sort {-it.value.size()}.eachWithIndex { String word, List<ArcheologyFile> files, int i ->
//    if (files.size() > 10) {
//        String usages = ""
//        files.each {
//          usages += "${it.javaName()}[${it.module.name}] , "  
//        }
//        println word + "  "+files.size()+"  "+ usages
//        dp << new DataPoint3d(name: word, x: i, y: 0, z:files.size())
//    }
//}
//


String contents = """
<html>
  <head>
    <link rel="stylesheet" type="text/css" href="http://visapi-gadgets.googlecode.com/svn/trunk/termcloud/tc.css"/>
    <script type="text/javascript" src="http://visapi-gadgets.googlecode.com/svn/trunk/termcloud/tc.js"></script>
    <script type="text/javascript" src="http://www.google.com/jsapi"></script>
  </head>
  <body>
    <div id="tcdiv"></div>
    <script type="text/javascript">
      google.load("visualization", "1");
      google.setOnLoadCallback(draw);
      function draw() {
        data = new google.visualization.DataTable();
        data.addColumn('string', 'Label');
        data.addColumn('number', 'Value');
        data.addColumn('string', 'Link');
"""
def selectedWords = wordsWithClasses.findAll {it.value.size() > 100}.sort()
contents += "                data.addRows(${selectedWords.size()});\n"
selectedWords.eachWithIndex { String word, List<ArcheologyFile> files, int i ->
    contents += "                    data.setValue(${i},0,'${word}');\n" 
    contents += "                    data.setValue(${i},1,${files.size()});\n" 
    contents += "                    data.setValue(${i},2,'${word}.html');\n"
    File wordFile = new File("./results/termcloud/${word}.html")
    wordFile.delete()
    wordFile.parentFile.mkdirs()
    wordFile << "<html><head><title>${word} in class names</title></head><body><h1>${files.size()} occurences of '${word}' in class names</h1>\n"
    wordFile << "<p><a href='index.html'> &lt;&lt; Index</a></p>\n"
    wordFile << "<table>\n"
    files.groupBy {it.module.name}.sort().each { String moduleName, List<ArcheologyFile> moduleFiles -> 
       wordFile << "   <tr>\n" 
       wordFile << "      <td>${moduleName}</td>\n"
       wordFile << "      <td>\n"
       moduleFiles.each {
           wordFile << "                ${it.javaName()}, \n"
       }
       wordFile << "      </td>\n"
       wordFile << "   </tr>\n"  
    }
    wordFile << "</table>\n"
    wordFile << "<p><a href='index.html'> &lt;&lt; Index</a></p>\n"
    wordFile << "</body></html>\n"
}

//"        data.addRows(3);
//        data.setValue(0, 0, 'First Term');
//        data.setValue(0, 1, 10);
//        data.setValue(1, 0, 'Second');
//        data.setValue(1, 1, 30);
//        data.setValue(1, 2, 'http://www.google.com');
//        data.setValue(2, 0, 'Third');
//        data.setValue(2, 1, 20);

contents +=
"""        var outputDiv = document.getElementById('tcdiv');
        var tc = new TermCloud(outputDiv);
        tc.draw(data, null);
      }
    </script>
  </body>
</html>

"""

File output = new File("./results/termcloud/index.html")
output.delete()
output.parentFile.mkdirs()
output << contents
"cmd /c start ${output.canonicalPath}".execute()

return new DataPointProvider(xLabel: "rank",
    yLabel: "",
    zLabel: "occurences", dataPoints: dp)