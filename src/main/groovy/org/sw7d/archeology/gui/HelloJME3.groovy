/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.sw7d.archeology.gui;

import com.jme3.app.SimpleApplication;
import com.jme3.niftygui.NiftyJmeDisplay;
import de.lessvoid.nifty.Nifty;
import org.sw7d.archeology.gui.ShowSourceController;

public class HelloJME3 extends SimpleApplication {

    private Nifty nifty;

    public static void main(String[] args) {
        HelloJME3 app = new HelloJME3();
        app.setPauseOnLostFocus(false);
        app.start();
    }

    public void simpleInitApp() {
        NiftyJmeDisplay niftyDisplay = new NiftyJmeDisplay(assetManager,
                inputManager,
                audioRenderer,
                guiViewPort, 2048, 2048);
        nifty = niftyDisplay.getNifty();
        ShowSourceController controller = new ShowSourceController()
        //controller.setApp(this);
        nifty.fromXml("Interface/selectModule.xml", "source", controller);

        // attach the nifty display to the gui view port as a processor
        guiViewPort.addProcessor(niftyDisplay);

        // disable the fly cam
        flyCam.setEnabled(false);
        inputManager.setCursorVisible(true);
    }
    
    public void doneSelecting() {
        System.out.println("APP received done notification");
        nifty.exit();
        flyCam.setEnabled(true);
        inputManager.setCursorVisible(false);
    }
}