package gregtech.api.util;

import java.util.ArrayList;

import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.TickEvent;
import cpw.mods.fml.common.gameevent.TickEvent.Phase;
import cpw.mods.fml.common.gameevent.TickEvent.Type;
import cpw.mods.fml.relauncher.Side;
import gregtech.GTMod;

public class TickDeferral {

    private static ArrayList<Runnable> nextTick = new ArrayList<>(), thisTick = new ArrayList<>();

    private static final TickDeferral INSTANCE = new TickDeferral();

    private TickDeferral() {
        FMLCommonHandler.instance()
            .bus()
            .register(this);
    }

    public static synchronized void schedule(Runnable runnable) {
        nextTick.add(runnable);
    }

    @SubscribeEvent
    public void tick(TickEvent.ServerTickEvent event) {
        if (event.side == Side.SERVER && event.type == Type.SERVER && event.phase == Phase.END) {
            for (Runnable action : thisTick) {
                try {
                    action.run();
                } catch (Throwable t) {
                    GTMod.GT_FML_LOGGER.error("Could not run scheduled Runnable " + action, t);
                }
            }

            thisTick.clear();

            var temp = thisTick;
            thisTick = nextTick;
            nextTick = temp;
        }
    }
}
