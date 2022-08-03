package Player;

public class PoolingLayer {

    private final int kernelSize;
    private final int stride;

    //used for Backpropagation
    private FeatureMap[] pooledFeatureMaps;

    public PoolingLayer(int pKernelSize, int pStride){
        kernelSize = pKernelSize;
        stride = pStride;
    }

    //region POOLING

    /**
     * Minimizes the Feature Maps and saves the deleted indices
     * @param featureMap Feature Map
     * @return smaller Feature Map
     */
    public float[][] maxPooling(float[][] featureMap, int index){
        int newWidth = Math.round((featureMap.length- kernelSize)/(stride*1f)) + 1;
        int newHeight = Math.round((featureMap[0].length- kernelSize)/(stride*1f)) + 1;
        float[][] smallerFeatureMap = new float[newWidth][newHeight];
        float[][] pooled = new float[featureMap.length][featureMap[0].length];
        for(int y = 0; y < newHeight;y++){
            for(int x = 0; x < newWidth;x++){
                float maxValue = featureMap[x*stride][y*stride];
                int indexX = x*stride;
                int indexY = y*stride;
                for(int ky = 0; ky < kernelSize; ky++){
                    for(int kx = 0; kx < kernelSize; kx++){
                        if(y*stride+ky < featureMap[0].length && x*stride+kx < featureMap.length){
                            if(featureMap[x*stride+kx][y*stride+ky] > maxValue){
                                maxValue = featureMap[x*stride+kx][y*stride+ky];
                                indexX = x*stride+kx;
                                indexY = y*stride+ky;
                            }
                        }
                    }
                }
                pooled[indexX][indexY] = 1;
                smallerFeatureMap[x][y] = maxValue;
            }
        }
        pooledFeatureMaps[index] = new FeatureMap(pooled);
        return smallerFeatureMap;
    }

    /**
     * Minimizes all Feature Maps
     * @param pFeatureMaps all Feature Maps
     * @return all smaller Feature Maps
     */
    public FeatureMap[] featureMapPooling(FeatureMap[] pFeatureMaps){
        pooledFeatureMaps = new FeatureMap[pFeatureMaps.length];
        for(int i = 0; i < pFeatureMaps.length;i++){
            float[][] pooled = maxPooling(pFeatureMaps[i].getFeatureMap(), i);
            pFeatureMaps[i] = new FeatureMap(pooled);
        }
        return pFeatureMaps;
    }

    //endregion

    //region GETTER & SETTER

    /**
     * @return Saved indices of the pooling method
     */
    public FeatureMap[] getPooledFeatureMaps(){
        return pooledFeatureMaps;
    }

    //endregion

}
