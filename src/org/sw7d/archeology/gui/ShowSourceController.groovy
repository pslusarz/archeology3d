/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.sw7d.archeology.gui

import de.lessvoid.nifty.screen.ScreenController
import de.lessvoid.nifty.controls.Label
import de.lessvoid.nifty.screen.Screen
import de.lessvoid.nifty.Nifty
import de.lessvoid.nifty.controls.textfield.TextFieldControl

class ShowSourceController implements ScreenController {
    Label tf
    
    public void bind(Nifty nifty, Screen screen) {
        System.out.println("bind( " + screen.getScreenId() + ")");
        tf = screen.findNiftyControl("sourcecode", Label.class);
        tf.text = new File("src/org/sw7d/archeology/gui/GroovyMain.groovy").text
    }
    
    public void onStartScreen() {
        System.out.println("onStartScreen");
    }

    public void onEndScreen() {
        System.out.println("onEndScreen");
    }
	
}

