package io.anuke.mindustry;

import static io.anuke.mindustry.Vars.*;

import com.badlogic.gdx.Input.Buttons;

import io.anuke.mindustry.ai.Pathfind;
import io.anuke.mindustry.resource.ItemStack;
import io.anuke.mindustry.world.Tile;
import io.anuke.mindustry.world.blocks.Blocks;
import io.anuke.mindustry.world.blocks.ProductionBlocks;
import io.anuke.ucore.core.Effects;
import io.anuke.ucore.core.Inputs;
import io.anuke.ucore.core.Sounds;
import io.anuke.ucore.scene.utils.Cursors;
import io.anuke.ucore.util.Mathf;

public class Input{
	
	public static void doInput(){
		
		//player is dead
		if(player.health <= 0) return;

		if(Inputs.keyUp("rotate"))
			rotation++;

		rotation %= 4;

		if(recipe != null && !Inventory.hasItems(recipe.requirements)){
			recipe = null;
			Cursors.restoreCursor();
		}
		
		
		if(Inputs.buttonUp(Buttons.LEFT) && recipe != null && 
				World.validPlace(World.tilex(), World.tiley(), recipe.result) && !ui.hasMouse()){
			Tile tile = World.tile(World.tilex(), World.tiley());
			
			if(tile == null)
				return; //just in case
			
			tile.setBlock(recipe.result);
			tile.rotation = rotation;

			Pathfind.updatePath();

			Effects.effect("place", World.roundx(), World.roundy());
			Effects.shake(2f, 2f);
			Sounds.play("place");

			for(ItemStack stack : recipe.requirements){
				Inventory.removeItem(stack);
			}

			if(!Inventory.hasItems(recipe.requirements)){
				recipe = null;
				Cursors.restoreCursor();
			}
		}

		if(recipe != null && Inputs.buttonUp(Buttons.RIGHT)){
			recipe = null;
			Cursors.restoreCursor();
		}
		
		Tile cursor = World.cursorTile();

		//block breaking
		if(Inputs.buttonDown(Buttons.RIGHT) && World.cursorNear() && cursor.artifical()
				&& cursor.block() != ProductionBlocks.core){
			Tile tile = cursor;
			breaktime += Mathf.delta();
			if(breaktime >= breakduration){
				Effects.effect("break", tile.entity);
				Effects.shake(3f, 1f);
				tile.setBlock(Blocks.air);
				Pathfind.updatePath();
				breaktime = 0f;
				Sounds.play("break");
			}
		}else{
			breaktime = 0f;
		}

	}
}