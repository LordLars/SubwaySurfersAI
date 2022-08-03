package Player;

import java.io.Serializable;

public class NNLayerData implements Serializable{
    /**
     * Saved Data of the Neural Network Layer
     */
    public float[][] weights;
    public float[] biases;
    public int preNeuronCount;
    public int neuronCount;

}
