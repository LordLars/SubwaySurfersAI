package Player;

import Main.Settings;

public class CNNLayer {

    private final PoolingLayer poolingLayer;

    private int preFeatureMapCount;
    private int featureMapCount;
    private FeatureMap[] preFeatureMaps;
    private FeatureMap[] chainRule;
    private FeatureMap[] featureMaps;
    private FeatureMap[] relUFeatureMaps;

    private Kernel[] kernels;
    private Kernel[] biases;

    private boolean start;
    private boolean usedPooling;


    public CNNLayer(int kernelAnzahl, int pVorFeatureMapAnzahl) {
        featureMapCount = kernelAnzahl;
        preFeatureMapCount = pVorFeatureMapAnzahl;
        featureMaps = new FeatureMap[featureMapCount];
        poolingLayer = new PoolingLayer(2, 2);
        start = true;
        buildKernels();
    }

    //region CNN LAYER BUILDER

    /**
     * Builds new Kernels with a specific size
     */
    private void buildKernels() {
        kernels = new Kernel[featureMapCount];
        for (int i = 0; i < featureMapCount; i++) {
            kernels[i] = new Kernel(Settings.kernelWidth,Settings.kernelHeight, preFeatureMapCount, true);
        }
    }


    /**
     * Builds new Biases with a specific size
     * @param width width of the Bias
     * @param height height of the Bias
     */
    private void buildBias(int width, int height) {
        start = false;
        biases = new Kernel[kernels.length];
        for (int i = 0; i < kernels.length; i++) {
            biases[i] = new Kernel(width,height, 1, false);
        }
    }

    //endregion

    //region CONVOLUTION

    /**
     * Creates new Feature Maps with the convolution method
     */
    public void convolution() {
        int newWidth = preFeatureMaps[0].getFeatureMap().length - (Settings.kernelWidth - 1);
        int newHeight = preFeatureMaps[0].getFeatureMap()[0].length - (Settings.kernelWidth - 1);
        if (start) buildBias(newWidth, newHeight);
        for (int k = 0; k < kernels.length; k++) {
            FeatureMap newFeatureMap = new FeatureMap(new float[newWidth][newHeight]);
            for (int i = 0; i < preFeatureMapCount; i++) {
                float[][] kernel = kernels[k].getKernel(i);
                float[][] bias = biases[k].getKernel(0);
                float[][] featureMap = preFeatureMaps[i].getFeatureMap();
                newFeatureMap.add(new FeatureMap(validConvolution(featureMap, kernel, bias)));
            }
            featureMaps[k] = newFeatureMap;
        }
        activation();
    }

    /**
     * Convolution method but the kernel doesn't overlap edge of the feature Map
     * @param featureMap Feature Map
     * @param kernel Kernel
     * @param bias Bias
     * @return new Feature Map
     */
    private float[][] validConvolution(float[][] featureMap, float[][] kernel, float[][] bias) {
        int newWidth = featureMap.length - kernel.length + 1;
        int newHeight = featureMap[0].length - kernel[0].length + 1;
        float[][] newFeatureMap = new float[newWidth][newHeight];

        for (int y = 0; y < newHeight; y++) {
            for (int x = 0; x < newWidth; x++) {
                float sum = 0;
                for (int ky = 0; ky < kernel[0].length; ky++) {
                    for (int kx = 0; kx < kernel.length; kx++) {
                        sum += kernel[kx][ky] * featureMap[x + kx][y + ky];
                    }
                }
                if (bias != null) sum += bias[x][y];
                newFeatureMap[x][y] = sum;
            }
        }
        return newFeatureMap;
    }

    /**
     * Convolution method but the kernel does overlap edge of the feature Map
     * @param featureMap Feature Map
     * @param kernel Kernel
     * @return new Feature Map
     */
    private float[][] fullConvolution(float[][] featureMap, float[][] kernel) {
        int newWidth = featureMap.length + kernel.length - 1;
        int newHeight = featureMap[0].length + kernel[0].length - 1;
        float[][] newFeatureMap = new float[newWidth][newHeight];
        for (int y = 0; y < newFeatureMap[0].length; y++) {
            for (int x = 0; x < newFeatureMap.length; x++) {
                float sum = 0;
                for (int ky = 0; ky < kernel.length; ky++) {
                    for (int kx = 0; kx < kernel.length; kx++) {
                        int xIndex = x + kx - (kernel.length - 1);
                        int yIndex = y + ky - (kernel.length - 1);
                        if (xIndex >= 0 && xIndex < featureMap.length && yIndex >= 0 && yIndex < featureMap[0].length)
                            sum += featureMap[xIndex][yIndex] * kernel[kx][ky];
                    }
                }
                newFeatureMap[x][y] = sum;
            }
        }
        return newFeatureMap;
    }
    /**
     * minimizes the Feature Maps with the pooling method
     */
    public void maxPooling() {
        featureMaps = poolingLayer.featureMapPooling(featureMaps);
        usedPooling = true;
    }

    //endregion

    //region BACK - PROPAGATION


    /**
     * Backpropagation method
     * @param featureMapChain chain rule from the NN
     */
    public void backPropagation(FeatureMap[] featureMapChain) {
        FeatureMap[] pooled = poolingBackpropagation(featureMapChain);
        updateKernels(pooled);
        updateBias(pooled);
        aktualisiereChainRule(pooled);
    }

    /**
     * Backpropagation method in Pooling Layer
     * @param featureMapChain values of the chain rule
     * @return new Feature Map
     */
    private FeatureMap[] poolingBackpropagation(FeatureMap[] featureMapChain){
        FeatureMap[] pooled = poolingLayer.getPooledFeatureMaps();
        //Turns every value of the Feature Map to 0 if it wasn't used after pooling
        if (usedPooling) {
            for (int i = 0; i < kernels.length; i++) {
                pooled[i].multiply(relUFeatureMaps[i]);
                float[][] pooledArray = pooled[i].getFeatureMap();
                float[][] chain = featureMapChain[i].getFeatureMap();
                for (int y = 0; y < chain[0].length; y++) {
                    for (int x = 0; x < chain.length; x++) {
                        for (int j = 0; j < 2; j++) {
                            for (int k = 0; k < 2; k++) {
                                int xIndex = x * 2 + k;
                                int yIndex = y * 2 + j;
                                if (xIndex < pooledArray.length && yIndex < pooledArray[0].length) pooledArray[xIndex][yIndex] *= chain[x][y];
                            }
                        }
                    }
                }
                pooled[i] = new FeatureMap(pooledArray);
            }
        } else {
            pooled = featureMapChain;
        }
        return pooled;
    }

    /**
     * Updates the Kernels with the Backpropagation method
     * @param chain values of the chain Rule
     */
    private void updateKernels(FeatureMap[] chain) {
        for (int k = 0; k < kernels.length; k++) {
            for (int i = 0; i < preFeatureMapCount; i++) {
                float[][] featureMap = preFeatureMaps[i].getFeatureMap();
                float[][] chainArray = chain[k].getFeatureMap();
                float[][] kernelLoss = validConvolution(featureMap, chainArray, null);
                float[][] kernel = kernels[k].getKernel(i);

                for (int y = 0; y < kernel.length; y++) {
                    for (int x = 0; x < kernel.length; x++) {
                        kernel[x][y] = kernel[x][y] - kernelLoss[x][y] * Settings.cLernRate;
                    }
                }
                kernels[k].setKernel(kernel, i);
            }
        }
    }

    /**
     * Updates the Biases with the Backpropagation method
     * @param chain values of the chain Rule
     */
    private void updateBias(FeatureMap[] chain) {
        for (int i = 0; i < chain.length; i++) {
            float[][] bias = biases[i].getKernel(0);
            float[][] chainArray = chain[i].getFeatureMap();
            for (int y = 0; y < bias[0].length; y++) {
                for (int x = 0; x < bias.length; x++) {
                    bias[x][y] = bias[x][y] - chainArray[x][y] * Settings.cLernRate;
                }
            }
            biases[i].setKernel(bias, 0);
        }
    }

    /**
     * Updates the Chain Rule
     * @param chain values of the chain Rule
     */
    private void aktualisiereChainRule(FeatureMap[] chain) {
        chainRule = new FeatureMap[preFeatureMapCount];
        for (int i = 0; i < preFeatureMapCount; i++) {
            int width = preFeatureMaps[0].getFeatureMap().length;
            int height = preFeatureMaps[0].getFeatureMap()[0].length;
            FeatureMap newChainRule = new FeatureMap(new float[width][height]);
            for (int k = 0; k < kernels.length; k++) {
                float[][] chainArray = chain[k].getFeatureMap();
                float[][] kernel = rotateKernel(kernels[k].getKernel(i));
                float[][] inputLoss = fullConvolution(chainArray, kernel);
                newChainRule.add(new FeatureMap(inputLoss));
            }
            chainRule[i] = newChainRule;
        }
    }

    /**
     * Rotates the kernel by 180°
     * @param kernel Kernel
     * @return rotated Kernel
     */
    private float[][] rotateKernel(float[][] kernel) {
        int kL = kernel.length;
        float[][] newKernel = new float[kL][kL];
        for (int y = 0; y < kernel.length; y++) {
            for (int x = 0; x < kernel[y].length; x++) {
                newKernel[kL - x - 1][kL - y - 1] = kernel[x][y];
            }
        }
        return newKernel;
    }
    
    //endregion

    //region ACTIVATION

    /**
     * The values of the Feature Maps are put into the activation function ReLU
     * Furthermore the values are saved for the backpropagation method
     * and put into the derivative of the activation function
     */
    private void activation() {
        relUFeatureMaps = new FeatureMap[featureMaps.length];
        for (int i = 0; i < featureMaps.length; i++) {
            float[][] featureMap = featureMaps[i].getFeatureMap();
            float[][] relu = new float[featureMap.length][featureMap[0].length];
            for (int y = 0; y < featureMap[0].length; y++) {
                for (int x = 0; x < featureMap.length; x++) {
                    featureMap[x][y] = reLU(featureMap[x][y]);
                    relu[x][y] = reLUDerivative(featureMap[x][y]);
                }
            }
            featureMaps[i] = new FeatureMap(featureMap);
            relUFeatureMaps[i] = new FeatureMap(relu);
        }
    }

    /**
     * Activation function: ReLU
     * @param activation value to be calculated
     * @return result of the ReLU function
     */
    private float reLU(float activation) {
        return Math.max(0,activation*.1f);
    }

    /**
     * Activation function: Derivative of ReLU
     * @param activation value to be calculated
     * @return result of the derivative of the ReLU function
     */
    private float reLUDerivative(float activation) {
        float derivative = .1f;
        if (activation < 0) derivative = 0;
        return derivative;
    }

    //endregion

    //region GETTER & SETTER

    /**
     * @return Previous Feature Map
     */
    public FeatureMap[] getPreFeatureMap(){
        return preFeatureMaps;
    }

    /**
     * @return Feature Maps
     */
    public FeatureMap[] getNeueFeatureMaps() {
        return featureMaps;
    }

    /**
     * Update previous Feature Maps
     * @param pFeatureMaps new Feature Map
     */
    public void setPreFeatureMaps(FeatureMap[] pFeatureMaps) {
        preFeatureMaps = pFeatureMaps;
        preFeatureMapCount = preFeatureMaps.length;
    }

    /**
     * @return Values of the Chain Rule
     */
    public FeatureMap[] getChainRule() {
        return chainRule;
    }



    //endregion

    //region SAVE & LOAD
    /**
     * Saves the Data of the layer
     * @return Data of Kernels and Biases
     */
    public CNNLayerData save(){
        CNNLayerData data = new CNNLayerData();
        data.biases = new KernelData[biases.length];
        data.kernels = new KernelData[kernels.length];
        data.preFeatureMapCount = preFeatureMapCount;
        for(int i = 0; i < kernels.length;i++){
            data.biases[i] = biases[i].save();
            data.kernels[i] = kernels[i].save();
        }
        return data;
    }

    /**
     * Loads the saved data of the layer
     * @param data saved values
     */
    public void load(CNNLayerData data){
        featureMapCount = data.kernels.length;
        buildBias(1,1);
        buildKernels();
        for(int i = 0; i < featureMapCount; i++){
            kernels[i].load(data.kernels[i]);
            biases[i].load(data.biases[i]);
        }
    }

    //endregion
}

