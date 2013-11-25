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
import com.jme3.font.BitmapFont
import com.jme3.font.Rectangle
import com.jme3.math.FastMath
import com.jme3.renderer.queue.RenderQueue.Bucket
import com.jme3.niftygui.NiftyJmeDisplay
import de.lessvoid.nifty.Nifty
/**
 *
 * @author ps
 */
class GroovyMain extends SimpleApplication {
    static void main(args){
        
        new GroovyMain(pauseOnLostFocus: false, displayStatView: false, displayFps: false).start()
    }
    
    Node pivot
    Material mat, selectedMaterial, originMaterial
    Modules modules
    List<String> selectedModules
    Map<String, ArcheologyFile> selectedFilesByName = [:]
    Map<String, Geometry> spatialsByName = [:]
    def javaFiles
    def javaNames
    def namesByPopularity
    final int MAX_CLASSES = 100
    Geometry selected
    BitmapText backgroundOperation
    BitmapText currentSelection
    BitmapText selectionOrigin
    BitmapText help
    boolean loadedModules = false
    
    
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
      //jme3tools.optimize.GeometryBatchFactory.optimize(rootNode);
  
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
        
        SelectModuleController selectModuleController = new SelectModuleController()
        stateManager.attach(selectModuleController)
        
        NiftyJmeDisplay niftyDisplay = new NiftyJmeDisplay(assetManager,
            inputManager,
            audioRenderer,
            guiViewPort);
        nifty = niftyDisplay.getNifty();
        guiViewPort.addProcessor(niftyDisplay);
        nifty.validateXml("Interface/selectModule.xml")
        nifty.fromXml("Interface/selectModule.xml", "nothing", selectModuleController)
        
        
        
        
        makeGraphFromPickle()

        


    }
    
    def initKeys() {
        handleAction("ZoomOut", new KeyTrigger(KeyInput.KEY_H), {boolean keyPressed, float tpf -> if (!keyPressed) {flyCam.moveCamera(-10, false)}})
        handleAction("ZoomIn", new KeyTrigger(KeyInput.KEY_J), {boolean keyPressed, float tpf -> if (!keyPressed) {flyCam.moveCamera(10, false)}})
        handleAction("SelectModules", new KeyTrigger(KeyInput.KEY_L), {boolean keyPressed, float tpf -> if (!keyPressed && loadedModules) {displaySelectModulesDialog()}})
        handleAction("ViewSource", new KeyTrigger(KeyInput.KEY_V), {boolean keyPressed, float tpf -> if (!keyPressed) {displayViewSource()}})
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
                    displayAllMeetingCriteria {Geometry toBeDisplayed -> !selected || toBeDisplayed == selected || onlySelectedModules().findFirstClassFile(toBeDisplayed.name).imports?.contains(selected.name)}  
                }
            })
         
        handleAction("Imports", new KeyTrigger(KeyInput.KEY_I), 
            {boolean keyPressed, float tpf -> if (!keyPressed) {
                    displayAllMeetingCriteria {Geometry toBeDisplayed ->  !selected || toBeDisplayed == selected || selectedFilesByName[selected.name].imports.contains(toBeDisplayed.name)}
                }
            })
        
    }
    
    Modules onlySelectedModules() {
        Modules result = new Modules()
        result.addAll( modules.findAll {selectedModules.contains(it.name)})
        return result
    }
    
    void displayAllMeetingCriteria(Closure criteria) {
        selectionOrigin.text = ""
        spatialsByName.each { String name, Geometry currentSpatial ->
            if (!selectedFilesByName[currentSpatial.name] || !criteria.call(currentSpatial)) {
                pivot.detachChild(currentSpatial) 
            } else {
                reset(currentSpatial)
            }
            
            if (selected == currentSpatial) {
                selected.setMaterial(originMaterial)
                selectionOrigin.text = "Origin: "+selected.name

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
        currentSelection.text = "Selected: ${selected?.name?:'none'}"
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
        guiFont = assetManager.loadFont("Interface/Fonts/Default.fnt");
        BitmapText ch = new BitmapText(guiFont, false);
        ch.setSize(guiFont.getCharSet().getRenderedSize() * 2);
        ch.setText("+"); // crosshairs
        ch.setLocalTranslation( // center
            (float) (settings.getWidth() / 2 - ch.getLineWidth()/2), (float) (settings.getHeight() / 2 + ch.getLineHeight()/2), 150f);
        guiNode.attachChild(ch);
        
        help = makeHUDText(10, settings.getHeight() - guiFont.charSet.lineHeight, ColorRGBA.Blue) 
        help.text = "Esc - quit, click - select, K - selection importers, I - selection imports, L - select modules, H/J - zoom"
        backgroundOperation = makeHUDText(10, settings.getHeight() - guiFont.charSet.lineHeight - guiFont.charSet.lineHeight *1.5, ColorRGBA.Red)       
        currentSelection = makeHUDText(settings.getWidth() / 2.5, guiFont.charSet.lineHeight, ColorRGBA.Orange)       
        selectionOrigin = makeHUDText(10, guiFont.charSet.lineHeight, ColorRGBA.Blue)

    }
    
    BitmapText makeHUDText(x, y, ColorRGBA color) {
        BitmapText result = new BitmapText(guiFont, false);
        result.setSize(guiFont.getCharSet().getRenderedSize());      // font size
        result.setColor(color);                             // font color
        result.setText("");             // the text
        result.setLocalTranslation((float) x, (float) y, 0f); // position
        guiNode.attachChild(result);
        return result
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
    
    
    long lastUpdateTime = System.currentTimeMillis()
    
    @Override
    public void simpleUpdate(float tpf) {
        if (!loadedModules) {
            if (System.currentTimeMillis() - lastUpdateTime > 1000) {
                lastUpdateTime = System.currentTimeMillis()
                backgroundOperation.text = backgroundOperation.text+"."
            }
            return
        }
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

            }
            backgroundOperation.text = "Initializing classes (${currentModule} / ${Math.min(MAX_CLASSES, namesByPopularity.size())})"
        } else {
            backgroundOperation.text = ""
        }
        
           
    } 
    
    void makeQuickGraph() {
        Apache5000.classes.each {
            makeBox(it.name, it.popularity, it.imports, it.lines)
        }
    }
    
    
    void makeGraphFromPickle() {
        backgroundOperation.setText("PATIENce MORTAL, loADING SOM3 DATAZ")
        new Thread() {
            void run() {               
                modules = Modules.create()
                selectModules (modules.collect {it.name})
        
                javaFiles = modules*.files.flatten().findAll{!it.javaName()?.startsWith('java') && it.extension() == 'java'}
                javaNames = javaFiles*.javaName()
                namesByPopularity = modules*.files*.imports.flatten().findAll{!it?.startsWith('java') && it}.groupBy {it}.sort {a, b -> -a.value.size() <=>-b.value.size()}
        
                backgroundOperation.setText("")
                loadedModules = true
            }
        }.start()
        
        
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
        
        BitmapFont fnt = assetManager.loadFont("Interface/Fonts/Default.fnt");
        BitmapText txt = new BitmapText(fnt, false);
        txt.setBox(new Rectangle(0, 30, 100, 0));       
        txt.setSize( 15f );
        txt.setColor(ColorRGBA.Black)
        txt.setText("< Popularity");
        txt.rotate(0f, 0f, (float)FastMath.DEG_TO_RAD * (180));
        txt.setLocalTranslation(75f,-75f,0f)
        pivot.attachChild(txt);
        
        BitmapText txt2 = new BitmapText(fnt, false);
        txt2.setBox(new Rectangle(0, 30, 100, 0));       
        txt2.setSize( 15f );
        txt2.setColor(ColorRGBA.Black)
        txt2.setText("Imports >");
        txt2.rotate(0f, 0f, (float)FastMath.DEG_TO_RAD * (90));
        txt2.setLocalTranslation(-25f, 30f, 0f)
        pivot.attachChild(txt2);
        
        BitmapText txt3 = new BitmapText(fnt, false);
        txt3.setBox(new Rectangle(0, 0, 100, 30));       
        txt3.setSize( 15f );
        txt3.setColor(ColorRGBA.Black)
        txt3.setText("Size >");
        txt3.rotate((float)FastMath.DEG_TO_RAD * 45, (float)FastMath.DEG_TO_RAD * (90), (float)FastMath.DEG_TO_RAD * (180));
        txt3.setLocalTranslation(-100f,-120f,30f)
        pivot.attachChild(txt3);
        
        
    }
    
    void makeBox(String projectName, int popularity, int imports, int size) {
        //size 2 box, useful for drilling down in heavily populated zones
        Box b = new Box(new Vector3f(popularity / 5,imports / 5, size / 100-3), new Vector3f(popularity / 5 + 1,imports / 5+1, size/100));
        //Box b = new Box(new Vector3f(popularity / 10,imports / 10, 0), new Vector3f(popularity / 10 + 1,imports / 10+1, size/100));
        Geometry geom = new Geometry(projectName, b);
        spatialsByName[projectName] = geom
        TangentBinormalGenerator.generate(b);
        geom.setMaterial(mat);
        pivot.attachChild(geom);     
    }
    
    boolean displayingSelectModulesDialog = false
    boolean displayingViewSource = false
    Nifty nifty
    
    void displayViewSource() {
        if (displayingViewSource) {
            nifty.gotoScreen("nothing")
            flyCam.setEnabled(true);
            inputManager.setCursorVisible(false);
            displayingViewSource = false
        } else if (selected) {
          displayingViewSource = true
          nifty.gotoScreen("source")
          //nifty.fromXml("Interface/displaySource.xml", "start", new ShowSourceController());
          //flyCam.setEnabled(false);
          //inputManager.setCursorVisible(true);
        }
        
    }
     
    void displaySelectModulesDialog() {
        if (displayingSelectModulesDialog) { return}
        displayingSelectModulesDialog = true
        //nifty.fromXml("Interface/selectModule.xml", "start", new SelectModuleController(app: this));
        nifty.gotoScreen("start")
        flyCam.setEnabled(false);
        inputManager.setCursorVisible(true);
    }
    
    void doneSelectingModules(List<String> selectedModules2)  { 
        println "Selected modules: "+selectedModules2
        nifty.gotoScreen("nothing")
        flyCam.setEnabled(true);
        inputManager.setCursorVisible(false);
        selectModules(selectedModules2)
        
        displayAllMeetingCriteria {Geometry toBeDisplayed -> true}
        displayingSelectModulesDialog = false
    }
    
    def selectModules(List<String> selectedModules2) {
        List<ArcheologyFile> selectedModuleFiles = modules.findAll {selectedModules2.contains(it.name)}*.files.flatten()
        selectedFilesByName.clear()
        selectedModuleFiles.each {
            selectedFilesByName[it.javaName()] = it
        }
        this.selectedModules = selectedModules2
        if (selected && !selectedFilesByName[selected.name]) {
            select(null)
        }
    }
	
}

