package com.plusls.MasaGadget.impl.feature.entityTrace;

//#if MC < 12110
import com.google.common.collect.Queues;
import com.plusls.MasaGadget.game.Configs;
import com.plusls.MasaGadget.util.MiscUtil;
import com.plusls.MasaGadget.util.RenderUtil;
import com.plusls.MasaGadget.util.SyncUtil;
import fi.dy.masa.malilib.util.Color4f;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import top.hendrixshen.magiclib.MagicLib;
import top.hendrixshen.magiclib.api.event.minecraft.render.RenderEntityListener;
import top.hendrixshen.magiclib.api.event.minecraft.render.RenderLevelListener;
import top.hendrixshen.magiclib.api.render.context.LevelRenderContext;
import top.hendrixshen.magiclib.api.render.context.RenderContext;
import top.hendrixshen.magiclib.impl.render.context.EntityRenderContext;
import top.hendrixshen.magiclib.impl.render.context.RenderGlobal;

import java.util.Queue;
//#endif

import lombok.Getter;
import org.jetbrains.annotations.ApiStatus;

//#if MC < 12110
public class EntityTraceRenderer implements RenderEntityListener, RenderLevelListener {
//#else
//$$ import lombok.Getter;
//$$ import org.jetbrains.annotations.ApiStatus;
//$$
//$$ // TODO: Re-implement when MagicLib's RenderEntityListener API is stable for 1.21.10
//$$ public class EntityTraceRenderer {
//#endif
    @Getter
    private static final EntityTraceRenderer instance = new EntityTraceRenderer();
    //#if MC < 12110
    private final Queue<Entity> queue = Queues.newConcurrentLinkedQueue();
    //#endif

    @ApiStatus.Internal
    public void init() {
        //#if MC < 12110
        MagicLib.getInstance().getEventManager().register(RenderEntityListener.class, this);
        MagicLib.getInstance().getEventManager().register(RenderLevelListener.class, this);
        //#endif
    }

    //#if MC < 12110
    @Override
    public void preRenderEntity(Entity entity, EntityRenderContext renderContext) {
        // NO-OP
    }

    @Override
    public void postRenderEntity(Entity entity, EntityRenderContext renderContext) {
        if (entity instanceof Villager &&
                Configs.renderVillageHomeTracer.getBooleanValue() ||
                Configs.renderVillageJobSiteTracer.getBooleanValue()) {
            this.queue.add(entity);
        }
    }

    @Override
    public void preRenderLevel(ClientLevel level, LevelRenderContext renderContext) {
        // NO-OP
    }

    @Override
    public void postRenderLevel(ClientLevel level, LevelRenderContext renderContext) {
        float partialTick = top.hendrixshen.magiclib.util.minecraft.render.RenderUtil.getPartialTick();

        for (Entity entity : this.queue) {
            if (entity instanceof Villager) {
                Villager villager = MiscUtil.cast(SyncUtil.syncEntityDataFromIntegratedServer(entity));

                if (Configs.renderVillageHomeTracer.getBooleanValue()) {
                    villager.getBrain().getMemory(MemoryModuleType.HOME).ifPresent(globalPos -> {
                        Vec3 eyeVec3 = entity.getEyePosition(partialTick);
                        Vec3 bedVec3 = new Vec3(globalPos.pos().getX() + 0.5, globalPos.pos().getY() + 0.5, globalPos.pos().getZ() + 0.5);
                        RenderGlobal.disableDepthTest();
                        RenderUtil.drawConnectLine(eyeVec3, bedVec3, 0.05,
                                new Color4f(1, 1, 1),
                                Color4f.fromColor(Configs.renderVillageHomeTracerColor.getColor(), 1.0F),
                                Configs.renderVillageHomeTracerColor.getColor());
                        RenderGlobal.enableDepthTest();
                    });
                }

                if (Configs.renderVillageJobSiteTracer.getBooleanValue()) {
                    villager.getBrain().getMemory(MemoryModuleType.JOB_SITE).ifPresent(globalPos -> {
                        Vec3 eyeVec3 = entity.getEyePosition(partialTick);
                        Vec3 jobVev3 = new Vec3(globalPos.pos().getX() + 0.5, globalPos.pos().getY() + 0.5, globalPos.pos().getZ() + 0.5);
                        RenderGlobal.disableDepthTest();
                        RenderUtil.drawConnectLine(eyeVec3, jobVev3, 0.05,
                                new Color4f(1, 1, 1),
                                Color4f.fromColor(Configs.renderVillageJobSiteTracerColor.getColor(), 1.0F),
                                Configs.renderVillageJobSiteTracerColor.getColor());
                        RenderGlobal.enableDepthTest();
                    });
                }
            }
        }

        this.queue.clear();
    }
    //#endif
}
