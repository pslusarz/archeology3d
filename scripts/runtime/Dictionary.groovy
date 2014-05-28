import org.sw7d.archeology.Module
import org.sw7d.archeology.ArcheologyFile
import org.sw7d.archeology.data.DataPointProvider
import org.sw7d.archeology.data.DataPoint3d
import org.sw7d.archeology.data.Scale3d
import org.sw7d.archeology.tools.NameParser
import org.sw7d.archeology.tools.OutputFileGoldplating


def isGenerated(ArcheologyFile file) {
    file.canonicalPath.contains('thrift') || 
    file.canonicalPath.contains('protobuf/generated') || 
    file.canonicalPath.contains('codegen')
}

List<DataPoint3d> dp = []

Map<String, List<ArcheologyFile>> wordsWithClasses = [:].withDefault {[]}
modules*.files.flatten().findAll{it.javaName() && !it.javaName().startsWith('java') && 
         !isGenerated(it)}.each { ArcheologyFile file ->
         def words = NameParser.getWords(file.javaName()) 
         words.each { String word ->
             wordsWithClasses[word.toLowerCase()] << file
         }
         
       
}

int frequencyCutoff = 500

String contents = """
<html>
  <head>
    <script type="text/javascript" src="tc-mod.js"></script>
    <script type="text/javascript" src="http://www.google.com/jsapi"></script>
<title>Apache Java class name dictionary</title>
  </head>
  <body>
<p>Component words of Apache Java project classes with over ${frequencyCutoff} occurences</p>
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
def selectedWords = wordsWithClasses.findAll {it.value.size() > frequencyCutoff}.sort()
contents += "                data.addRows(${selectedWords.size()});\n"
selectedWords.eachWithIndex { String word, List<ArcheologyFile> files, int i ->
    contents += "                    data.setValue(${i},0,'${word}');\n" 
    contents += "                    data.setValue(${i},1,${files.size()});\n" 
    contents += "                    data.setValue(${i},2,'_${word}.html');\n"
    File wordFile = new File("./results/termcloud/_${word}.html")
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
           wordFile << "                <a href='${it.repoUrl()}'>${it.javaName()}</a>, \n"
       }
       wordFile << "      </td>\n"
       wordFile << "   </tr>\n"  
    }
    wordFile << "</table>\n"
    wordFile << "<p><a href='index.html'> &lt;&lt; Index</a></p>\n"
    wordFile << OutputFileGoldplating.lastModified()
    wordFile << OutputFileGoldplating.analytics
   
    wordFile << "</body></html>\n"
}

contents +=
"""        var outputDiv = document.getElementById('tcdiv');
        var tc = new TermCloud(outputDiv);
        tc.draw(data, null);
      }
    </script>
"""
contents += OutputFileGoldplating.lastModified()
contents += OutputFileGoldplating.analytics
contents +="""
  </body>
</html>

"""

File output = new File("./results/termcloud/index.html")
output.delete()
output.parentFile.mkdirs()
output << contents
if (System.properties['os.name'].toLowerCase().contains('windows')) {
  "cmd /c start ${output.canonicalPath}".execute()
} else {
  "open ${output.canonicalPath}".execute()
}

String termCloudScript = """
/*

  Adapted from visapi-gadgets for Archeology3d

A list of terms, where the size and color of each term is determined
by a number (typically numebr of times it appears in some text).
Uses the Google Visalization API.

Data Format
  First column is the text (string)
  Second column is the weight (positive number)
  Third optional column ia a link (string)

Configuration options:
  target Target for link: '_top' (default) '_blank'

Methods
  getSelection

Events
  select

*/

TermCloud = function(container) {
  this.container = container;
}

TermCloud.MIN_UNIT_SIZE = 15;
TermCloud.MAX_UNIT_SIZE = 300;
TermCloud.RANGE_UNIT_SIZE = TermCloud.MAX_UNIT_SIZE - TermCloud.MIN_UNIT_SIZE;

TermCloud.nextId = 0;

TermCloud.prototype.draw = function(data, options) {

  var cols = data.getNumberOfColumns();
  var valid = (cols >= 2 && cols <= 3 && data.getColumnType(0) == 'string' &&
      data.getColumnType(1) == 'number');
  if (valid && cols == 3) {
    valid = data.getColumnType(2) == 'string';
  }

  if (!valid) {
    this.container.innerHTML = '<span class="term-cloud-error">TermCloud Error: Invalid data format. First column must be a string, second a number, and optional third column a string</span>';
    return;
  }

  options = options || {};
  var linkTarget = options.target || '_top';

  // Compute frequency range
  var minFreq = 999999;
  var maxFreq = 0;
  for (var rowInd = 0; rowInd < data.getNumberOfRows(); rowInd++) {
    var f = data.getValue(rowInd, 1);
    if (f > 0) {
      minFreq = Math.min(minFreq, f);
      maxFreq = Math.max(maxFreq, f);
    }
  }

  if (minFreq > maxFreq) {
    minFreq = maxFreq;
  }
  if (minFreq == maxFreq) {
    maxFreq++;
  }
  var range = maxFreq - minFreq;
  range = Math.max(range, 4);

  var html = [];
  var id = TermCloud.nextId++;
  html.push('<div class="term-cloud">');
  for (var rowInd = 0; rowInd < data.getNumberOfRows(); rowInd++) {
    var f = data.getValue(rowInd, 1);
    if (f > 0) {
      var text = data.getValue(rowInd, 0);
      var link = cols == 3 ? data.getValue(rowInd, 2) : null;
      var freq = data.getValue(rowInd, 1);
      var size = TermCloud.MIN_UNIT_SIZE +
           Math.round((freq - minFreq) / range * TermCloud.RANGE_UNIT_SIZE);
      html.push('<a class="term-cloud-link" href="', (link || '#'), '" id="_tc_', id, '_', rowInd , '"');
      if (link) {
        html.push(' target="', linkTarget, '"');
      }
      html.push('>');
      html.push('<span style="font-size: ', size, 'px;">');
      html.push(this.escapeHtml(text).replace(/ /g, '&nbsp;'));
      html.push('</span>');
      html.push('</a>');
      html.push(' ');
    }
  }
  html.push('</div>');

  this.container.innerHTML = html.join('');
  
  // Add event listeners
  var self = this;
  for (var rowInd = 0; rowInd < data.getNumberOfRows(); rowInd++) {
    var f = data.getValue(rowInd, 1);
    if (f > 0) {
      var text = data.getValue(rowInd, 0);
      var link = cols == 3 ? data.getValue(rowInd, 2) : null;
      var anchor = document.getElementById('_tc_' + id + '_' + rowInd);
      anchor.onclick = this.createListener(rowInd, !!link);
    }
  }
};

TermCloud.prototype.createListener = function(row, hasLink) {
  var self = this;
  return function() { 
    self.selection = [{row: row}];
    google.visualization.events.trigger(self, 'select', {});
    return hasLink;
  }
};

TermCloud.prototype.selection = [];

TermCloud.prototype.getSelection = function() {
  return this.selection;
};

TermCloud.prototype.escapeHtml = function(text) {
  if (text == null) {
    return '';
  }
  return text.replace(/&/g, '&amp;').
      replace(/</g, '&lt;').
      replace(/>/g, '&gt;').
      replace(/"/g, '&quot;');
};


"""

File js = new File("./results/termcloud/tc-mod.js")
js.delete()
js << termCloudScript

return new DataPointProvider(xLabel: "rank",
    yLabel: "",
    zLabel: "occurences", dataPoints: dp)