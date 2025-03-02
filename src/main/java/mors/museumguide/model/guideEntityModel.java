package mors.museumguide.model;

import mors.museumguide.entity.guideEntity;
import mors.museumguide.state.guideEntityRenderState;
import net.minecraft.client.model.*;
import net.minecraft.client.render.entity.model.EntityModel;
import net.minecraft.client.render.entity.model.EntityModelPartNames;
import net.minecraft.util.math.MathHelper;

public class guideEntityModel extends EntityModel<guideEntityRenderState> {
    private final ModelPart body;

    public guideEntityModel(ModelPart root) {
        super(root);
        body = root.getChild(EntityModelPartNames.BODY);
    }

    public static TexturedModelData getTexturedModelData() {
        ModelData modelData = new ModelData();
        ModelPartData root = modelData.getRoot();
        root.addChild(
                EntityModelPartNames.BODY
                , ModelPartBuilder.create().uv(-16, -8).cuboid(-5.0F, -21.0F, -5.0F, 10.0F, 21.0F, 10.0F, new Dilation(0.0F)), ModelTransform.pivot(0.0F, 24.0F, 0.0F));
        return TexturedModelData.of(modelData, 16, 16);
    }
    @Override
    public void setAngles(guideEntityRenderState state) {
        super.setAngles(state);

    }
}
