package Player;

public class FeatureMap {

    private final float[][] featureMap;

    public FeatureMap(float[][] pFeatureMap){
        featureMap = pFeatureMap;
    }

    //region ADDITION

    /**
     * Adds up another Feature Map to this Feature Map
     * @param pFeatureMap another Feature Map
     */
    public void add(FeatureMap pFeatureMap){
        float[][] fM = pFeatureMap.getFeatureMap();
        if(fM.length == this.featureMap.length){
            for(int y = 0; y < fM[0].length;y++){
                for(int x = 0; x < fM.length;x++){
                    this.featureMap[x][y] += fM[x][y];
                }
            }
        }
    }
    //endregion

    //region MULTIPLICATION
    /**
     * Multiplies another Feature Map to this Feature Map
     * @param pFeatureMap another Feature Map
     */
    public void multiply(FeatureMap pFeatureMap){
        float[][] fM = pFeatureMap.getFeatureMap();
        if(fM.length == this.featureMap.length && fM[0].length == this.featureMap[0].length){
            for(int y = 0; y < fM[0].length;y++){
                for(int x = 0; x < fM.length;x++){
                    this.featureMap[x][y] *= fM[x][y];
                }
            }
        }
    }
    //endregion

    //region GETTER & SETTER
    /**
     * @return Feature Map
     */
    public float[][] getFeatureMap(){
        return featureMap;
    }

    //endregion
}
