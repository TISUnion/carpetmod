package carpet.logging.logHelpers;

import carpet.logging.Logger;
import carpet.logging.LoggerRegistry;
import carpet.utils.Messenger;
import carpet.settings.CarpetSettings;
import com.google.common.collect.Sets;
import net.minecraft.entity.Entity;
import net.minecraft.entity.projectile.EntitySnowball;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * A generic log helper for logging the trajectory of things like blocks and throwables.
 */
public class TrajectoryLogHelper
{
    private static final int MAX_TICKS_PER_LINE = 20;
    public static final String TISCM_VISPROJ_LOGGER = "TISCM_VISPROJ_LOGGER";
    public static final Set<EntitySnowball> VISUALIZE_SNOWBALLS = Sets.newHashSet();

    private boolean doLog;
    private Logger logger;

    private ArrayList<Vec3d> positions = new ArrayList<>();
    private ArrayList<Vec3d> motions = new ArrayList<>();
    private ArrayList<String> collide = new ArrayList<>();
    private ArrayList<Long> gametick = new ArrayList<>();
    private String hitType;
    private World world;
    private Entity entity;

    public TrajectoryLogHelper(Entity entity, String logName)
    {
        this.logger = LoggerRegistry.getLogger(logName);
        this.entity = entity;
        this.doLog = this.logger.hasOnlineSubscribers();
    }

    public void onTick(double x, double y, double z, double motionX, double motionY, double motionZ, long gt)
    {
        if (!doLog) return;
        positions.add(new Vec3d(x, y, z));
        motions.add(new Vec3d(motionX, motionY, motionZ));
        collide.add("f");
        gametick.add(gt);
    }

    public void onCollide(double x, double y, double z, String type, World worldIn)
    {
        if (!doLog) return;
        positions.add(new Vec3d(x, y, z));
        motions.add(new Vec3d(0,0,0));
        collide.add("t");
        gametick.add(worldIn.getGameTime());
        hitType = type;
        world = worldIn;

        for (EntitySnowball entity : VISUALIZE_SNOWBALLS)
        {
            entity.getEntityWorld().removeEntity(entity);
        }
        VISUALIZE_SNOWBALLS.clear();
    }

    public void onFinish()
    {
        if (!doLog) return;
        logger.log( (option) -> {
            List<ITextComponent> comp = new ArrayList<>();
            switch (option)
            {
                case "brief":
                    comp.add(Messenger.s(""));
                    List<String> line = new ArrayList<>();

                    for (int i = 0; i < positions.size(); i++)
                    {
                        Vec3d pos = positions.get(i);
                        Vec3d mot = motions.get(i);
                        String coll = collide.get(i);
                        long gt = gametick.get(i);
                        line.add("w  x");
                        if (!coll.equals("t")) {
                            line.add(String.format("^w Tick: %d\ngt: %d\nx: %f\ny: %f\nz: %f\n------------\nmx: %f\nmy: %f\nmz: %f",
                                    i, gt, pos.x, pos.y, pos.z, mot.x, mot.y, mot.z));
                        }
                        else {
                            line.add(String.format("^w Hit:\nx: %f\ny: %f\nz: %f\n------------\nHit on: %s",
                                    pos.x, pos.y, pos.z, this.hitType));
                        }
                        if ((((i+1) % MAX_TICKS_PER_LINE)==0) || i == positions.size()-1)
                        {
                            comp.add(Messenger.c(line.toArray(new Object[0])));
                            line.clear();
                        }
                    }
                    break;
                case "full":
                    comp.add(Messenger.c("w ---------"));
                    for (int i = 0; i < positions.size(); i++)
                    {
                        Vec3d pos = positions.get(i);
                        Vec3d mot = motions.get(i);
                        comp.add(Messenger.c(
                                String.format("w tick: %3d pos",i),Messenger.dblt("w",pos.x, pos.y, pos.z),
                                "w   mot",Messenger.dblt("w",mot.x, mot.y, mot.z)));
                    }
                    break;
                case "visualize":
                    if (CarpetSettings.visualizeProjectileLoggerEnabled) {
                        if (!positions.isEmpty())
                        {
                            comp.add(Messenger.c("w visualize logger: visualized " + (positions.size() - 1) + " tick(s)"));
                        }
                        for (int i = 0; i < positions.size(); i++) {
                            Vec3d pos = positions.get(i);
                            EntitySnowball visEntity = new EntitySnowball(world, pos.x, pos.y, pos.z);
                            visEntity.setNoGravity(true);
                            if (i < positions.size() - 1) {
                                visEntity.setCustomName(new TextComponentString(i + ""));
                            } else {
                                visEntity.setCustomName(new TextComponentString("Hit"));
                            }
                            visEntity.setCustomNameVisible(true);
                            visEntity.addTag(TISCM_VISPROJ_LOGGER);
                            visEntity.logHelper = null;
                            visEntity.setInvulnerable(true);
                            try {
                                world.spawnEntity(visEntity);
                            } catch (NullPointerException exception) {
                                comp.clear();
                                comp.add(Messenger.c("w visualize logger: visualize failed, someone is killing snowballs?"));
                            }
                        }
                    }
                    else
                    {
                        boolean warn = true;
                        if (this.entity instanceof EntitySnowball)
                        {
                            EntitySnowball entitySnowball = (EntitySnowball)entity;
                            if (entitySnowball.isVisProjLogger())
                            {
                                warn = false;
                            }
                        }
                        if (warn)
                        {
                            comp.add(Messenger.c("w visualize logger: visualize is not enabled"));
                        }
                    }
                    break;
            }
            return comp.toArray(new ITextComponent[0]);
        });
        doLog = false;
    }
}

