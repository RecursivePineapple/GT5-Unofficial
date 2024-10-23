package gregtech.api.interfaces.metatileentity;

import gregtech.common.render.GTRendererBlock;

/**
 * Used to render any extra special effects for in-world MTEs.
 */
public interface IRendersDecorations {

    /**
     * Called from {@link GTRendererBlock#renderWorldBlock} (an ISBRH).
     */
    public void renderDecorations();

}
