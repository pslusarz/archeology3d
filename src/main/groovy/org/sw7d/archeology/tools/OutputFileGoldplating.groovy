package org.sw7d.archeology.tools

class OutputFileGoldplating {
    static String analytics = """
 <script>
  (function(i,s,o,g,r,a,m){i['GoogleAnalyticsObject']=r;i[r]=i[r]||function(){
  (i[r].q=i[r].q||[]).push(arguments)},i[r].l=1*new Date();a=s.createElement(o),
  m=s.getElementsByTagName(o)[0];a.async=1;a.src=g;m.parentNode.insertBefore(a,m)
  })(window,document,'script','//www.google-analytics.com/analytics.js','ga');

  ga('create', 'UA-49239121-1', 'pslusarz.github.io');
  ga('send', 'pageview');

</script>
    """
    static String lastModified() {
        return "<p>Last modified: ${new Date().format('yyyy/MM/dd HH:mm:ss')}</p>"
    }
    
    static String timestamp() {
        new Date().format('yyyy-MM-dd_HH-mm-ss')
    }
    
    
}

