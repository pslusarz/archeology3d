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
import org.sw7d.archeology.data.DataPointProvider
import org.sw7d.archeology.data.DataPoint3d
import com.jme3.system.AppSettings
import org.sw7d.archeology.data.DefaultDataPointProvider
import com.jme3.scene.BatchNode
import com.jme3.collision.CollisionResult

class GroovyMain extends SimpleApplication {
    static void main(args){
        AppSettings settings = new AppSettings(true)
        settings.setResolution(1280,1048)
        settings.vSync = true
        settings.samples = 0
        settings.title = 'Archeology3D'
        new GroovyMain(pauseOnLostFocus: false, displayStatView: false, displayFps: false, showSettings: false, settings: settings).start()
    }
    
    Material mat 
    Map<String, ArcheologyFile> selectedFilesByName = [:]
    Map<String, Geometry> spatialsByName = [:]
    Map<Geometry, DataPoint3d> dataPointsByGeometry = [:]
    Geometry selected
    BitmapText backgroundOperation
    BitmapText currentSelection
    BitmapText selectionOrigin
    BitmapText help
    DataPointProvider provider
    BatchNode batchNode
    
    @Override
    void simpleInitApp() {
        switchFirstPersonNavigation(false)
        batchNode = new BatchNode()
        rootNode.attachChild(batchNode)
        provider = new DefaultDataPointProvider(maxDataPoints: 5000)
        def al = new AmbientLight()
        al.setColor(ColorRGBA.White.mult(0.5f))
        rootNode.addLight(al)
        
        viewPort.setBackgroundColor(ColorRGBA.White);
        makeLight(100,1500,1500, ColorRGBA.White) 
        
        markOrigin()

        rootNode.rotate(-1.5f, 0f, 0f)

        resetCamera()
        flyCam.setMoveSpeed((float) (flyCam.getMoveSpeed() * 10f));
        
        initKeys()
        initCrossHairs() 
        mat = makeMaterial(ColorRGBA.Blue)

        ShowSourceController showSourceController = new ShowSourceController()
        stateManager.attach(showSourceController)
        
        NiftyJmeDisplay niftyDisplay = new NiftyJmeDisplay(assetManager,
            inputManager,
            audioRenderer,
            guiViewPort);
        nifty = niftyDisplay.getNifty();
        guiViewPort.addProcessor(niftyDisplay);
        nifty.validateXml("Interface/selectModule.xml")
        nifty.fromXml("Interface/selectModule.xml", "nothing", showSourceController)
        
        
        
        
        makeGraphFromPickle()

        


    }
    
    def resetCamera() {
        cam.setLocation(new Vector3f(150f, 150f, -150f))
        cam.setRotation(new Quaternion(-1.5f, 0f, 0f, 1f))
        cam.lookAt(new Vector3f(-150f, -130f, 150f), rootNode.getLocalTranslation())
    }
    
    def initKeys() {
        handleAction("ResetCamera", new KeyTrigger(KeyInput.KEY_C), {boolean keyPressed, float tpf -> if (!keyPressed) {resetCamera()}})
        handleAction("FirstPersonNavigation", new KeyTrigger(KeyInput.KEY_M), {boolean keyPressed, float tpf -> if (!keyPressed) {switchFirstPersonNavigation(!firstPersonNavigation)}})
        handleAction("ZoomOut", new KeyTrigger(KeyInput.KEY_H), {boolean keyPressed, float tpf -> if (!keyPressed) {flyCam.moveCamera(-10, false)}})
        handleAction("ZoomIn", new KeyTrigger(KeyInput.KEY_J), {boolean keyPressed, float tpf -> if (!keyPressed) {flyCam.moveCamera(10, false)}})
        handleAction("ViewSource", new KeyTrigger(KeyInput.KEY_V), {boolean keyPressed, float tpf -> if (!keyPressed) {displayViewSource()}})
        handleAction("Select", new MouseButtonTrigger(MouseInput.BUTTON_LEFT), 
            {boolean keyPressed, float tpf -> if (!keyPressed) {
                    CollisionResults results = new CollisionResults()
                    Ray ray = new Ray(cam.location, cam.direction)
                    batchNode.collideWith(ray, results)
                    println results
                    results.each { CollisionResult cr ->
                        println cr.getGeometry().name +" distance "+cr.getDistance() 
                    }
                    select(results.closestCollision?.geometry)
                    println results.closestCollision?.geometry?.name
                }
            })
        
        handleAction("RunScript", new KeyTrigger(KeyInput.KEY_R), 
            {boolean keyPressed, float tpf -> if (!keyPressed) {
                    Binding binding = new Binding();
                    binding.setVariable("foo", new Integer(2));
                    binding.setVariable("modules", Modules.create())
                    GroovyShell shell = new GroovyShell(binding);
                    File scriptFile = new File("scripts/runtime/DefaultScript.groovy")
                    println "script file needs to be in: "+scriptFile.canonicalPath+ " (${scriptFile.exists()?'it exists':'it does not exist'})"
                    Object value
                    try {
                        value = shell.evaluate(scriptFile);
                    } catch (Exception e) {
                        e.printStackTrace()
                        println "error evaluating script: "+e.getMessage()
                    }
                    println "returned value: "+value
                    if (value instanceof DataPointProvider) {
                        DataPointProvider newProvider = value as DataPointProvider
                        newProvider.loadedModules = false
                        batchNode.detachAllChildren()
                        spatialsByName.clear()
                        dataPointsByGeometry.clear()
                        provider = newProvider
                        markOrigin()
                        newProvider.loadedModules = true
                    }
                }
            })
        
    }
    
    void reset(Geometry geometry) {
        batchNode.attachChild(geometry)
        geometry.setMaterial(mat)
    }
    
    void select(Geometry selection) {
        selected = selection
        currentSelection.text = "Selected: ${selected?.name?:'none'}"
        DataPoint3d dp = dataPointsByGeometry[selection]
        if (dp) {
            selectionOrigin.text = "${provider.xLabel}: ${dp.x}\n${provider.yLabel}: ${dp.y}\n${provider.zLabel}: ${dp.z}"
        } else {
            selectionOrigin.text = ""
        }
    }
    
    void handleAction (String aname, Trigger trigger, Closure handler) {
        ActionListener actionListener = new ActionListener() {
            String actionName = aname
            void onAction(String name, boolean keyPressed, float tpf) {
                if (name == actionName) {
                    if (!keyPressed) println "======== $actionName =========="
                    handler.call(keyPressed, tpf) //TODO: is this a bug? called several times while key down?
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
        help.text = "Esc - quit, M - toggle mouse mode, click - select, V - view source, H/J - zoom, C - center camera, R - run script"
        backgroundOperation = makeHUDText(10, settings.getHeight() - guiFont.charSet.lineHeight - guiFont.charSet.lineHeight *1.5, ColorRGBA.Red)       
        currentSelection = makeHUDText(settings.getWidth() / 2.5, guiFont.charSet.lineHeight, ColorRGBA.Orange)       
        selectionOrigin = makeHUDText(10, guiFont.charSet.lineHeight * 4, ColorRGBA.Brown)

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
    
    Material makeMaterial(ColorRGBA color) {
        Material result = new Material(assetManager, "Common/MatDefs/Light/Lighting.j3md");
        result.setBoolean('UseMaterialColors', true)
        result.setColor('Diffuse', color)
        result.setColor('Ambient', color)
        result.setColor('Specular', ColorRGBA.White)
        result.setFloat('Shininess', 64f)
        return result
    }
    
    
    long lastUpdateTime = System.currentTimeMillis()
    boolean lastBox = false
    @Override
    public void simpleUpdate(float tpf) {
        if (!provider.loadedModules) {
            if (System.currentTimeMillis() - lastUpdateTime > 1000) {
                lastUpdateTime = System.currentTimeMillis()
                backgroundOperation.text = backgroundOperation.text+"."
            }
            return
        }
        DataPoint3d dataPoint = provider.getNextDataPoint()
        if (dataPoint) {
            makeBox(dataPoint)
            backgroundOperation.text = "Initializing classes (${provider.dataPointCompletionRatio})"
            lastBox = true
            rootNode.detachChild(batchNode)
        } else {
            if (lastBox) {
                backgroundOperation.text = ""
                //jme3tools.optimize.GeometryBatchFactory.optimize(batchNode)
                println "now preparing to display all nodes "+new Date()

                batchNode.batch()
                rootNode.attachChild(batchNode)
                lastBox = false
                println "now done preparing to display all nodes "+new Date()
            }
        }
        
           
    } 
    
    void makeGraphFromPickle() {
        backgroundOperation.setText("PATIENce MORTAL, loADING SOM3 DATAZ")
        new Thread() {
            void run() {               
                provider.initModules()
                //selectModules (provider.modules.collect {it.name})      
                backgroundOperation.setText("")
                provider.loadedModules = true
            }
        }.start()
        
        
    }
    
    
    void makeLight(def x, y, z, color) {
        makeUnitBox(x,y,z,color)
        Vector3f sunVector = new Vector3f(x,y,z)    
        DirectionalLight sun = new DirectionalLight();
        sun.setDirection(sunVector);
        sun.setColor(color);
        rootNode.addLight(sun);
        
    }
    
    Map<ColorRGBA, Material> materialsByColor = [:].withDefault{ColorRGBA color -> 
      Material mat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md")      
      mat.setColor("Color", color)
      return mat
    }
    
    void makeUnitBox(def x, y, z, color) {
        Vector3f sunVector = new Vector3f(x,y,z)
        Box b = new Box(sunVector, 0.5f, 0.5f, 0.5f);
        Geometry geom = new Geometry("Box", b);
        geom.setMaterial(materialsByColor[color]);
        batchNode.attachChild(geom) 
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
        txt.setText("< ${provider.getXYZLabels()[0]}");
        txt.rotate(0f, 0f, (float)FastMath.DEG_TO_RAD * (180));
        txt.setLocalTranslation(75f,-75f,0f)
        batchNode.attachChild(txt);
        
        BitmapText txt2 = new BitmapText(fnt, false);
        txt2.setBox(new Rectangle(0, 30, 100, 0));       
        txt2.setSize( 15f );
        txt2.setColor(ColorRGBA.Black)
        txt2.setText("${provider.getXYZLabels()[1]} >");
        txt2.rotate(0f, 0f, (float)FastMath.DEG_TO_RAD * (90));
        txt2.setLocalTranslation(-25f, 30f, 0f)
        batchNode.attachChild(txt2);
        
        BitmapText txt3 = new BitmapText(fnt, false);
        txt3.setBox(new Rectangle(0, 0, 100, 30));       
        txt3.setSize( 15f );
        txt3.setColor(ColorRGBA.Black)
        txt3.setText("${provider.getXYZLabels()[2]} >");
        txt3.rotate((float)FastMath.DEG_TO_RAD * 45, (float)FastMath.DEG_TO_RAD * (90), (float)FastMath.DEG_TO_RAD * (180));
        txt3.setLocalTranslation(-100f,-120f,30f)
        batchNode.attachChild(txt3);
        
        
    }
    
    void makeBox(DataPoint3d dataPoint) {
        //size 2 box, useful for drilling down in heavily populated zones
        int boxHeight = 3
        int boxSideLength = 1
        Box b = new Box(new Vector3f(dataPoint.x,dataPoint.y, dataPoint.z - boxHeight), new Vector3f(dataPoint.x + boxSideLength,dataPoint.y + boxSideLength, dataPoint.z));
        //Box b = new Box(new Vector3f(popularity / 10,imports / 10, 0), new Vector3f(popularity / 10 + 1,imports / 10+1, size/100));
        Geometry geom = new Geometry(dataPoint.name, b);
        //geom.setUserData("dataPoint", dataPoint)
        spatialsByName[dataPoint.name] = geom
        dataPointsByGeometry[geom] = dataPoint
        TangentBinormalGenerator.generate(b);
        geom.setMaterial(mat);
        batchNode.attachChild(geom);     
    }
    
    //boolean displayingSelectModulesDialog = false
    boolean displayingViewSource = false
    Nifty nifty
    
    boolean firstPersonNavigation = false
    public switchFirstPersonNavigation(boolean newValue) {
       firstPersonNavigation = newValue
       flyCam.enabled = firstPersonNavigation
       inputManager.cursorVisible = !firstPersonNavigation
    }
    
    void displayViewSource() {
        if (displayingViewSource) {
            nifty.gotoScreen("nothing")
            switchFirstPersonNavigation(true)
            displayingViewSource = false
        } else if (selected) {
            displayingViewSource = true
            switchFirstPersonNavigation(false)
            nifty.gotoScreen("source")
        }
        
    }
     
    //    void displaySelectModulesDialog() {
    //        if (displayingSelectModulesDialog) { return}
    //        displayingSelectModulesDialog = true
    //        //nifty.fromXml("Interface/selectModule.xml", "start", new SelectModuleController(app: this));
    //        nifty.gotoScreen("start")
    //        flyCam.setEnabled(false);
    //        inputManager.setCursorVisible(true);
    //    }
    
    //    void doneSelectingModules(List<String> selectedModules2)  { 
    //        println "Selected modules: "+selectedModules2
    //        nifty.gotoScreen("nothing")
    //        flyCam.setEnabled(true);
    //        inputManager.setCursorVisible(false);
    //        selectModules(selectedModules2)
    //        
    //        displayAllMeetingCriteria {Geometry toBeDisplayed -> true}
    //        displayingSelectModulesDialog = false
    //    }
    
    //    def selectModules(List<String> selectedModules2) {
    //        List<ArcheologyFile> selectedModuleFiles = provider.modules.findAll {selectedModules2.contains(it.name)}*.files.flatten()
    //        selectedFilesByName.clear()
    //        selectedModuleFiles.each {
    //            selectedFilesByName[it.javaName()] = it
    //        }
    //        this.selectedModules = selectedModules2
    //        if (selected && !selectedFilesByName[selected.name]) {
    //            select(null)
    //        }
    //    }
    
    Modules getAvailableModules() {
        Modules.create()
    }
	
}

