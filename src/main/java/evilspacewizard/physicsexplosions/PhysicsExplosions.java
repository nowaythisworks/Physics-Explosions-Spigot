
package evilspacewizard.physicsexplosions;

package brazil.physicsexplosions;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

public final class PhysicsExplosions extends JavaPlugin implements Listener {

	public static PhysicsExplosions instance;

	public static PhysicsExplosions getInstance() {
		return instance;
	}

	public void onEnable() {
		getServer().getPluginManager().registerEvents(this, this);
		
		//the x/20tps rate of the physics update frequency
		//the higher the value, the lesser the lag, but also the lower accuracy of simulation.
		int loopFrequency = 2;
		new PhysicsEntity().runTaskTimer(this, 0, loopFrequency);
	}
	
	//as usual, great with the method names
	@EventHandler
	public void boom(EntityExplodeEvent e) {
		final Entity en = e.getEntity();

		for (final Block b : e.blockList()) {
			if (b.getType().equals(Material.SNOW)) continue;
			
			final ArmorStand as = (ArmorStand) en.getWorld().spawnEntity(b.getLocation(), EntityType.ARMOR_STAND);
			
			as.getEquipment().setHelmet(new ItemStack(b.getType()));
			as.setVisible(false);
			
			//implement: cannot unequip stand
			//invulnerable doesnt seem to do the trick, at least in 16.5
			as.setInvulnerable(true);

			as.setGravity(true);
			
			PhysicsEntity.activeEntityCache.put(as, as.getLocation().toVector());
		}
	}

}
