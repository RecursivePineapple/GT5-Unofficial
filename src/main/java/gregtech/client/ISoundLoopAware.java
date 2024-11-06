package gregtech.client;

public interface ISoundLoopAware {
    
    public void modifySoundLoop(GTSoundLoop loop);

    public default void onSoundLoopTicked(GTSoundLoop loop) {

    }

}
