package io.anuke.mindustry.io;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.utils.*;
import com.badlogic.gdx.utils.Json.Serializer;
import com.badlogic.gdx.utils.JsonWriter.OutputType;
import io.anuke.mindustry.Vars;
import io.anuke.mindustry.world.Map;
import io.anuke.ucore.UCore;
import io.anuke.ucore.core.Settings;
import io.anuke.ucore.graphics.Pixmaps;

public class Maps implements Disposable{
	private IntMap<Map> maps = new IntMap<>();
	private ObjectMap<String, Map> mapNames = new ObjectMap<>();
	private Map networkMap;
	private int lastID;
	private Json json = new Json();

	public Maps() {
		json.setOutputType(OutputType.json);
		json.setElementType(ArrayContainer.class, "maps", Map.class);
		json.setSerializer(Color.class, new ColorSerializer());
	}

	public Iterable<Map> list(){
		return maps.values();
	}

	public void setNetworkMap(Pixmap pixmap){
		if(networkMap != null){
			networkMap.pixmap.dispose();
			networkMap = null;
		}
		networkMap = new Map();
		networkMap.custom = true;
		networkMap.pixmap = pixmap;
		networkMap.visible = false;
		networkMap.name = "network map";
		networkMap.id = -1;
	}

	public Map getMap(int id){
		if(id == -1){
			return networkMap;
		}
		return maps.get(id);
	}

	public Map getMap(String name){
		return mapNames.get(name);
	}

	public void loadMaps(){
		if(!loadMapFile(Gdx.files.internal("maps/maps.json"))){
			throw new RuntimeException("Failed to load maps!");
		}

		if(!Vars.gwt) {
			if (!loadMapFile(Vars.customMapDirectory.child("maps.json"))) {
				try {
					Vars.customMapDirectory.child("maps.json").writeString("{}", false);
				} catch (Exception e) {
					throw new RuntimeException("Failed to create custom map directory!");
				}
			}
		}
	}
	
	public void removeMap(Map map){
		maps.remove(map.id);
		mapNames.remove(map.name);
		Array<Map> out = new Array<>();
		for(Map m : maps.values()){
			if(m.custom){
				out.add(m);
			}
		}
		saveMaps(out, Vars.customMapDirectory.child("maps.json"));
	}
	
	public void saveAndReload(Map map, Pixmap out){
		if(map.pixmap != null && out != map.pixmap && map.texture != null){
			map.texture.dispose();
			map.texture = new Texture(out);
		}else if (out == map.pixmap){
			map.texture.draw(out, 0, 0);
		}
		
		map.pixmap = out;
		if(map.texture == null) map.texture = new Texture(map.pixmap);
		
		if(map.id == -1){
			if(mapNames.containsKey(map.name)){
				map.id = mapNames.get(map.name).id;
			}else{
				map.id = ++lastID;
			}
		}
		
		if(!Settings.has("hiscore" + map.name)){
			Settings.defaults("hiscore" + map.name, 0);
		}
		
		saveCustomMap(map);
		Vars.ui.levels.reload();
	}

	public void saveMaps(Array<Map> array, FileHandle file){
		json.toJson(new ArrayContainer(array), file);
	}
	
	public void saveCustomMap(Map toSave){
		toSave.custom = true;
		Array<Map> out = new Array<>();
		boolean added = false;
		for(Map map : maps.values()){
			if(map.custom){
				if(map.name.equals(toSave.name)){
					out.add(toSave);
					toSave.id = map.id;
					added = true;
				}else{
					out.add(map);
				}
			}
		}
		if(!added){
			out.add(toSave);
		}
		maps.remove(toSave.id);
		mapNames.remove(toSave.name);
		maps.put(toSave.id, toSave);
		mapNames.put(toSave.name, toSave);
		Pixmaps.write(toSave.pixmap, Vars.customMapDirectory.child(toSave.name + ".png"));
		saveMaps(out, Vars.customMapDirectory.child("maps.json"));
	}

	private boolean loadMapFile(FileHandle file){
		try{
			Array<Map> arr = json.fromJson(ArrayContainer.class, file).maps;
			if(arr != null){ //can be an empty map file
				for(Map map : arr){
					map.pixmap = new Pixmap(file.sibling(map.name + ".png"));
					map.texture = new Texture(map.pixmap);
					maps.put(map.id, map);
					mapNames.put(map.name, map);
					lastID = Math.max(lastID, map.id);
				}
			}
			return true;
		}catch(Exception e){
			if(!Vars.android) UCore.error(e);
			Gdx.app.error("Mindustry-Maps", "Failed loading map file: " + file);
			return false;
		}
	}

	@Override
	public void dispose(){
		for(Map map : maps.values()){
			map.texture.dispose();
			map.pixmap.dispose();
		}
		maps.clear();
	}

	public static class ArrayContainer{
		Array<Map> maps;

		public ArrayContainer() {
		}

		public ArrayContainer(Array<Map> maps) {
			this.maps = maps;
		}
	}

	private class ColorSerializer implements Serializer<Color>{

		@Override
		public void write(Json json, Color object, Class knownType){
			json.writeValue(object.toString().substring(0, 6));
		}

		@Override
		public Color read(Json json, JsonValue jsonData, Class type){
			return Color.valueOf(jsonData.asString());
		}

	}
}
