package Player;

import java.io.Serializable;

public class CNNLayerData implements Serializable{
    /**
     * Saved data for kernels and biases
     */
    public KernelData[] kernels;
    public KernelData[] biases;
    public int preFeatureMapCount;
}
