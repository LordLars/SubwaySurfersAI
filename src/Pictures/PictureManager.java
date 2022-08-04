package Pictures;

import Main.Action;
import Main.AIState;
import Main.Settings;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class PictureManager{

    private final AIState state;

    private final int width;
    private final int height;
    private final Action[] actions;
    private Action shownAction;


    public PictureManager() {
        this.state = Settings.aiState;
        width = Settings.pictureWidth;
        height = Settings.pictureHeight;

        actions = new Action[]{Action.Jump,Action.Roll,Action.Left,Action.Right,Action.None};
    }

    //region GETTER & SETTER

    /**
     * Takes a screenshot with the given size in the Settings
     * @return Image
     */
    public BufferedImage getImage(){
        if(state.equals(AIState.Data) || state.equals(AIState.Play)){
            try {
                Robot robot = new Robot();
                Rectangle rect = new Rectangle(700,350,width,height);
                return robot.createScreenCapture(rect);
            } catch (AWTException e) {
                e.printStackTrace();
            }
        }else{
            return getRandomTrainingImage();
        }
        return null;
    }

    /**
     * Reads a random image from the dataset
     * @return Random Image
     */
    private BufferedImage getRandomTrainingImage(){
        int aktion = (int)(Math.random() * actions.length);
        shownAction = actions[aktion];
        String path = "DataSet\\" + shownAction + "\\";
        int num = (int)(Math.random() * (new File(path).list().length-1));
        try {
            ImageIO.setUseCache(false);
            return ImageIO.read(new File(path + shownAction + num + ".png"));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Forms a 3 dimensional array for the RGB values of the image
     * @return RGB values
     */
    public float[][][] getPixel(){
        BufferedImage image = getImage();
        BufferedImage image1 = new BufferedImage(width, height, BufferedImage.TYPE_BYTE_GRAY);
        Graphics g = image1.getGraphics();
        g.drawImage(image, 0, 0, null);
        g.dispose();
        float[][][] pixel = new float[3][width][height];
        for(int y = 0; y < height;y++){
            for(int x = 0; x < width;x++){
                Color farbe = new Color(image.getRGB(x,y), true);
                pixel[0][x][y] = farbe.getRed()/255f;
                pixel[1][x][y] = farbe.getGreen()/255f;
                pixel[2][x][y] = farbe.getBlue()/255f;
            }
        }
        return pixel;
    }

    /**
     * @return shown Action of the saved image
     */
    public Action getActionShown(){
        return shownAction;
    }

    //endregion
}
