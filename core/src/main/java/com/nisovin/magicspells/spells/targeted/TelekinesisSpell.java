package com.nisovin.magicspells.spells.targeted;

import java.util.HashSet;

import org.bukkit.Material;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.block.BlockFace;
import org.bukkit.event.Event.Result;
import org.bukkit.event.block.Action;
import org.bukkit.entity.LivingEntity;

import com.nisovin.magicspells.util.BlockUtils;
import com.nisovin.magicspells.util.MagicConfig;
import com.nisovin.magicspells.spells.TargetedSpell;
import com.nisovin.magicspells.util.compat.EventUtil;
import com.nisovin.magicspells.spells.TargetedLocationSpell;
import com.nisovin.magicspells.events.SpellTargetLocationEvent;
import com.nisovin.magicspells.events.MagicSpellsPlayerInteractEvent;

public class TelekinesisSpell extends TargetedSpell implements TargetedLocationSpell {
	
	private boolean checkPlugins;
	
	public TelekinesisSpell(MagicConfig config, String spellName) {
		super(config, spellName);
		
		checkPlugins = getConfigBoolean("check-plugins", true);
		
		losTransparentBlocks = new HashSet<>(losTransparentBlocks);
		losTransparentBlocks.remove(Material.LEVER);

		for (Material material : Material.values()) {
			if (!material.name().toUpperCase().contains("PRESSURE_PLATE")
					&& !material.name().toUpperCase().contains("BUTTON")) continue;
			losTransparentBlocks.remove(material);
		}
	}
	
	@Override
	public PostCastAction castSpell(LivingEntity caster, SpellCastState state, float power, String[] args) {
		if (state == SpellCastState.NORMAL && caster instanceof Player) {
			Block target = getTargetedBlock(caster, power, args);
			if (target == null) return noTarget(caster, args);

			SpellTargetLocationEvent event = new SpellTargetLocationEvent(this, caster, target.getLocation(), power, args);
			if (!event.callEvent()) return noTarget(caster, args);
			
			target = event.getTargetLocation().getBlock();

			boolean activated = activate((Player) caster, target);
			if (!activated) return noTarget(caster, args);

			playSpellEffects(caster, target.getLocation(), power, args);
		}
		return PostCastAction.HANDLE_NORMALLY;
	}

	@Override
	public boolean castAtLocation(LivingEntity caster, Location target, float power, String[] args) {
		if (!(caster instanceof Player)) return false;
		boolean activated = activate((Player) caster, target.getBlock());
		if (activated) playSpellEffects(caster, target, power, args);
		return activated;
	}

	@Override
	public boolean castAtLocation(LivingEntity caster, Location target, float power) {
		return castAtLocation(caster, target, power, null);
	}

	@Override
	public boolean castAtLocation(Location target, float power) {
		return false;
	}

	private boolean checkPlugins(Player caster, Block target) {
		if (!checkPlugins) return true;
		MagicSpellsPlayerInteractEvent event = new MagicSpellsPlayerInteractEvent(caster, Action.RIGHT_CLICK_BLOCK, caster.getEquipment().getItemInMainHand(), target, BlockFace.SELF);
		EventUtil.call(event);
		return event.useInteractedBlock() != Result.DENY;
	}

	private boolean activate(Player caster, Block target) {
		Material targetType = target.getType();
		if (targetType == Material.LEVER || BlockUtils.isButton(targetType)) {
			if (!checkPlugins(caster, target)) return false;
			BlockUtils.activatePowerable(target);
			return true;
		} else if (BlockUtils.isPressurePlate(targetType)) {
			if (!checkPlugins(caster, target)) return false;
			BlockUtils.activatePowerable(target);
			return true;
		}
		return false;
	}

}
