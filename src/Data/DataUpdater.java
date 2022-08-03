package Data;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class DataUpdater {

    /**
     * If pictures are deleted in the dataset the numbers are not correct,
     * so they will be reorganized
     */
    public static void updateAllFileTags(){
        updateFileTags("None");
        updateFileTags("Right");
        updateFileTags("Left");
        updateFileTags("Jump");
        updateFileTags("Roll");
    }

    /**
     * Updates a specific folder
     * @param type Specific folder
     */
    private static void updateFileTags(String type){
        String path = "DataSet\\" + type + "\\";
        try {
            Files.createDirectories(Paths.get(path));
        } catch (IOException e) {
            e.printStackTrace();
        }

        File file = new File(path);
        File[] files = file.listFiles();

        for(int i = 0; i < files.length;i++){
            File rename = new File(path + "ERROR " + i + ".png");
            files[i].renameTo(rename);
        }
        files = file.listFiles();
        for(int i = 0; i < files.length;i++){
            File rename = new File(path + type+i+".png");
            files[i].renameTo(rename);

            //if you want to reset the right Pictures - they will be copied from the Left and flipped
            /*
            if(type.equals("Left")){
                try {
                    flipImage(ImageIO.read(new File(path + type + i + ".png")).getSubimage(0,0,400,250));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
             */
        }

    }

    /**
     * Flips an image on the y-axis
     * @param image Image
     */
    private static void flipImage(BufferedImage image){
        try {
            Graphics2D g = image.createGraphics();
            g.drawImage(image,400,0,-400,250,null);
            Files.createDirectories(Paths.get("DataSet\\Right\\"));
            int k = new File("DataSet\\Right\\").list().length;
            ImageIO.write(image, "png" ,new File("DataSet\\Right\\Right" + k + ".png"));

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
