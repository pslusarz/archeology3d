package org.sw7d.archeology

import java.util.List

class Modules extends ArrayList<Module> {
    static String serializedFile = "apache-modules.pickle"

    public Modules initFromFilesystem() {
        String root = '../apache-data/'
        println "reading from file system"
        new File(root).eachDir { File repository ->
            repository.eachDir{ File moduleDir ->
                Module module = new Module(name: moduleDir.name, path: moduleDir.absolutePath, repository: repository.name)
                println "  " + module.name
                if (module.name != '.DS_Store') {
                    this << module.init()
                }
            }
        }
        this.each { it.initLibrary(this) }
        //initLevels()
        this

    }

    List<ArcheologyFile> findClassFile(String className) {
      List<ArcheologyFile> result = []
      this.each { Module module ->
        ArcheologyFile file = module.classFiles[className]
        if (file) {
            result << file
        }
      }
      result
    }

    ArcheologyFile findFirstClassFile(String className) {
        List<ArcheologyFile> result = findClassFile(className)
        if (result.size() > 0) {
            return result.first()
        } else {
            return null
        }
    }

//    private void initLevels() {
//        def changed = true
//        while (changed) {
//            changed = false
//            this.each {
//                if (it.dependsOn.size() == 0) {
//                    if (it.level == -1) {
//                        it.level = 1
//                        changed = true
//                    }
//                } else if (!it.dependsOn.findAll { it.level == -1 }) {
//                    def newLevel = (it.dependsOn.max { a, b -> a.level <=> b.level }).level + 1
//                    if (newLevel != it.level) {
//                        changed = true
//                        it.level = newLevel
//                    }
//                }
//            }
//        }
//    }

    public serialize() {
        println "serializing to $serializedFile"
        def output = new FileOutputStream(serializedFile)
        output.withObjectOutputStream { oos ->
            oos << this
        }
    }

    public static Modules initFromPickle() {
        println "loading from pickle"
        def fis = new FileInputStream(serializedFile)
        def ois = new MyObjectInputStream(Module.class.classLoader, fis)
        return ois.readObject()
    }

    public static Modules create() {
        if (!new File(serializedFile).exists()) {
            new Modules().initFromFilesystem().serialize()
        }
        return initFromPickle()
    }
}
