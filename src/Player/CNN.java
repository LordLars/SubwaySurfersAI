package Player;

import Main.Settings;

import java.awt.image.BufferedImage;

public class CNN {

    private int convolutionLayerCount;
    private CNNLayer[] convolutionLayers;
    private final int[] kernelCount;

    private float[][] pixelR, pixelG, pixelB;
    private int preWidth,preHeight;


    public CNN(){
        kernelCount = Settings.kernelCount;
        convolutionLayerCount = kernelCount.length;
    }

    //region CNN BUILDER
    /**
     * Creates the CNN Layers
     */
    public void buildConvolutionLayers() {
        convolutionLayers = new CNNLayer[convolutionLayerCount];
        convolutionLayers[0] = new CNNLayer(kernelCount[0],3);
        for(int i = 1; i < convolutionLayerCount; i++) convolutionLayers[i] = new CNNLayer(kernelCount[i], kernelCount[i-1]);
    }
    //endregion

    //region PROPAGATION

    /**
     * Propagates through the CNN Layers
     */
    public void propagation(){
        FeatureMap[] featureMaps = new FeatureMap[]{new FeatureMap(pixelR),new FeatureMap(pixelG),new FeatureMap(pixelB)};

        PoolingLayer p = new PoolingLayer(2,2);
        for (int i = 0; i < Settings.poolingTimes; i++) featureMaps = p.featureMapPooling(featureMaps);
        propagate(featureMaps,0);
        for(int i = 1; i < convolutionLayerCount; i++) propagate(convolutionLayers[i-1].getNeueFeatureMaps(),i);
    }

    /**
     * Starts propagation in a specific layer
     * @param pFeatureMaps Feature Maps from the previous layer
     * @param index layer index
     */
    private void propagate(FeatureMap[] pFeatureMaps, int index){
        convolutionLayers[index].setPreFeatureMaps(pFeatureMaps);
        convolutionLayers[index].convolution();
        if(Settings.withPooling[index])convolutionLayers[index].maxPooling();
    }


    /**
     * Transforms the Feature Maps of the last layer into an Array
     * @return array
     */
    public float[] flatten(){
        int layer = convolutionLayers.length-1;
        FeatureMap[] featureMaps = convolutionLayers[layer].getNeueFeatureMaps();
        int featureMapWidth = featureMaps[0].getFeatureMap().length;
        int featureMapHeight = featureMaps[0].getFeatureMap()[0].length;

        preWidth = featureMapWidth;
        preHeight = featureMapHeight;
        float[] pixel = new float[featureMaps.length * featureMapWidth * featureMapHeight];
        for(int i = 0; i < featureMaps.length;i++){

            for(int y = 0; y< featureMapHeight; y++){
                for(int x = 0; x< featureMapWidth; x++){
                    int index = i*featureMapWidth*featureMapHeight + y * featureMapHeight + x;
                    pixel[index] = featureMaps[i].getFeatureMap()[x][y];
                }
            }
        }
        return pixel;
    }
    //endregion

    //region BACK - PROPAGATION

    /**
     * Backpropagation of CNN
     * @param preChain values of the chain Rule from the NN
     */
    public void backPropagation(float[] preChain){
        FeatureMap[] featureMapChain = transformToFeatureMaps(preChain);
        convolutionLayers[convolutionLayers.length - 1].backPropagation(featureMapChain);
        for(int i = convolutionLayers.length - 2; i >= 0; i--) {
            convolutionLayers[i].backPropagation(convolutionLayers[i+1].getChainRule());
        }
    }

    /**
     * Transforms an array back to Feature Maps
     * @param preChain array
     * @return Feature Maps
     */
    private FeatureMap[] transformToFeatureMaps(float[] preChain){
        FeatureMap[] featureMapChain = new FeatureMap[convolutionLayers[convolutionLayers.length-1].getNeueFeatureMaps().length];
        for(int i = 0; i < featureMapChain.length;i++){
            float[][] featureMap = new float[preWidth][preHeight];
            for(int y = 0; y < preHeight;y++){
                for(int x = 0; x < preWidth;x++){
                    featureMap[x][y] = preChain[i*preWidth*preHeight+x+y*preHeight];
                }
            }
            featureMapChain[i] = new FeatureMap(featureMap);
        }
        return featureMapChain;
    }

    //endregion

    //region GUI

    /**
     * Returns the images from the different CNN layers
     * @return images
     */
    public BufferedImage[] ausgabe(){
        BufferedImage[] images = new BufferedImage[Settings.getImageNum()];
        images[0] = createImage(pixelR, pixelG, pixelB);

        float[][] temp = convolutionLayers[0].getPreFeatureMap()[1].getFeatureMap();
        images[1] = createImage(temp, temp, temp);
        temp = convolutionLayers[0].getPreFeatureMap()[2].getFeatureMap();
        images[2] = createImage(temp, temp, temp);

        for(int i = Settings.getImageNum()- convolutionLayerCount; i < Settings.getImageNum(); i++){
            temp = convolutionLayers[i-(Settings.getImageNum()- convolutionLayerCount)].getPreFeatureMap()[0].getFeatureMap();
            images[i] = createImage(temp,temp,temp);
        }

        return images;
    }

    /**
     * Creates a Feature Maps by taking the pixel Values of each Color
     * @param imagePixelR Red pixel
     * @param imagePixelG Green pixel
     * @param imagePixelB Blue pixel
     * @return image
     */
    public BufferedImage createImage(float[][] imagePixelR,float[][] imagePixelG,float[][] imagePixelB){
        BufferedImage image = new BufferedImage(imagePixelR.length,imagePixelR[0].length,BufferedImage.TYPE_INT_RGB);

        for (int y = 0; y < imagePixelR[0].length;y++){
            for (int x = 0; x < imagePixelR.length;x++){
                int r = (int)((imagePixelR[x][y]) * 255f);
                int g = (int)((imagePixelG[x][y]) * 255f);
                int b = (int)((imagePixelB[x][y]) * 255f);
                int col = (r << 16) | (g << 8) | b;
                image.setRGB(x,y,col);
            }
        }
        return image;
    }

    //endregion

    //region GETTER & SETTER

    /**
     * Sets the pixel values
     * @param pPixel all color pixel values
     */
    public void setPixel(float[][][] pPixel){
        pixelR = pPixel[0];
        pixelG = pPixel[1];
        pixelB = pPixel[2];
    }

    //endregion

    //region SAVE & LOAD

    /**
     * Resets the CNN
     */
    public void reset(){
        buildConvolutionLayers();
        convolutionLayerCount = convolutionLayers.length;
    }

    /**
     * Returns the Data of the CNN
     * @return data
     */
    public CNNData save(){
        CNNData daten = new CNNData();
        daten.convolutionData = new CNNLayerData[convolutionLayerCount];
        for(int i = 0; i < convolutionLayerCount; i++){
            daten.convolutionData[i] = convolutionLayers[i].save();
        }
        return daten;
    }

    /**
     * Loads the saved data
     * @param daten saved data
     */
    public void load(CNNData daten) {
        convolutionLayerCount = daten.convolutionData.length;
        convolutionLayers = new CNNLayer[convolutionLayerCount];
        for(int i = 0; i < convolutionLayerCount; i++){
            CNNLayerData convolutionDaten = daten.convolutionData[i];
            convolutionLayers[i] = new CNNLayer(convolutionDaten.kernels.length,convolutionDaten.preFeatureMapCount);
            convolutionLayers[i].load(daten.convolutionData[i]);
        }
    }

    //endregion
}
