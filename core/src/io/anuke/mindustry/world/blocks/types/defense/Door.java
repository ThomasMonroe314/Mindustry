package io.anuke.mindustry.world.blocks.types.defense;

import com.badlogic.gdx.math.Rectangle;
import io.anuke.mindustry.Vars;
import io.anuke.mindustry.entities.TileEntity;
import io.anuke.mindustry.graphics.Fx;
import io.anuke.mindustry.world.Block;
import io.anuke.mindustry.world.Tile;
import io.anuke.mindustry.world.blocks.types.Wall;
import io.anuke.ucore.core.Effects;
import io.anuke.ucore.core.Effects.Effect;
import io.anuke.ucore.entities.Entities;
import io.anuke.ucore.entities.SolidEntity;
import io.anuke.ucore.graphics.Draw;
import io.anuke.ucore.util.Tmp;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import static io.anuke.mindustry.Vars.player;
import static io.anuke.mindustry.Vars.tilesize;

public class Door extends Wall{
	protected Effect openfx = Fx.dooropen;
	protected Effect closefx = Fx.doorclose;

	public Door(String name) {
		super(name);
		solid = false;
		solidifes = true;
	}
	
	@Override
	public void draw(Tile tile){
		DoorEntity entity = tile.entity();
		
		if(!entity.open){
			Draw.rect(name, tile.drawx(), tile.drawy());
		}else{
			Draw.rect(name + "-open", tile.drawx(), tile.drawy());
		}
	}
	
	@Override
	public boolean isSolidFor(Tile tile){
		DoorEntity entity = tile.entity();
		return !entity.open;
	}

	@Override
	public void tapped(Tile tile){
		DoorEntity entity = tile.entity();
		
		if(anyEntities(tile) && entity.open){
			return;
		}
		
		entity.open = !entity.open;
		if(!entity.open){
			Effects.effect(closefx, tile.drawx(), tile.drawy());
		}else{
			Effects.effect(openfx, tile.drawx(), tile.drawy());
		}
	}
	
	boolean anyEntities(Tile tile){
		int x = tile.x, y = tile.y;
		Block type = tile.block();
		Tmp.r2.setSize(type.width * Vars.tilesize, type.height * Vars.tilesize);
		Tmp.r2.setCenter(tile.drawx(), tile.drawy());
		
		for(SolidEntity e : Entities.getNearby(Vars.control.enemyGroup, x * tilesize, y * tilesize, tilesize * 2f)){
			Rectangle rect = e.hitbox.getRect(e.x, e.y);

			if(Tmp.r2.overlaps(rect)){
				return true;
			}
		}
		
		if(Tmp.r2.overlaps(player.hitbox.getRect(player.x, player.y))){
			return true;
		}
		
		return false;
	}
	
	@Override
	public TileEntity getEntity(){
		return new DoorEntity();
	}
	
	public class DoorEntity extends TileEntity{
		public boolean open = false;
		
		@Override
		public void write(DataOutputStream stream) throws IOException{
			super.write(stream);
			stream.writeBoolean(open);
		}
		
		@Override
		public void read(DataInputStream stream) throws IOException{
			super.read(stream);
			open = stream.readBoolean();
		}
	}

}
