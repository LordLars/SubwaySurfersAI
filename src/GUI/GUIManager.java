package GUI;

import General.Action;
import General.Settings;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;


public class GUIManager extends JFrame implements ActionListener{

    private final int width;
    private final int height;

    private JTextArea layerInfo;
    private JTextArea currentInfo;
    private JButton button;
    public JLabel[] images;

    public GUIManager(){
        images = new JLabel[Settings.getImageNum()];
        width = Settings.pictureWidth;
        height = Settings.pictureHeight;
        buildGUI();
    }

    //region GUI BUILDER

    private void buildGUI(){
        this.setVisible(true);
        this.setSize(800,2000);
        this.setLocation(940,0);
        this.setLayout(new GridLayout(4,2,10,10));
        this.setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        for(int i = 0; i < images.length;i++){
            images[i] = new JLabel();
            this.add(images[i]);
        }
        buildTextArea();
        buildButton();
        this.revalidate();
    }

    private void buildTextArea(){
        layerInfo = new JTextArea();
        layerInfo.setFont(new Font("Dialog", Font.BOLD, 12));
        this.add(layerInfo);

        currentInfo = new JTextArea();
        currentInfo.setFont(new Font("Dialog", Font.BOLD, 12));
        this.add(currentInfo);

    }

    private void buildButton(){
        button = new JButton();
        button.setText("Pause || ");
        button.addActionListener(this);
        button.setActionCommand("Pause");
        this.add(button);
    }

    //endregion

    //region UPDATE IMAGES

    /**
     * Updates the images in the UI
     * @param images new Images
     */
    public void updateImages(BufferedImage[] images){
        for (int i = 0; i < this.images.length; i++){
            images[i] = changeSize(images[i],width,height);
            this.images[i].setIcon(new ImageIcon(images[i]));
        }
    }

    /**
     * Scales the image to a specific size
     * @param image current image
     * @param width new width
     * @param height new height
     */
    public BufferedImage changeSize(BufferedImage image, int width, int height) {
        Image temp = image.getScaledInstance(width, height, Image.SCALE_SMOOTH);
        BufferedImage neuesBild = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);

        Graphics2D g2d = neuesBild.createGraphics();
        g2d.drawImage(temp, 0, 0, null);
        g2d.dispose();

        return neuesBild;
    }
    //endregionS

    //region KI INFO

    /**
     * Calculates the length of each layer in the CNN & NN
     * And Displays it to the GUI
     */
    public void displayLayerInfo(){
        int[] kernelCount = Settings.kernelCount;
        int tempWidth = width;
        int tempHeight = height;

        tempWidth = (int) Math.ceil(tempWidth/(Math.pow(2,Settings.poolingTimes)));
        tempHeight = (int) Math.ceil(tempHeight/Math.pow(2,Settings.poolingTimes));

        String text = "Input: "+ tempWidth+ " x " + tempHeight + " x " + 3 + "\n";

        for(int i = 0; i < kernelCount.length;i++){
            int kernelLength = kernelCount[i];
            tempWidth -= Settings.kernelWidth - 1;
            tempHeight -= Settings.kernelHeight - 1;
            text += "-----------------------------------------------------------\n";
            text += "Convolution Layer: " + tempWidth + " x " + tempHeight + " x " + kernelLength + "       " + (tempWidth * tempHeight * kernelLength)+ "\n";
            if(Settings.withPooling[i]){
                tempWidth = Math.round(tempWidth/2f);
                tempHeight = Math.round(tempHeight/2f);
                text += "Pooling Layer:         " + tempWidth + " x " + tempHeight + " x " + kernelLength + "       " + (tempWidth * tempHeight * kernelLength) + "\n";
            }

        }

        text += "-----------------------------------------------------------\n" +
                "Flatten: " + (tempWidth * tempHeight * kernelCount[kernelCount.length-1]) +"\n" +
                "-----------------------------------------------------------\n";

        for(int i = 0; i < Settings.NNLayerCount;i++){
            text += "DenseLayer " + Settings.NNLayerLength + "\n";
        }
        text += "Output: 5";

        layerInfo.setText(text);

    }

    /**
     * Displays the current accuracy of the AI and displays the output
     * @param pAccuracy current accuracy
     * @param bestAccuracy best saved accuracy
     * @param learned how many pictures it learned
     * @param output output of the NN
     * @param target what action the image shows
     */
    public void updateAccuracy(float pAccuracy, float bestAccuracy, int learned, String output, Action target){
        currentInfo.setText(
                "Current Accuracy: " + pAccuracy + "% out of " + learned + "\n" +
                        "Best Accuracy: " + bestAccuracy + "\n" +
                        output + "\n" +
                        "Target: " + target);
    }

    /**
     * Displays the output of the AI when it plays
     * @param action taken action
     * @param output output of the NN
     */
    public void updateAction(Action action, String output){
        currentInfo.setFont(new Font("Dialog", Font.BOLD, 20));
        currentInfo.setText(action + "\n\n" + output);
    }

    //endregion


    @Override
    public void actionPerformed(ActionEvent e) {
        if(e.getActionCommand().equals("Pause")){
            Settings.pause();
            button.setActionCommand("Play");
            button.setText("Play > ");
        }
        else if(e.getActionCommand().equals("Play")){
            Settings.play();
            button.setActionCommand("Pause");
            button.setText("Pause || ");
        }
    }

}
