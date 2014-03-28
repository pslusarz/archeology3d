package org.sw7d.archeology

class Module implements Serializable{
  String path
  String name
  String group
  List<Module> referencedBy = []
  List<Module> dependsOn = []
  List<ArcheologyFile> files = []
  List<ArcheologyFile> meaningfulFiles = []
  Map<String, ArcheologyFile> classFiles = new HashMap<>()
  Map lines = [:]
  String dependenciesFile
  String repository
  File buildFile
  String buildType
  boolean hasAntBuild
  boolean hasAntBuildMainDir
  boolean hasIvy
  boolean isGrails
  boolean isGradle
  boolean isStaticContent
  boolean isBBv2
  boolean dataServicesBuild
  boolean hasIntegration
  Date lastModDate = new Date() - 10000
  static final long serialVersionUID = 1l
  int level = -1


  def buildCategories = [
	  ['Building Blocks Java', {it.hasAntBuild && it.hasIvy && !it.isGrails && !it.isGradle && !it.isBBv2}],
	  ['Building Blocks (static content)', {it.isStaticContent}],
	  ['Building Blocks V2', {it.isBBv2}],
	  ['Grails', {it.isGrails}],
	  ['Gradle', {it.isGradle}],
	  ['*Ant (non-standard)', {!it.isBBv2 && !it.isGrails && !it.isGradle && !it.hasIvy && !it.isStaticContent && !it.dataServicesBuild && (it.hasAntBuild || it.hasAntBuildMainDir)}],
	  ['*Ant+Ivy (non-standard)', {it.hasAntBuildMainDir && it.hasIvy && !it.hasAntBuild && !it.isGrails &&!it.isBBv2 && !it.isGradle}],
	  ['Data Services build', {it.dataServicesBuild}],
	  ['Ivy only', {!it.hasAntBuild && it.hasIvy && !it.isGrails && !it.hasAntBuildMainDir && !it.isGradle}],
	  ['*No recognizable build pattern', {!it.hasAntBuild && !it.hasIvy && !it.isGrails && !it.hasAntBuildMainDir && !it.isGradle && !it.isBBv2 && !it.isStaticContent && !it.dataServicesBuild}]
  ]

  static final FileFilter MEANINGFULFILES = [accept: {File it -> it.name != 'CVS' && it.name != '.DS_Store' && it.name != '.svn' && it.name != '.git' && it.name != '.gitignore' && it.name.toLowerCase() != 'dummy.txt'
  }] as FileFilter

  public String gname() {
	  name.replaceAll("-", "_")
  }
  
  public boolean isRepoGit() {
      ['git', 'github', 'stash'].contains(repository)
  } 
  
  String repoUrl() {
      if (repository == 'github' && path.contains("/apache")) {
         return "https://github.com/apache/${name}" 
      } else {
          return "file://${path}"
      }
  }  

  public String toString() {
	  String result = "$name -> $buildType ($repository)"
	  result += "\nFiles (${files.size()}):"
	  files.each {
		  result +="\n   "+ it.absolutePath
	  }
	  println "  Referenced by:"
	  referencedBy.each {
		  result +="\n    $it.name $it.repository"
	  }
	  return result
  }

  boolean isLibrary() {
	 referencedBy.size() > 0
  }

  def initLibrary(modules) {
	  if (['VMS'].contains(name)) {
		  return
	  }
	  modules.each {
		  if (it != this && it.dependenciesFile ) {
			 if (it.dependenciesFile.contains(/"$name"/) || (it.dependenciesFile.contains(/:$name:/))) {
				 referencedBy << it
				 it.dependsOn << this
			 }
		  }
	  }
  }

  boolean hasMeaningfulFiles(File dir) {
	  if (!dir.exists()) {
		  return false
	  }
	  def files = dir.listFiles(MEANINGFULFILES)
	  def result = false
	  if (files.find { !it.isDirectory()}) {
		  result = true
	  } else {
		for (File subdir : files) {
			if (hasMeaningfulFiles(subdir)) {
				result = true
				break
			}
		}
	  }
	  return result
  }
  void initFiles(File dir) {
	  def currentDirfiles = dir.listFiles(MEANINGFULFILES)
	  currentDirfiles.each {
			  if ( !it.isDirectory()) {
			      files << new ArcheologyFile(it, this)
			  } else {
				initFiles(it)
			  }
	  }
  }

  boolean isSandbox() {
	  def result = false
	  result
  }

  boolean hasMeaningfulFiles() {
	  hasMeaningfulFiles( new File(path))
  }

  boolean isDead() {
	  boolean result = false
	  File moduleDir = new File(path)
	  if (!hasMeaningfulFiles()) {
		  result = true
	  }
	  def files = moduleDir.listFiles([accept: {File it -> it.name != 'CVS' && it.name != '.DS_Store'}] as FileFilter)

	  files.each {
		  if (!it.isDirectory() && (it.name.toLowerCase().contains('dead') || it.name.toLowerCase().contains('moved'))) {
			  result = true
		  }
	  }
	  return result
  }

  List<ArcheologyFile> acceptanceFiles() {
	  List<ArcheologyFile> results = []
	  meaningfulFiles.each {
		  if (it.absolutePath.contains('acceptance')) {
			  results << it
		  }
	  }
	  results
  }

  boolean isUsedAsAcceptance() {
	acceptanceFiles() &&
	referencedBy.find { Module user ->
		findReferenceLineDownstream(user)
	}
  }

  String findReferenceLineDownstream(Module user) {
	  user.dependenciesFile.split('\n').find { String line ->
		  line.contains(name) && line.contains(/->acceptance/)
	  }
  }


  def initMeaningfulFiles() {
	  meaningfulFiles = []
	  files.each {
		if (MEANINGFULFILES.accept(it) ) {
			meaningfulFiles << it
		}
	  }
  }

  def initClassFiles() {
      files.findAll{it.javaName()}.each {
        classFiles[it.javaName()] = it
      }
  }


  def initLinesOfCode() {
	  files.each { ArcheologyFile file ->
		  if (file.isText()) {
			  lines[file.extension()] = (lines[file.extension()]?:0) + file.linesCount
		  }
	  }
  }

  List<ArcheologyFile> filesOfType(String extension) {
	  files.findAll {ArcheologyFile file -> file.extension() == extension}
  }


  boolean dependsOnTransiently(Module candidate) {
    boolean result = false
    dependsOn.findAll {it != candidate}.each { Module upstream ->
       if (upstream.dependsOn.contains(candidate)) {
           result = true
       }  else if (upstream.dependsOnTransiently(candidate)) {
           result = true
       }
    }
    return result
  }


  Module init() {
	  initFiles(new File(path))
	  initMeaningfulFiles()
      initClassFiles()
	  initLinesOfCode()
	  files.each {
		  Date lmd = new Date(it.lastModified())
		  if (lmd > lastModDate) {
			  lastModDate = lmd
		  }
	  }
	  File buildingBlocksBuild = new File(path+'/build/build.xml')
	  if (buildingBlocksBuild.exists()) {
		  buildFile = buildingBlocksBuild
		  hasAntBuild = true
	  }
	  File antBuild = new File(path+'/build.xml')
	  if (antBuild.exists()) {
		  buildFile = antBuild
		  hasAntBuildMainDir = true
	  }

	  File ivy = new File(path+'/ivy.xml')
	  if (ivy.exists()) {
		dependenciesFile = ivy.text
		hasIvy = true
	  }
	  File loalbuild = new File(path+'/LocalBuild.xml')
	  if (loalbuild.exists()) {
		  dataServicesBuild = true
		  File tasks = new File(path+'/Tasks.xml')
		  if (tasks.exists()) {
			  dependenciesFile = tasks.text
		  }
	  }

	  File grails = new File(path+'/grails-app/conf/BuildConfig.groovy')
	  if (grails.exists()) {
		  buildFile = grails
		  dependenciesFile = grails.text
		  isGrails = true
	  }
	  File gradle = new File(path+'/build.gradle')
	  if (gradle.exists()) {
		  buildFile = gradle
		  dependenciesFile = gradle.text
		  isGradle = true
	  }
	  if(isGradle) {
	  	  File gradlePropertiesFile = new File(path + '/gradle.properties')
		  if(gradlePropertiesFile.exists()) {
			  Properties gradleProperties = new Properties()
			  gradlePropertiesFile.withInputStream { stream ->
				  gradleProperties.load(stream)
			  }
			  group = gradleProperties['group']
		  }
		  if(group == null) {
			  // Look in build.gradle group it wasn't found in gradle.properties
		  	  String line = buildFile.text.split('\n').find { line ->
					line.startsWith('group')
			  }
              if (line) {
                  def betweenQuotes = line.find(/'(.+)'/)
                  if (betweenQuotes) {
                      group = betweenQuotes.replace("'", "")
                  } else {
                      betweenQuotes =  line.find(/"(.+)"/)
                      if (betweenQuotes) {
                          group = betweenQuotes.replace('"', '')
                      }
                  }
              }
		  }
	  }
	  else if(hasIvy && !isGrails) {
		  def dependenciesNodes
		  dependenciesNodes = new XmlSlurper().parseText(dependenciesFile)
		  group = dependenciesNodes.info.@organisation.text()
	  }
	  isBBv2 = buildFile ? buildFile.text.contains('buildingBlocks-v2'): false
	  isStaticContent = hasAntBuild && buildFile.text.contains('building-blocks-static-content.xml')

	  hasIntegration = hasMeaningfulFiles(new File(path+"/src/integration/"))
	  buildCategories.each {
		  if (it[1](this)) {
			  buildType = it[0]
		  }
	  }
	  this
  }

}
