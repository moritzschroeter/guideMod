package mors.museumguide.entity.renderer;

import mors.museumguide.MuseumGuide;
import mors.museumguide.entity.guideEntity;
import mors.museumguide.model.guideEntityModel;
import mors.museumguide.model.guideEntityModelLayers;
import mors.museumguide.state.guideEntityRenderState;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.MobEntityRenderer;
import net.minecraft.util.Identifier;

public class guideEntityRenderer extends MobEntityRenderer<guideEntity, guideEntityRenderState, guideEntityModel> {
    private static final Identifier TEXTURE = Identifier.of(MuseumGuide.MOD_ID, "textures/entity/guide.png");

    public guideEntityRenderer(EntityRendererFactory.Context context)   {
        super(context, new guideEntityModel(context.getPart(guideEntityModelLayers.GUIDE)), 0.375f);
    }

    @Override
    public guideEntityRenderState createRenderState()   {
        return new guideEntityRenderState();
    }
    @Override
    public void updateRenderState(guideEntity entity, guideEntityRenderState state, float tickProgress) {
        super.updateRenderState(entity, state, tickProgress);
    }

    @Override
    public Identifier getTexture(guideEntityRenderState state) {
        return TEXTURE;
    }
}

