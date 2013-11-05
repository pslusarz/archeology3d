/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package jme3test.helloworld;

import com.jme3.app.SimpleApplication;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Matrix3f;
import com.jme3.math.Vector3f;
import com.jme3.scene.Geometry;
import com.jme3.scene.shape.Box;

/**
 *
 * @author ps
 */
public class HelloJME3 extends SimpleApplication {
    public static void main(String[] args) {
        HelloJME3 app = new HelloJME3();
        app.start();
    }
    @Override
    public void simpleInitApp() {
        for (int i = 0; i < 1; i++) {
          Box b = new Box(new Vector3f(0,0,0), new Vector3f(5,10,20));
          
        Geometry geom = new Geometry("Box", b);
        Material mat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        mat.setColor("Color", ColorRGBA.Cyan);
        geom.setMaterial(mat);
        geom.setLocalTranslation(Vector3f.UNIT_X);
        //geom.setLocalRotation(Matrix3f.ZERO);
        rootNode.attachChild(geom);
        
        }
        
//        for (int i = 0; i < 18; i++) {
//          Box b = new Box(new Vector3f(-5,-5,i), 0.5f, 0.5f, 0.5f);
//        Geometry geom = new Geometry("Box", b);
//        Material mat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
//        mat.setColor("Color", ColorRGBA.Yellow);
//        geom.setMaterial(mat);
//        rootNode.attachChild(geom);
//        
//        }
//        
//                for (int i = 0; i < 5; i++) {
//          Box b = new Box(new Vector3f(-3,-2,i), 0.5f, 0.5f, 0.5f);
//        Geometry geom = new Geometry("Box", b);
//        Material mat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
//        mat.setColor("Color", ColorRGBA.Red);
//        geom.setMaterial(mat);
//        rootNode.attachChild(geom);
//        
//        }
                
                                for (int i = 0; i < 3; i++) {
          Box b = new Box(new Vector3f(2,2,i), 0.5f, 0.5f, 0.5f);
        Geometry geom = new Geometry("Box", b);
        Material mat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        mat.setColor("Color", ColorRGBA.Green);
        geom.setMaterial(mat);
        rootNode.attachChild(geom);
        
        }
    }
    
}
