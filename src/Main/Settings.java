package Main;

import Data.DataUpdater;

public class Settings {

    public static AIState aiState = AIState.Learn;
    public static int aiCount = 15;
    public static boolean overwrite = true;

    //region GENERAL
    public static boolean running = true;

    //How many pictures it can learn until gets saved
    public static int maxAttemptCount = 2000;
    //How many times it gets saved until it gets reset
    public static int maxLearningAttempts = 50;

    public static int pictureWidth = 400;
    public static int pictureHeight = 250;
    //endregion

    //region NEURAL NETWORK
    public static float nLernRate = .01f; // Lernrate des NNs
    public static int NNLayerCount = 1;
    public static int NNLayerLength = 128;
    //endregion

    //region CONVOLUTIONAL NEURAL NETWORK
    public static float cLernRate = .01f; // Lernrate des CNNs
    public static int poolingTimes = 3;
    public static int[] kernelCount = new int[]{32,16};
    public static boolean[] withPooling = new boolean[]{true,true};
    public static int kernelWidth = 3;
    public static int kernelHeight = 3;
    //endregion

    //region GETTER & SETTER
    /**
     * Resumes the AI
     */
    public static void play(){
        if(!aiState.equals(AIState.Learn) && !aiState.equals(AIState.Play) && !aiState.equals(AIState.None))DataUpdater.updateAllFileTags();
        running = true;
    }

    /**
     * Stops the AI
     */
    public static void pause(){
        running = false;
    }

    /**
     * @return How many pictures should be shown in the GUI
     */
    public static int getImageNum(){
        if(!Settings.aiState.equals(AIState.Play)) return kernelCount.length + 3;
        else return 1;
    }

    /**
     * If the AI should overwrite the saved Data
     */
    public static void stopOverwriting(){
        overwrite = false;
    }

    /**
     * @return Neural Network Input Length, depends on the layers of the CNN
     */
    public static int getNNInputLength(){
        int tempWidth = (int) Math.ceil(pictureWidth/(Math.pow(2,poolingTimes)));
        int tempHeight = (int) Math.ceil(pictureHeight/Math.pow(2,poolingTimes));

        for(int i = 0;i < kernelCount.length;i++){
            tempWidth -= kernelWidth - 1;
            tempHeight -= kernelHeight - 1;
            if(withPooling[i]){
                tempWidth = Math.round(tempWidth/2f);
                tempHeight = Math.round(tempHeight/2f);
            }
        }
        return tempWidth * tempHeight * kernelCount[kernelCount.length-1];
    }

    //endregion
}

