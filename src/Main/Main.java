package Main;

import Data.DataManager;
import GUI.GUIManager;
import Pictures.PictureManager;
import Player.AI;
import com.github.kwhat.jnativehook.GlobalScreen;
import com.github.kwhat.jnativehook.NativeHookException;

public class Main{

    public Main(){
        GUIManager guiManager = new GUIManager();
        PictureManager pictureManager = new PictureManager();

        if(Settings.aiState.equals(AIState.Data)){
            try {
                GlobalScreen.registerNativeHook();
            } catch (NativeHookException e) {
                e.printStackTrace();
            }
            DataManager dataManager = new DataManager(pictureManager);
            GlobalScreen.addNativeKeyListener(dataManager);
        }else if(Settings.aiState.equals(AIState.Learn) || Settings.aiState.equals(AIState.Play)){
            new AI(pictureManager, guiManager).start();
        }else if(Settings.aiState.equals(AIState.None)){
            new AI(pictureManager, guiManager);
        }

    }
}
