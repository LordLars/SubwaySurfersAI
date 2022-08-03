package Data;

import Pictures.PictureManager;
import com.github.kwhat.jnativehook.keyboard.NativeKeyEvent;
import com.github.kwhat.jnativehook.keyboard.NativeKeyListener;

import javax.imageio.ImageIO;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

import java.nio.file.Paths;
import java.util.Objects;


public class DataManager implements NativeKeyListener {

    private final PictureManager pictureManager;

    public DataManager(PictureManager pictureManager){
        this.pictureManager = pictureManager;
    }

    @Override
    public void nativeKeyPressed(NativeKeyEvent e){
        if(e.getKeyCode() == NativeKeyEvent.VC_RIGHT) savePicture("Right");
        if(e.getKeyCode() == NativeKeyEvent.VC_LEFT) savePicture("Left");
        if(e.getKeyCode() == NativeKeyEvent.VC_UP) savePicture("Jump");
        if(e.getKeyCode() == NativeKeyEvent.VC_DOWN) savePicture("Roll");
        if(e.getKeyCode() == NativeKeyEvent.VC_SPACE) savePicture("None");

    }

    //region SCREENSHOT

    /**
     * Takes a Screenshot and saves it dependent ont the action
     * @param action Action
     */
    public void savePicture(String action){
        String path = "DataSet\\" + action + "\\";
        try {
            int i = Objects.requireNonNull(new File(path).list()).length;
            Files.createDirectories(Paths.get(path));
            System.out.println(action);
            BufferedImage image = pictureManager.getImage();
            ImageIO.write(image, "png" ,new File(path + action + i + ".png"));
            if(action.equals("Left")) flipImage(image,"Right");
            if(action.equals("Right")) flipImage(image,"Left");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Flips an image on the y-axis to have more images
     * @param image Image
     * @param action Action
     */
    private void flipImage(BufferedImage image,String action){
        Graphics2D g = image.createGraphics();
        g.drawImage(image,400,0,-400,250,null);
        try {
            int k = Objects.requireNonNull(new File("DataSet\\" + action + "\\").list()).length;
            ImageIO.write(image, "png" ,new File("DataSet\\"+action+"\\"+ action + k + ".png"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //endregion


}
