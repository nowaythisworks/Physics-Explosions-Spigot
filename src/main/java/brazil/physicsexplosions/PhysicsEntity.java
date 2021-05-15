package brazil.physicsexplosions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.ArmorStand;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.EulerAngle;
import org.bukkit.util.Vector;

/**
 * good luck understanding this lmao
 * i sure as hell cant
 * 
 * @author brazil
 */
public class PhysicsEntity extends BukkitRunnable {

	//active cache of all force-affected entities
	public static ConcurrentHashMap<ArmorStand, Vector> activeEntityCache = new ConcurrentHashMap<ArmorStand, Vector>();
	
	//index of veloicites of force affected entities
	public static HashMap<ArmorStand, Vector> velocityIndex = new HashMap<ArmorStand, Vector>();

	//index of entity lifetimes
	public static HashMap<ArmorStand, Integer> lifetimeIndex = new HashMap<ArmorStand, Integer>();
	
	@Override
	public void run() {
		
		//maximum lifespan of an entity
		final int maxLifeSpan = 150;
		
		if (activeEntityCache.size() == 0) return;
		
		for (ArmorStand a : activeEntityCache.keySet())
		{

			final Vector targetLocation = getRandomTargetLocation(a);
			
			final World world = a.getWorld();
			final Location loc = a.getLocation();
			
			// Cleans immediately stuck entities
			if (velocityIndex.containsKey(a))
			{
				if (velocityIndex.get(a).equals(loc.toVector()))
				{
					a.remove();
					safelyRemove(a);
					continue;
				}
			}
			
			// saves current update velocity
			Vector currentVelocity = getVelocity(a);
			
			//lifetime tracker
			int entityLifetime = 0;
			
			//bump lifetime every update
			//increasing the update frequency from the main class will require you to adjust the max life span
			if (lifetimeIndex.containsKey(a))
			{
				entityLifetime = lifetimeIndex.get(a);
				entityLifetime++;
			}
			
			lifetimeIndex.put(a, entityLifetime);
			
			final Location belowStand = loc;
			belowStand.setY(belowStand.getY()-2);
			
			if (world.getBlockAt(belowStand).getType().equals(Material.AIR))
			{
				/**
				 * Clean up still blocks by putting them into the ground,
				 * rather than having them awkwardly float.
				 */
				final Location aboveLoc = loc;
				
				aboveLoc.setY(loc.getY()+1);
				
				if (aboveLoc.getBlock().getType().equals(Material.AIR))
				{
					currentVelocity = targetLocation;
					loc.setY(loc.getY()-2);
					a.teleport(loc);
				}
				
			}
			else
			{
				/**
				 * sketchy rotation code
				 */
				if (!currentVelocity.equals(new Vector(0, 0, 0)))
				{
					if (activeEntityCache.containsKey(a))
					{
						if (!activeEntityCache.get(a).equals(loc.toVector()))
						{
							final EulerAngle rot = a.getHeadPose();
							a.setHeadPose(new EulerAngle(rot.getX() + 0.15, rot.getY() + 0.15, rot.getZ() + 0.15));
						}
					}
				}
				else
				{
					//Clear all "Stuck" entities
					safelyRemove(a);
					a.remove();
					continue;
					//there is definitely a better way to do this
				}
			}
			
			//Update Velocity Index with New Velocity
			velocityIndex.put(a, currentVelocity);
			activeEntityCache.put(a, loc.toVector());
			
			//Note: #setVelocity() completely overwrites existing velocity
			//additionally, #getVelocity() doesn't really do anything afaik, so we index it ourselves.
			a.setVelocity(currentVelocity);
			
			if (entityLifetime >= maxLifeSpan)
			{
				/**
				 * Remove after 150 iterations (on a 2/20 tick this is 10 iterations a second, so 15 seconds lifetime)
				 */
				a.remove();
				safelyRemove(a);
			}
		}
		
	}

	private void safelyRemove(ArmorStand a) {
		velocityIndex.remove(a);
		activeEntityCache.remove(a);
	}

	private Vector getRandomTargetLocation(ArmorStand a) {
		/**
		 * (pls pls pls optimize)
		 * vertical pathfinding code
		 * very early
		 */
		final ArrayList<Location> pass = new ArrayList<Location>();
		final ArrayList<Vector> targets = new ArrayList<Vector>();
		
		final Location loc = a.getLocation();
		final World world = a.getWorld();
		
		int xBlockPos = loc.getBlockX();
		int yBlockPos = loc.getBlockY()-2;
		int zBlockPos = loc.getBlockZ();
		
		/**
		 * i call this the double decker bus
		 * as in, there are two layers, it isn't very fast,
		 * and there are much better ways to do its job.
		 */
		
		for (int i = -1; i < 1; i++)
		{
			yBlockPos -= i;
			pass.add(new Location(world, xBlockPos - 1, yBlockPos, zBlockPos + 1));
			pass.add(new Location(world, xBlockPos - 1, yBlockPos, zBlockPos));
			pass.add(new Location(world, xBlockPos - 1, yBlockPos, zBlockPos - 1));

			pass.add(new Location(world, xBlockPos, yBlockPos, zBlockPos + 1));
			pass.add(new Location(world, xBlockPos, yBlockPos, zBlockPos));
			pass.add(new Location(world, xBlockPos, yBlockPos, zBlockPos - 1));

			pass.add(new Location(world, xBlockPos + 1, yBlockPos, zBlockPos + 1));
			pass.add(new Location(world, xBlockPos + 1, yBlockPos, zBlockPos));
			pass.add(new Location(world, xBlockPos + 1, yBlockPos, zBlockPos - 1));
		}
		
		for (Location l : pass)
		{
			//acceptable next position blocks is AIR
			if (l.getBlock().getType().equals(Material.AIR))
			{
				//let's go for an adventure:
				final Vector dir = loc
						.toVector()
						.subtract(l.toVector())
						.normalize()
						.multiply(-1.5);
				//what a journey
				
				targets.add(dir);
				//return dir;
			}
		}
		if (targets.size() > 0) return targets.get((int) (Math.random() * targets.size()));
		return null;
		
	}

	private Vector getVelocity(ArmorStand a) {
		/**
		 * #getVelocity of Entity doesnt do anything afaik
		 * so im caching this instead
		 */
		if (!velocityIndex.containsKey(a))
		{
			velocityIndex.put(a, new Vector(0,0,0));
		}
		
		return velocityIndex.get(a);
	}
}
