package Player;

import General.Action;
import General.AIState;
import Pictures.PictureManager;
import General.Settings;
import GUI.GUIManager;

import java.awt.*;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.io.*;

public class AI extends Thread {

    private final NN neuralNetwork;
    private final CNN convolutionalNetwork;
    private final PictureManager pictureManager;
    private final GUIManager guiManager;
    private float accuracy;
    private int correctGuessCount;

    //How many pictures it can learn until gets saved
    private final int maxAttemptCount;
    private int currentAttempts;

    //How many times it gets saved until it gets reset
    private final int maxLearningAttempts;
    private int currentLearningAttempts;


    public AI(PictureManager pictureManager, GUIManager guiManager){
        this.pictureManager = pictureManager;
        this.guiManager = guiManager;
        maxAttemptCount = Settings.maxAttemptCount;
        maxLearningAttempts = Settings.maxLearningAttempts;
        convolutionalNetwork = new CNN();
        neuralNetwork = new NN();
        convolutionalNetwork.buildConvolutionLayers();
        neuralNetwork.ebenenErstellen();
        propagation();
        updateGUILayerInfo();

        if(Settings.aiState.equals(AIState.Play)) load();
        else if(Settings.aiState.equals(AIState.Learn)){
            int rand = (int)Math.round(Math.random());
            reset();
            if(rand == 0) load();
        }
    }

    //region PLAY & LEARN

    @Override
    public void run() {
        while (true){
            if(Settings.running){
                if(Settings.aiState.equals(AIState.Learn)) learn();
                else play();
            }
            try {
                if(Settings.aiState.equals(AIState.Play)) Thread.sleep(200);
                else Thread.sleep(0);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Teaches the AI
     */
    private void learn(){
        propagation();
        updateAccuracy(neuralNetwork.getOutput(),pictureManager.getActionShown());
        backPropagation();
        updateGUIImage();
    }

    /**
     * Transfers the output of the AI to the keyboard
     */
    private void play(){

        float[][][] p = pictureManager.getPixel();
        float[][][] temp = pictureManager.getPixel();
        //Coordinates to recognize whether the AI failed or not
        int x = 100;
        int y = 249;
        int c = 2;
        p[0][x][y] = 0;
        p[1][x][y] = 0;
        p[2][x][y] = 0;
        guiManager.updateImages(new BufferedImage[]{convolutionalNetwork.createImage(p[0],p[1],p[2])});

        //if pause menu
        if(temp[c][x][y] == 0.6666667f){
            Settings.pause();
            Robot robot;
            try {
                robot = new Robot();
                robot.delay(1500);
                robot.keyPress(KeyEvent.VK_ESCAPE);
                robot.keyRelease(KeyEvent.VK_ESCAPE);
                robot.delay(1500);
                robot.mousePress(InputEvent.BUTTON1_DOWN_MASK);
                robot.mouseRelease(InputEvent.BUTTON1_DOWN_MASK);
                robot.delay(3000);
                Settings.play();
            } catch (AWTException e) {
                e.printStackTrace();
            }
        }else{
            propagation();
            guiManager.updateAction(neuralNetwork.getOutput(), neuralNetwork.getOutputText());
            Robot robot;
            try {
                robot = new Robot();
                int keyCode = neuralNetwork.getOutput().getKeycode();
                if(keyCode != 0) {
                    robot.delay(50);
                    robot.keyPress(keyCode);
                    robot.delay(250);
                    robot.keyRelease(keyCode);
                }
            } catch (AWTException e) {
                e.printStackTrace();
            }
        }
    }

    //endregion

    //region GUI

    /**
     * Updates all GUI images
     */
    private void updateGUIImage(){
        guiManager.updateImages(convolutionalNetwork.ausgabe());
    }

    /**
     * Updates the Layer Information int the GUI
     */
    private void updateGUILayerInfo(){
        guiManager.displayLayerInfo();
    }

    //endregion

    //region PROPAGATION

    /**
     * Propagation
     */
    private void propagation(){
        convolutionalNetwork.setPixel(pictureManager.getPixel());
        convolutionalNetwork.propagation();
        neuralNetwork.setInput(convolutionalNetwork.flatten());
        neuralNetwork.propagation();

    }

    //endregion

    //region BACK - PROPAGATION
    /**
     * Starts the Backpropagation in NN and CNN
     */
    public void backPropagation() {
        Action target = pictureManager.getActionShown();
        neuralNetwork.backPropagation(target);
        convolutionalNetwork.backPropagation(neuralNetwork.getChain());
    }

    /**
     * Updates the accuracy of the AI
     * @param output output of the NN
     * @param target what the AI should have done
     */
    private void updateAccuracy(Action output, Action target){
        if(output.equals(target)) correctGuessCount +=1;
        currentAttempts += 1;
        accuracy = correctGuessCount / (currentAttempts *1f) *100;
        guiManager.updateAccuracy(accuracy, getSavedAccuracy(), currentAttempts, neuralNetwork.getOutputText(), target);
        updateGeneration();
    }

    /**
     * Saves the AI if it learned a specific number of pictures "maxAttemptCount"
     * And resets the AI after some time "maxLearningAttempts"
     */
    private void updateGeneration(){
        //until it gets saved
        if(currentAttempts > maxAttemptCount){
            currentAttempts = 0;
            correctGuessCount = 0;
            save();
            currentLearningAttempts +=1;
            //until it gets reset
            if(currentLearningAttempts >= maxLearningAttempts){
                currentLearningAttempts = 0;
                int rand = (int)Math.round(Math.random());
                if(rand == 0) load();
                else reset();
            }
        }
    }

    //endregion

    //region SAVE & LOAD
    /**
     * Saves the AI
     */
    private void save() {
        System.out.println("Saving " + getSavedAccuracy() + " ----------------------------------------------------------" + accuracy);
        if(Settings.overwrite || accuracy > getSavedAccuracy()) {
            Settings.stopOverwriting();
            try{
                FileOutputStream cnnFile = new FileOutputStream("cnnSave.dat");
                FileOutputStream fcFile = new FileOutputStream("fcSave.dat");
                BufferedOutputStream bfCNN = new BufferedOutputStream(cnnFile);
                BufferedOutputStream bfFC = new BufferedOutputStream(fcFile);
                ObjectOutputStream objCNN = new ObjectOutputStream(bfCNN);
                ObjectOutputStream objFC = new ObjectOutputStream(bfFC);

                CNNData cnnData = convolutionalNetwork.save();
                objCNN.writeObject(cnnData);
                objCNN.close();
                NNData fcData = new NNData();
                fcData.accuracy = accuracy;
                fcData.nnLayerData = neuralNetwork.saving();
                objFC.writeObject(fcData);
                objFC.close();

            }catch (IOException ignored){}
        }
    }

    /**
     * Loads the AI
     */
    private void load() {
        try {
            System.out.println("Load");
            FileInputStream fisCNN = new FileInputStream("cnnSave.dat");
            BufferedInputStream bisCNN = new BufferedInputStream(fisCNN);
            ObjectInputStream oisCNN = new ObjectInputStream(bisCNN);

            CNNData dataCNN = (CNNData) oisCNN.readObject();
            convolutionalNetwork.load(dataCNN);

            FileInputStream fisFC = new FileInputStream("fcSave.dat");
            BufferedInputStream bisFC = new BufferedInputStream(fisFC);
            ObjectInputStream oisFC = new ObjectInputStream(bisFC);
            NNData dataFC = (NNData) oisFC.readObject();
            neuralNetwork.load(dataFC);

        } catch (IOException | ClassNotFoundException ignored) {}

        propagation();
        neuralNetwork.propagation();
    }

    /**
     * Returns the saved accuracy
     */
    private float getSavedAccuracy(){
        try {
            FileInputStream fisFC = new FileInputStream("fcSave.dat");
            BufferedInputStream bisFC = new BufferedInputStream(fisFC);
            ObjectInputStream oisFC = new ObjectInputStream(bisFC);

            NNData dataFC = (NNData) oisFC.readObject();
            return dataFC.accuracy;

        } catch (IOException | ClassNotFoundException ignored) {}
        return 0;
    }

    /**
     * resets the AI
     */
    private void reset(){
        convolutionalNetwork.reset();
        propagation();
        neuralNetwork.ebenenErstellen();
        neuralNetwork.propagation();
    }

    //endregion

}
