package Starter;

import General.AIState;
import General.Main;
import General.Settings;
import Data.DataUpdater;

public class StarterClass {


    public static void main(String[] args) {
        if(!Settings.aiState.equals(AIState.Play) && !Settings.aiState.equals(AIState.None)) DataUpdater.updateAllFileTags();
        for (int i = 0; i < Settings.aiCount; i++){
            new Main();
        }

    }
}
