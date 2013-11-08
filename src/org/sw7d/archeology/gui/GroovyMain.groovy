package org.sw7d.archeology.gui

import com.jme3.app.SimpleApplication
import com.jme3.scene.shape.Box
import com.jme3.math.Vector3f
import com.jme3.math.Matrix3f
import com.jme3.scene.Geometry
import com.jme3.material.Material
import com.jme3.math.ColorRGBA
import com.jme3.scene.Node
import org.sw7d.archeology.Modules
import org.sw7d.archeology.ArcheologyFile
import org.sw7d.archeology.data.Apache5000
import com.jme3.light.DirectionalLight
import com.jme3.util.TangentBinormalGenerator
import com.jme3.math.Quaternion
import com.jme3.light.AmbientLight
import com.jme3.renderer.queue.RenderQueue.ShadowMode
import com.jme3.input.controls.KeyTrigger
import com.jme3.input.KeyInput
import com.jme3.input.controls.ActionListener
import com.jme3.font.BitmapText
import com.jme3.input.controls.Trigger
import com.jme3.input.controls.MouseButtonTrigger
import com.jme3.input.MouseInput
import com.jme3.collision.CollisionResults
import com.jme3.math.Ray
/**
 *
 * @author ps
 */
class GroovyMain extends SimpleApplication {
    static void main(args){
        
        new GroovyMain().start()
    }
    
    Node pivot
    Material mat, selectedMaterial, originMaterial
    Modules modules
    Map<String, Geometry> spatialsByName = [:]
    def javaFiles
    def javaNames
    def namesByPopularity
    final int MAX_CLASSES = 600
    Geometry selected
    
    int currentModule = 0
    
    @Override
    void simpleInitApp() {
        pivot = new Node(pivot)   
        def al = new AmbientLight()
        al.setColor(ColorRGBA.White.mult(0.5f))
        pivot.addLight(al)
        
        viewPort.setBackgroundColor(ColorRGBA.White);
        makeLight(100,1500,1500, ColorRGBA.White) 
        
        markOrigin()
        
        rootNode.attachChild(pivot)
      
  
        pivot.rotate(-1.5f, 0f, 0f)

        cam.setLocation(new Vector3f(150f, 150f, -150f))
        cam.setRotation(new Quaternion(-1.5f, 0f, 0f, 1f))
        cam.lookAt(new Vector3f(-150f, -130f, 150f), pivot.getLocalTranslation())
        flyCam.setMoveSpeed((float) (flyCam.getMoveSpeed() * 10f));
        
        initKeys()
        initCrossHairs() 
        mat = makeMaterial("Common/MatDefs/SSAO/Textures/random.png")
        selectedMaterial = makeMaterial("Common/MatDefs/Water/Textures/caustics.jpg")
        originMaterial = makeMaterial("Common/MatDefs/Water/Textures/foam.jpg")
        //makeQuickGraph()
        makeGraphFromPickle()

        


    }
    
    def initKeys() {
        handleAction("Skip", new KeyTrigger(KeyInput.KEY_J), {boolean keyPressed, float tpf -> if (!keyPressed) {flyCam.moveCamera(10, false)}})
        handleAction("Select", new MouseButtonTrigger(MouseInput.BUTTON_LEFT), 
            {boolean keyPressed, float tpf -> if (!keyPressed) {
                    CollisionResults results = new CollisionResults()
                    Ray ray = new Ray(cam.location, cam.direction)
                    pivot.collideWith(ray, results)
                    select(results.closestCollision?.geometry)
                    println results.closestCollision?.geometry?.name
                }
            })
        handleAction("Users", new KeyTrigger(KeyInput.KEY_K), 
            {boolean keyPressed, float tpf -> if (!keyPressed) {
                  displayAllMeetingCriteria {Geometry toBeDisplayed -> modules.findFirstClassFile(toBeDisplayed.name).imports?.contains(selected.name)}  
                }
            })
         
        handleAction("Imports", new KeyTrigger(KeyInput.KEY_I), 
            {boolean keyPressed, float tpf -> if (!keyPressed) {
                    displayAllMeetingCriteria {Geometry toBeDisplayed ->  modules.findFirstClassFile(selected.name).imports.contains(toBeDisplayed.name)}
                }
            })
        
    }
    
    void displayAllMeetingCriteria(Closure criteria) {
        spatialsByName.each { String name, Geometry currentSpatial ->

            if (selected) {
                if (selected == currentSpatial) {
                    selected.setMaterial(originMaterial)
                    println " > Origin: "+selected.name
                } else {
                    if (!criteria.call(currentSpatial)) {
                        pivot.detachChild(currentSpatial) 
                    } else {
                        println "    "+currentSpatial.name
                        reset(currentSpatial)
                    }
                }                            
            } else {
                reset(currentSpatial)
            }
                         
        }   
    }
    
    void reset(Geometry geometry) {
        pivot.attachChild(geometry)
        geometry.setMaterial(mat)
    }
    
    void select(Geometry selection) {
        if (selection) {
            selection.setMaterial(selectedMaterial)
        }
        if (selected && selected.material != originMaterial && selected != selection) {
            selected.setMaterial(mat)
        }
        selected = selection
    }
    
    void handleAction (String aname, Trigger trigger, Closure handler) {
        ActionListener actionListener = new ActionListener() {
            String actionName = aname
            void onAction(String name, boolean keyPressed, float tpf) {
                if (name == actionName) {
                    if (!keyPressed) println "======== $actionName =========="
                    handler.call(keyPressed, tpf)
                }
            }
        }
        inputManager.addMapping(aname, trigger)
        inputManager.addListener(actionListener, aname)
    }
    
    def initCrossHairs() {
        setDisplayStatView(false);
        guiFont = assetManager.loadFont("Interface/Fonts/Default.fnt");
        BitmapText ch = new BitmapText(guiFont, false);
        ch.setSize(guiFont.getCharSet().getRenderedSize() * 2);
        ch.setText("+"); // crosshairs
        ch.setLocalTranslation( // center
            (float) (settings.getWidth() / 2 - ch.getLineWidth()/2), (float) (settings.getHeight() / 2 + ch.getLineHeight()/2), 150f);
        guiNode.attachChild(ch);
    }
    
    Material makeMaterial(String path) {
        Material result = new Material(assetManager, "Common/MatDefs/Light/Lighting.j3md");
        result.setTexture("DiffuseMap", 
            assetManager.loadTexture(path));
        result.setBoolean('UseMaterialColors', true)
        result.setColor('Diffuse', ColorRGBA.White)
        result.setColor('Ambient', ColorRGBA.White)
        result.setColor('Specular', ColorRGBA.White)
        result.setFloat('Shininess', 64f)
        return result
    }
    
    @Override
    public void simpleUpdate(float tpf) {
        if (modules && currentModule < namesByPopularity.size() && currentModule < MAX_CLASSES) {
            currentModule++
            def keys = []
            keys.addAll(namesByPopularity.keySet())
            def className = keys[currentModule]
            def list = namesByPopularity[keys[currentModule]]
        
            ArcheologyFile javaFile = modules.findFirstClassFile(className)
            if (javaFile) {
                def javaImports = javaFile.imports.findAll{javaNames.contains(it)}
                makeBox (javaFile.javaName(), list.size(), javaImports.size(), javaFile.linesCount)
                
                if (currentModule%100 == 0) {
                    println "$currentModule/${namesByPopularity.size()} ${javaFile.javaName()} ${list.size()} ${javaImports.size()} ${javaFile.linesCount}"
                }
            }
        }
        
           
    } 
    
    void makeQuickGraph() {
        Apache5000.classes.each {
            makeBox(it.name, it.popularity, it.imports, it.lines)
        }
    }
    
    
    void makeGraphFromPickle() {
        println "PATIENce MORTAL, loADING SOM3 DATAZ"
        modules = Modules.create()
        
        javaFiles = modules*.files.flatten().findAll{!it.javaName()?.startsWith('java') && it.extension() == 'java'}
        javaNames = javaFiles*.javaName()
        namesByPopularity = modules*.files*.imports.flatten().findAll{!it?.startsWith('java') && it}.groupBy {it}.sort {a, b -> -a.value.size() <=>-b.value.size()}

        //        int count = 0
        //        namesByPopularity.each { className, list ->
        //            if (count < 2000) {
        //                ArcheologyFile javaFile = modules.findFirstClassFile(className)
        //                if (javaFile) {
        //                    def javaImports = javaFile.imports.findAll{javaNames.contains(it)}
        //                    makeBox (javaFile.javaName(), list.size(), javaImports.size(), javaFile.linesCount)
        //                    count ++
        //                    if (count%100 == 0) {
        //                        println "$count/${namesByPopularity.size()}"
        //                    }
        //                }
        //            }
        //        }
    }
    
    
    void makeLight(def x, y, z, color) {
        makeUnitBox(x,y,z,color)
        Vector3f sunVector = new Vector3f(x,y,z)    
        DirectionalLight sun = new DirectionalLight();
        sun.setDirection(sunVector);
        sun.setColor(color);
        pivot.addLight(sun);
        
    }
    
    void makeUnitBox(def x, y, z, color) {
        Vector3f sunVector = new Vector3f(x,y,z)
        Box b = new Box(sunVector, 0.5f, 0.5f, 0.5f);
        Geometry geom = new Geometry("Box", b);
        Material mat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");      
        mat.setColor("Color", color);
        geom.setMaterial(mat);
        pivot.attachChild(geom) 
    }
    
    void markOrigin() {
        (1..20).each {
            makeUnitBox(10*it,0,0, ColorRGBA.Black)
        }
        (1..20).each {
            makeUnitBox(0,10*it,0, ColorRGBA.Gray)
        }
        (1..20).each {
            makeUnitBox(0,0,10*it, ColorRGBA.LightGray)
        }
    }
    
    void makeBox(String projectName, int popularity, int imports, int size) {
        Box b = new Box(new Vector3f(popularity / 10,imports / 10, 0), new Vector3f(popularity / 10 + 1,imports / 10+1, size/100));
        Geometry geom = new Geometry(projectName, b);
        spatialsByName[projectName] = geom
        TangentBinormalGenerator.generate(b);
        geom.setMaterial(mat);
        pivot.attachChild(geom);     
    }
	
}

