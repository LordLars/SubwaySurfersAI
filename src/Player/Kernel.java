package Player;

public class Kernel {

    private float[][][] kernel;
    private final int kernelWidth;
    private final int kernelHeight;
    private final int channels;

    public Kernel(int pKernelWidth, int pKernelHeight, int pChannels, boolean random){
        kernelWidth = pKernelWidth;
        kernelHeight = pKernelHeight;
        channels = pChannels;
        buildKernel(random);
    }

    //region KERNEL BUILDER

    /**
     * Builds a new Kernel
     * @param random if values should be random or fix
     */
    private void buildKernel(boolean random){
        kernel = new float[channels][kernelWidth][kernelHeight];
        for(int i = 0; i < channels; i++){
            for(int y = 0; y < kernelHeight;y++){
                for(int x = 0; x < kernelWidth;x++){
                    if(random) kernel[i][x][y] = (float) (Math.random()*1-.5f);
                    else kernel[i][x][y] = .1f;
                }
            }
        }
    }
    //endregion

    //region GETTER & SETTER

    /**
     * Returns a Kernel in a channel
     * @param index index of the channel
     * @return Kernel
     */
    public float[][] getKernel(int index){
        return kernel[index];
    }

    /**
     * Updates the Kernel
     * @param newKernel new Kernel
     * @param index the index of the channel
     */
    public void setKernel(float[][] newKernel,int index){
        kernel[index] = newKernel;
    }



    //endregion

    //region SAVE & LOAD
    /**
     * Saves the Kernel
     * @return Kernel Data
     */
    public KernelData save(){
        KernelData kernelData = new KernelData();
        kernelData.kernel = kernel;
        return kernelData;
    }

    /**
     * Loads the Kernel
     * @param data saved values
     */
    public void load(KernelData data){
        kernel = data.kernel;
    }

    //endregion

}
