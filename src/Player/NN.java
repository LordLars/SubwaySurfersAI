package Player;

import General.Action;
import General.Settings;

public class NN {

    private float[] input;
    private final Action[] output;
    private NNLayer[] layers;
    private final int NNLayerCount;
    private final int NNLayerLength;
    private final int outputLength;


    public NN(){
        NNLayerCount = Settings.NNLayerCount;
        NNLayerLength = Settings.NNLayerLength;

        output = new Action[]{Action.Jump,Action.Roll,Action.Left,Action.Right,Action.None};
        outputLength = output.length;
    }

    //region NN BUILDER

    /**
     * Build new Layers
     */
    public void ebenenErstellen(){
        layers = new NNLayer[NNLayerCount + 2];
        layers[0] = new NNLayer(Settings.getNNInputLength(), NNLayerLength, false);
        for(int i = 1; i < layers.length - 1; i++) {
            layers[i] = new NNLayer(NNLayerLength, NNLayerLength, false);
        }
        layers[layers.length - 1] = new NNLayer(NNLayerLength, outputLength, true);
    }

    //endregion

    //region PROPAGATION
    /**
     * Propagation method
     */
    public void propagation(){
        layers[0].setPreNeurons(input);
        layers[0].propagation();
        for(int i = 1; i < layers.length; i++) {
            layers[i].setPreNeurons(layers[i-1].getNeurons());
            layers[i].propagation();
        }
    }

    //endregion

    //region BACK - PROPAGATION
    /**
     * Backpropagation method
     */
    public void backPropagation(Action target) {
        float[] targets = new float[outputLength];

        for(int i = 0; i < output.length; i++){
            if(target.equals(output[i])) targets[i] = 1;
        }

        layers[layers.length - 1].backPropagation(targets);
        for(int i = layers.length - 2; i >= 0; i--) {
            layers[i].backPropagation(layers[i + 1].getNextChain());
        }
    }

    //endregion

    //region GETTER & SETTER

    /**
     * @return Chain Rule
     */
    public float[] getChain(){
        return layers[0].getNextChain();
    }

    /**
     * @return Output of the NN
     */
    public Action getOutput(){
        float[] output = layers[layers.length - 1].getNeurons();
        int maxIndex = 0;
        for(int i = 1; i < output.length;i++) {
            if(output[i] > output[maxIndex]) maxIndex = i;
        }
        return this.output[maxIndex];
    }

    /**
     * @return Every output of the NN as a string for the GUI
     */
    public String getOutputText(){
        float[] output = layers[layers.length - 1].getNeurons();
        StringBuilder stringBuilder = new StringBuilder();
        for(int i = 0; i < output.length;i++){
            stringBuilder.append(this.output[i]).append(": ").append(output[i]).append("\n");
        }
        return stringBuilder.toString();
    }

    /**
     * Updates the input
     * @param pInput new input
     */
    public void setInput(float[] pInput){
        input = pInput;
    }

    //endregion

    //region SAVE & LOAD
    /**
     * Saves the Values of each layer
     * @return Data
     */
    public NNLayerData[] saving(){
        NNLayerData[] nnLayerData = new NNLayerData[layers.length];
        for(int i = 0; i < layers.length; i++){
            nnLayerData[i] = layers[i].save();
        }
        return nnLayerData;
    }

    /**
     * Loads the saved Data
     * @param data saved Data
     */
    public void load(NNData data){
        for (int i = 0; i < layers.length; i++){
            layers[i].load(data.nnLayerData[i]);
        }
    }

    //endregion


}
